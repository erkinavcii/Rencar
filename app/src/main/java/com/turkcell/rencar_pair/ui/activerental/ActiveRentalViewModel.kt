package com.turkcell.rencar_pair.ui.activerental

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.model.VehicleLocationPoint
import com.turkcell.rencar_pair.data.repository.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private const val COST_POLL_INTERVAL_MS = 5000L

@HiltViewModel
class ActiveRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ActiveRentalUiState(
            rentalId = savedStateHandle["rentalId"] ?: "",
            vehicleId = savedStateHandle["vehicleId"] ?: "",
            brand = savedStateHandle["brand"] ?: "",
            model = savedStateHandle["model"] ?: "",
            plate = savedStateHandle["plate"] ?: "",
        ),
    )
    val uiState: StateFlow<ActiveRentalUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ActiveRentalEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadRentalStart()
        pollActiveRental()
        observeVehicleLocation()
    }

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            ActiveRentalIntent.Tick ->
                _uiState.update { it.copy(nowEpochMillis = System.currentTimeMillis()) }

            // Gecici kilit backend'de ayri bir uc nokta gerektirmiyor (RentalService'te
            // karsiligi yok); yalnizca sayaci durdurmadan yerel UI durumunu degistirir.
            ActiveRentalIntent.LockToggleClicked ->
                _uiState.update { it.copy(isVehicleLocked = !it.isVehicleLocked) }

            ActiveRentalIntent.EndRentalClicked -> endRental()
        }
    }

    private fun loadRentalStart() {
        viewModelScope.launch {
            val state = _uiState.value
            rentalRepository.getRentalDetails(state.rentalId)
                .onSuccess { rental ->
                    // Bu ekrana Home'daki "devam eden kiralama" yonlendirmesiyle de
                    // ulasilabiliyor; rental gercekte hala PREPARING'de (foto/start hic
                    // tamamlanmamis) olabilir. Boyle bir durumda sahte bir "aktif surus"
                    // gostermek yerine kullanici foto/start akisina geri gonderilir.
                    if (rental.status != "ACTIVE") {
                        _effect.send(
                            ActiveRentalEffect.RedirectToVehicleConditionBefore(
                                rentalId = state.rentalId,
                                vehicleId = state.vehicleId,
                                brand = state.brand,
                                model = state.model,
                                plate = state.plate,
                            ),
                        )
                        return@launch
                    }
                    val startMillis = parseIsoUtc(rental.startDate) ?: System.currentTimeMillis()
                    _uiState.update {
                        it.copy(startEpochMillis = startMillis, plan = rental.plan, startFee = rental.startFee)
                    }
                }
                .onFailure {
                    // Rezervasyon az once bu ekrana yonlendirdiginden startDate her zaman
                    // gercek olmali; alinamazsa suredigen sayaci "simdi" baslatilarak
                    // ekranin kullanilamaz hale gelmesi engelleniyor.
                    _uiState.update { it.copy(startEpochMillis = System.currentTimeMillis()) }
                }
        }
    }

    // Gercek ucret (currentCost) ve mesafe (distanceKm) sunucudan periyodik olarak
    // sorulur (GET /rentals/active). Saat (elapsedSeconds/elapsedTimeLabel) ise ayri,
    // yerel Tick ile saniyede bir akmaya devam eder - yalnizca gorsel sayac, bu
    // sorgudan etkilenmez. Aktif kiralama bulunamazsa (404->null) veya sorgu
    // basarisiz olursa mevcut deger korunur, hata gosterilmez (best-effort tazeleme).
    private fun pollActiveRental() {
        viewModelScope.launch {
            while (isActive) {
                rentalRepository.getActiveRental()
                    .onSuccess { active ->
                        if (active != null) {
                            _uiState.update {
                                it.copy(currentCost = active.currentCost, distanceKm = active.distanceKm)
                            }
                        }
                    }
                delay(COST_POLL_INTERVAL_MS)
            }
        }
    }

    // Aracin canli konumu (Socket.IO). ViewModel yasam suresi boyunca acik kalir;
    // viewModelScope iptal edildiginde alt akis (RideLocationClient) de kapanir.
    private fun observeVehicleLocation() {
        viewModelScope.launch {
            rentalRepository.vehiclePositionStream().collect { point: VehicleLocationPoint ->
                _uiState.update {
                    it.copy(vehicleLocation = ActiveRentalLatLng(point.latitude, point.longitude))
                }
            }
        }
    }

    // Kiralamayi gercekten sonlandiran API cagrisi (return/finish) burada yapilmiyor;
    // teslim sonrasi 4 foto tamamlanmadan kiralama bitirilemeyecegi icin bu cagri
    // VehicleCondition-AFTER ekraninin onay adimina tasindi (bkz. VehicleConditionViewModel).
    private fun endRental() {
        val state = _uiState.value
        viewModelScope.launch {
            _effect.send(
                ActiveRentalEffect.NavigateToVehicleCondition(
                    rentalId = state.rentalId,
                    vehicleId = state.vehicleId,
                    brand = state.brand,
                    model = state.model,
                    plate = state.plate,
                    durationSeconds = state.elapsedSeconds,
                    distanceMeters = state.distanceKm * 1000.0,
                ),
            )
        }
    }

    private fun parseIsoUtc(value: String): Long? {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return runCatching { formatter.parse(value)?.time }.getOrNull()
    }
}
