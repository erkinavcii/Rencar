package com.turkcell.rencar_pair.ui.reservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.repository.RentalRepository
import com.turkcell.rencar_pair.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReservationUiState(
            vehicleId = savedStateHandle["vehicleId"] ?: "",
            brand = savedStateHandle["brand"] ?: "",
            model = savedStateHandle["model"] ?: "",
            plate = savedStateHandle["plate"] ?: "",
            pricePerDay = savedStateHandle.get<String>("pricePerDay")?.toDoubleOrNull() ?: 0.0,
        ),
    )
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ReservationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: ReservationIntent) {
        when (intent) {
            is ReservationIntent.PlanSelected ->
                _uiState.update { it.copy(selectedPlan = intent.plan) }

            is ReservationIntent.TermsToggled ->
                _uiState.update { it.copy(termsAccepted = intent.accepted) }

            ReservationIntent.ConfirmClicked -> confirmReservation()
        }
    }

    // API v2 kurali: kiralama YALNIZ aktif bir rezervasyon uzerinden acilir (POST /rentals,
    // aksi halde 409 doner). Bu yuzden "Rezervasyonu Tamamla" once POST /reservations ile
    // araci RESERVED durumuna alip, ancak basarili olursa POST /rentals'i cagiriyor.
    private fun confirmReservation() {
        val state = _uiState.value
        if (!state.isConfirmEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val reservationResult = reservationRepository.reserveVehicle(state.vehicleId)
            val reservation = reservationResult.getOrNull()
            if (reservation == null) {
                _uiState.update { it.copy(isSubmitting = false) }
                _effect.send(
                    ReservationEffect.ShowError(
                        reservationResult.exceptionOrNull()?.message ?: "Araç rezerve edilemedi.",
                    ),
                )
                return@launch
            }

            val endDate = endDateFor(state.selectedPlan)
            val result = rentalRepository.createRental(state.vehicleId, endDate)
            _uiState.update { it.copy(isSubmitting = false) }
            result
                .onSuccess { rental ->
                    _effect.send(
                        ReservationEffect.NavigateToVehicleCondition(
                            rentalId = rental.id,
                            vehicleId = state.vehicleId,
                            brand = state.brand,
                            model = state.model,
                            plate = state.plate,
                            pricePerDay = state.pricePerDay,
                        ),
                    )
                }
                .onFailure { _effect.send(ReservationEffect.ShowError(it.message ?: "Rezervasyon oluşturulamadı.")) }
        }
    }

    // Tasarimda bitis tarihi secen bir alan yok; secilen plana gore otomatik bir sure
    // hesaplaniyor (Dakikalik->+30dk, Saatlik->+1sa, Gunluk->+1gun). Gercek bitis,
    // kullanici araci iade ettiginde (POST /rentals/{id}/return) belirlenecek.
    // java.time yerine SimpleDateFormat kullanildi: minSdk 24'te java.time,
    // core library desugaring olmadan calismiyor.
    private fun endDateFor(plan: RentalPlan): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        when (plan) {
            RentalPlan.DAKIKALIK -> calendar.add(Calendar.MINUTE, 30)
            RentalPlan.SAATLIK -> calendar.add(Calendar.HOUR_OF_DAY, 1)
            RentalPlan.GUNLUK -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(calendar.time)
    }
}
