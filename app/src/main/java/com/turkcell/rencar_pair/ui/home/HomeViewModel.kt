package com.turkcell.rencar_pair.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.repository.VehicleRepository
import com.turkcell.rencar_pair.domain.model.Vehicle
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

// Backend'in gercek fiyat segmenti (ECONOMY/COMFORT/SUV) tasarimdaki Ekonomik/
// Konfor/SUV kategorilerine esler. Onceden bu esleme aracin govde tipinden
// (SEDAN/SUV/...) tahmin ediliyordu; canli 100 arac karsilastirildiginda bunlarin
// %45'i yanlis kategoriye dusuyordu (bkz. docs/decisions.md). segment, /vehicles
// yanitinda zaten tam bu ayrim icin var.
private fun mapSegmentToCategory(segment: String): VehicleCategory = when (segment) {
    "COMFORT" -> VehicleCategory.KONFOR
    "SUV" -> VehicleCategory.SUV
    else -> VehicleCategory.EKONOMIK
}

private fun mapTransmissionLabel(transmission: String): String = when (transmission) {
    "AUTOMATIC" -> "Otomatik"
    else -> "Manuel"
}

private fun Vehicle.toMarker(): VehicleMarker = VehicleMarker(
    id = id,
    position = LatLng(latitude, longitude),
    priceLabel = "₺${pricePerDay.toInt()}/gün",
    category = mapSegmentToCategory(segment),
    inUse = status != "AVAILABLE",
    brand = brand,
    model = model,
    plate = plate,
    pricePerDay = pricePerDay.toInt(),
    fuelPercent = fuelPercent,
    rangeKm = rangeKm,
    transmissionLabel = mapTransmissionLabel(transmission),
    seats = seats,
)

private const val VEHICLE_PAGE_SIZE = 20

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

            is HomeIntent.MapBoundsChanged ->
                _uiState.update { it.copy(visibleMapBounds = intent.bounds) }

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
            // GET /rentals (getMyRentals) canlida 500 doner (bilinen backend hatasi);
            // aktif kiralama kontrolu bu yuzden calisan GET /rentals/active uzerinden
            // yapiliyor. Bu uc nokta arac ozetini (brand/model/plate) zaten gomulu
            // dondurdugunden ayrica bir GET /vehicles/{id} cagrisina gerek kalmiyor
            // (o cagri zaten RENTED araclar icin 404 dondugunden hicbir zaman calismazdi).
            rentalRepository.getActiveRental()
                .onSuccess { active ->
                    if (active == null) {
                        _uiState.update { it.copy(activeRental = null) }
                        return@onSuccess
                    }
                    val summary = ActiveRentalSummary(
                        rentalId = active.id,
                        vehicleId = active.vehicleId,
                        vehicle = ActiveRentalVehicle(
                            brand = active.vehicle.brand,
                            model = active.vehicle.model,
                            plate = active.vehicle.plate,
                            pricePerDay = 0.0, // RentalVehicleSummaryDto pricePerDay tasimiyor
                        ),
                    )
                    _uiState.update { it.copy(activeRental = summary) }
                    if (autoNavigate) {
                        _effect.send(HomeEffect.NavigateToActiveRental(summary))
                    }
                }
                .onFailure {
                    // Gercek bir ag/sunucu hatasi (404 zaten onSuccess'te null olarak
                    // ele alindi). Mevcut banner'i silmiyoruz: gecici bir hata yuzunden
                    // kullanicinin aktif kiralamaya erisimini kesmek, eski bir banner'i
                    // bir sure daha gostermekten daha kotu. Hatayi sadece acilista
                    // bildiriyoruz; her ON_RESUME'da snackbar gostermek gurultu olur.
                    if (autoNavigate) {
                        _effect.send(HomeEffect.ShowError("Aktif kiralama bilgisi alınamadı."))
                    }
                }
        }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            vehicleRepository.getAvailableVehicles(page = 1, limit = VEHICLE_PAGE_SIZE)
                .onSuccess { dtos ->
                    _uiState.update {
                        it.copy(isLoading = false, vehicles = dtos.map { dto -> dto.toMarker() })
                    }
                    if (dtos.size == VEHICLE_PAGE_SIZE) {
                        loadRemainingVehiclePages(nextPage = 2)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, vehicles = emptyList()) }
                    sendEffect(HomeEffect.ShowError(throwable.message ?: "Araçlar yüklenemedi."))
                }
        }
    }

    // Ilk sayfa (20 arac) hemen gosterildikten sonra, "Yakininda N arac" gercek
    // toplami yansitsin diye kalan sayfalar arka planda sessizce cekilip haritaya
    // eklenir; kullaniciya ekstra bir yukleniyor gostergesi gosterilmez (bkz.
    // docs/decisions.md, "Ana Harita — Arac Sayisi 20 ile Sabitlenmis" karari).
    private suspend fun loadRemainingVehiclePages(nextPage: Int) {
        var page = nextPage
        while (true) {
            val dtos = vehicleRepository.getAvailableVehicles(page = page, limit = VEHICLE_PAGE_SIZE).getOrNull()
            if (dtos.isNullOrEmpty()) break
            _uiState.update { it.copy(vehicles = it.vehicles + dtos.map { dto -> dto.toMarker() }) }
            if (dtos.size < VEHICLE_PAGE_SIZE) break
            page++
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
