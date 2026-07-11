package com.turkcell.rencar_pair.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.model.VehicleResponseDto
import com.turkcell.rencar_pair.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.repository.RentalRepository
import javax.inject.Inject

// API'nin gercek arac tipini (SEDAN/SUV/HATCHBACK/STATION/MINIVAN) tasarimdaki
// Ekonomik/Konfor/SUV kategorilerine esler. Otomotiv kiralama sektorunde yerlesik
// segment kurali: Sedan/Hatchback -> Ekonomik, Station/Minivan -> Konfor, SUV -> SUV.
private fun mapApiTypeToCategory(type: String): VehicleCategory = when (type) {
    "SUV" -> VehicleCategory.SUV
    "STATION", "MINIVAN" -> VehicleCategory.KONFOR
    else -> VehicleCategory.EKONOMIK
}

private fun VehicleResponseDto.toMarker(): VehicleMarker = VehicleMarker(
    id = id,
    position = LatLng(latitude, longitude),
    priceLabel = "₺${pricePerDay.toInt()}/gün",
    category = mapApiTypeToCategory(type),
    inUse = status != "AVAILABLE",
    brand = brand,
    model = model,
    plate = plate,
    pricePerDay = pricePerDay.toInt(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            isLoading = true,
            isLocationAccuracyHigh = tokenManager.isLocationAccuracyHigh()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var hasResumedOnce = false

    init {
        loadVehicles()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.FilterSelected ->
                _uiState.update { it.copy(selectedFilter = intent.filter) }

            is HomeIntent.LocationPermissionResult ->
                _uiState.update { it.copy(hasLocationPermission = intent.granted) }

            is HomeIntent.UserLocationChanged ->
                _uiState.update { it.copy(userLocation = intent.location) }

            is HomeIntent.SearchQueryChanged ->
                _uiState.update { it.copy(searchQuery = intent.query) }

            is HomeIntent.VehicleSelected ->
                _uiState.update { it.copy(selectedVehicleId = intent.vehicleId) }

            HomeIntent.VehicleDetailDismissed ->
                _uiState.update { it.copy(selectedVehicleId = null) }

            HomeIntent.LocateMeClicked -> locateMe()

            HomeIntent.FindNearestVehicleClicked -> findNearestVehicle()

            HomeIntent.RefreshSettings -> {
                _uiState.update {
                    it.copy(isLocationAccuracyHigh = tokenManager.isLocationAccuracyHigh())
                }
                // Ekran ilk kez ON_RESUME aldiginda (soguk acilis) aktif kiralamaya
                // otomatik donuluyor. Sonraki ON_RESUME'larda yalnizca banner tazeleniyor;
                // aksi halde ActiveRentalScreen'den geri tusuyla cikan kullanici aninda
                // o ekrana geri firlatilir ve haritaya hic donemez.
                val isColdStart = !hasResumedOnce
                hasResumedOnce = true
                checkActiveRental(autoNavigate = isColdStart)
            }
        }
    }

    private fun checkActiveRental(autoNavigate: Boolean) {
        viewModelScope.launch {
            rentalRepository.getMyRentals()
                .onSuccess { rentals ->
                    val active = rentals.find { it.status == "ACTIVE" }
                    if (active == null) {
                        _uiState.update { it.copy(activeRental = null) }
                        return@onSuccess
                    }
                    // Arac detayi alinamasa bile kullanicinin aktif kiralamasina
                    // donebilmesi gerekir; marka/model/plaka olmadan da devam ediyoruz.
                    val vehicle = vehicleRepository.getVehicleDetails(active.vehicleId).getOrNull()
                    val summary = ActiveRentalSummary(
                        rentalId = active.id,
                        vehicleId = active.vehicleId,
                        vehicle = vehicle?.let {
                            ActiveRentalVehicle(
                                brand = it.brand,
                                model = it.model,
                                plate = it.plate,
                                pricePerDay = it.pricePerDay,
                            )
                        },
                    )
                    _uiState.update { it.copy(activeRental = summary) }
                    if (autoNavigate) {
                        _effect.send(HomeEffect.NavigateToActiveRental(summary))
                    }
                }
                .onFailure {
                    // Kiralama listesi cekilemedi. Mevcut banner'i silmiyoruz: gecici bir
                    // ag hatasi yuzunden kullanicinin aktif kiralamaya erisimini kesmek,
                    // eski bir banner'i bir sure daha gostermekten daha kotu. Hatayi sadece
                    // acilista bildiriyoruz; her ON_RESUME'da snackbar gostermek gurultu olur.
                    if (autoNavigate) {
                        _effect.send(HomeEffect.ShowError("Aktif kiralama bilgisi alınamadı."))
                    }
                }
        }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            vehicleRepository.getAvailableVehicles()
                .onSuccess { dtos ->
                    _uiState.update {
                        it.copy(isLoading = false, vehicles = dtos.map { dto -> dto.toMarker() })
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, vehicles = emptyList()) }
                    sendEffect(HomeEffect.ShowError(throwable.message ?: "Araçlar yüklenemedi."))
                }
        }
    }

    private fun locateMe() {
        if (_uiState.value.hasLocationPermission) {
            sendEffect(HomeEffect.CenterOnUserLocation)
        } else {
            sendEffect(HomeEffect.RequestLocationPermission)
        }
    }

    private fun findNearestVehicle() {
        val state = _uiState.value
        if (!state.hasLocationPermission) {
            sendEffect(HomeEffect.RequestLocationPermission)
            return
        }
        val nearest = state.nearestVehicle
        if (nearest == null) {
            sendEffect(HomeEffect.ShowError("Konumunuz henüz alınamadı, birazdan tekrar deneyin."))
            return
        }
        _uiState.update { it.copy(selectedVehicleId = nearest.id) }
        sendEffect(HomeEffect.CenterOnLocation(nearest.position))
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
