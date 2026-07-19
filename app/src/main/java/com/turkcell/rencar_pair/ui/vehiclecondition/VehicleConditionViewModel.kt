package com.turkcell.rencar_pair.ui.vehiclecondition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.repository.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleConditionViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VehicleConditionUiState(
            mode = if (savedStateHandle.get<String>("mode") == "AFTER") {
                VehicleConditionMode.AFTER
            } else {
                VehicleConditionMode.BEFORE
            },
            rentalId = savedStateHandle["rentalId"] ?: "",
            vehicleId = savedStateHandle["vehicleId"] ?: "",
            brand = savedStateHandle["brand"] ?: "",
            model = savedStateHandle["model"] ?: "",
            plate = savedStateHandle["plate"] ?: "",
            pricePerDay = savedStateHandle.get<String>("pricePerDay")?.toDoubleOrNull() ?: 0.0,
            durationSeconds = savedStateHandle.get<String>("durationSeconds")?.toLongOrNull() ?: 0L,
            distanceMeters = savedStateHandle.get<String>("distanceMeters")?.toDoubleOrNull() ?: 0.0,
        ),
    )
    val uiState: StateFlow<VehicleConditionUiState> = _uiState.asStateFlow()

    private val _effect = Channel<VehicleConditionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: VehicleConditionIntent) {
        when (intent) {
            is VehicleConditionIntent.PhotoCaptured -> onPhotoCaptured(intent.side, intent.bytes)

            VehicleConditionIntent.ConfirmClicked -> confirm()

            VehicleConditionIntent.BackClicked -> onBackClicked()
        }
    }

    // BEFORE modunda geri cikmak, PREPARING durumundaki kiralamayi backend'de yariminda
    // asili birakirdi (bkz. docs/decisions.md); artik cancelPreparingRental cagrilarak
    // kiralama gercekten iptal ediliyor. Rental zaten start edilmisse (PREPARING degilse)
    // bu cagri hata donebilir, bu durumda da kullanici yine de geri gonderilir - baska
    // yapilabilecek bir sey yok, sonucu snackbar ile kesmeye gerek yok.
    private fun onBackClicked() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.mode == VehicleConditionMode.BEFORE) {
                rentalRepository.cancelPreparingRental(state.rentalId)
            }
            _effect.send(VehicleConditionEffect.NavigateBack)
        }
    }

    private fun onPhotoCaptured(side: VehicleSide, bytes: ByteArray) {
        val state = _uiState.value
        if (side in state.checkedSides || state.uploadingSide != null) return
        when (state.mode) {
            // Surus oncesi fotograflar sunucuya yuklenir: /start, 4 yon tamamlanmadan
            // 409 dondugu icin isaretlemenin tek dogruluk kaynagi sunucu yanitidir.
            VehicleConditionMode.BEFORE -> uploadPhoto(side, bytes)

            // API v2'de teslim (AFTER) fotograflari icin bir uc nokta yok; cekim UX'i
            // gercek kamera/galeriyle yapilir ama gorsel yalnizca yerel kontrol
            // listesini isaretler.
            VehicleConditionMode.AFTER ->
                _uiState.update { it.copy(checkedSides = it.checkedSides + side) }
        }
    }

    private fun uploadPhoto(side: VehicleSide, bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(uploadingSide = side) }
            rentalRepository.uploadPhoto(
                rentalId = _uiState.value.rentalId,
                side = side.apiName,
                fileBytes = bytes,
                fileName = "${side.apiName.lowercase()}.jpg",
            )
                .onSuccess { photosState ->
                    val uploadedSides = photosState.photos
                        .mapNotNull { photo -> VehicleSide.entries.find { it.apiName == photo.side } }
                        .toSet()
                    _uiState.update { it.copy(uploadingSide = null, checkedSides = uploadedSides) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(uploadingSide = null) }
                    _effect.send(
                        VehicleConditionEffect.ShowError(error.message ?: "Fotoğraf yüklenemedi."),
                    )
                }
        }
    }

    private fun confirm() {
        val state = _uiState.value
        if (!state.isConfirmEnabled) return
        when (state.mode) {
            VehicleConditionMode.BEFORE -> startRental(state)
            VehicleConditionMode.AFTER -> returnVehicle(state)
        }
    }

    private fun startRental(state: VehicleConditionUiState) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            rentalRepository.startRental(state.rentalId)
                .onSuccess { navigateToActiveRental(state) }
                .onFailure { error ->
                    // Bazi planlarda kiralama olusturuldugu anda ACTIVE olabilir; start bu
                    // durumda hata dondururse kullaniciyi cikmaza sokmadan devam edilir.
                    val current = rentalRepository.getRentalDetails(state.rentalId).getOrNull()
                    if (current?.status == "ACTIVE") {
                        navigateToActiveRental(state)
                    } else {
                        _effect.send(
                            VehicleConditionEffect.ShowError(error.message ?: "Sürüş başlatılamadı."),
                        )
                    }
                }
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    private suspend fun navigateToActiveRental(state: VehicleConditionUiState) {
        _effect.send(
            VehicleConditionEffect.NavigateToActiveRental(
                rentalId = state.rentalId,
                vehicleId = state.vehicleId,
                brand = state.brand,
                model = state.model,
                plate = state.plate,
                pricePerDay = state.pricePerDay,
            ),
        )
    }

    // Eski POST rentals/{id}/return yalniz DAILY plani icin gecerli (docs/decisions.md);
    // PER_MINUTE/HOURLY POST rentals/{id}/finish gerektiriyor. Ekranin elinde hangi
    // plana ait oldugu bilgisi yok, bu yuzden bitirmeden hemen once sunucudan sorulur
    // (nav-arg olarak tasimak yerine tek ek sorgu - rentalId zaten elde var).
    private fun returnVehicle(state: VehicleConditionUiState) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val rentalDetails = rentalRepository.getRentalDetails(state.rentalId).getOrNull()
            if (rentalDetails == null) {
                _uiState.update { it.copy(isSubmitting = false) }
                _effect.send(VehicleConditionEffect.ShowError("Kiralama durumu doğrulanamadı. Lütfen tekrar deneyin."))
                return@launch
            }

            if (rentalDetails.plan == "DAILY") {
                val result = rentalRepository.returnVehicle(state.rentalId)
                _uiState.update { it.copy(isSubmitting = false) }
                result
                    .onSuccess { rental ->
                        _effect.send(
                            VehicleConditionEffect.NavigateToTripSummary(
                                rentalId = rental.id,
                                brand = state.brand,
                                model = state.model,
                                plate = state.plate,
                                durationSeconds = state.durationSeconds,
                                distanceMeters = state.distanceMeters,
                                totalPrice = rental.totalPrice ?: 0.0,
                            ),
                        )
                    }
                    .onFailure { _effect.send(VehicleConditionEffect.ShowError(it.message ?: "Kiralama bitirilemedi.")) }
            } else {
                val result = rentalRepository.finishRental(state.rentalId)
                _uiState.update { it.copy(isSubmitting = false) }
                result
                    .onSuccess { finished ->
                        _effect.send(
                            VehicleConditionEffect.NavigateToTripSummary(
                                rentalId = finished.id,
                                brand = state.brand,
                                model = state.model,
                                plate = state.plate,
                                durationSeconds = state.durationSeconds,
                                distanceMeters = state.distanceMeters,
                                totalPrice = finished.totalPrice ?: 0.0,
                            ),
                        )
                    }
                    .onFailure { _effect.send(VehicleConditionEffect.ShowError(it.message ?: "Kiralama bitirilemedi.")) }
            }
        }
    }
}
