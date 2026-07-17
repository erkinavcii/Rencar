package com.turkcell.rencar_pair.ui.activerental

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.model.RentalResponseDto
import com.turkcell.rencar_pair.data.model.VehicleLocationPoint
import com.turkcell.rencar_pair.data.repository.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

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
            pricePerDay = savedStateHandle.get<String>("pricePerDay")?.toDoubleOrNull() ?: 0.0,
        ),
    )
    val uiState: StateFlow<ActiveRentalUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ActiveRentalEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var lastLocation: ActiveRentalLatLng? = null

    init {
        loadRentalStart()
        observeVehicleLocation()
    }

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            ActiveRentalIntent.Tick ->
                _uiState.update { it.copy(nowEpochMillis = System.currentTimeMillis()) }

            is ActiveRentalIntent.LocationUpdated -> onLocationUpdated(intent.location)

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
                    val startMillis = parseIsoUtc(rental.startDate) ?: System.currentTimeMillis()
                    _uiState.update { current ->
                        // Tekrar giriste arac RENTED oldugundan GET /vehicles/{id} 404 doner ve
                        // gunluk ucret navigasyonla 0 gelir; bu durumda kiralamadan turetiyoruz.
                        // Taze akista gecerli bir fiyat geldiginden ona dokunulmuyor.
                        val resolvedPrice =
                            if (current.pricePerDay > 0.0) current.pricePerDay
                            else derivePricePerDay(rental) ?: current.pricePerDay
                        current.copy(
                            startEpochMillis = startMillis,
                            pricePerDay = resolvedPrice,
                            plan = rental.plan,
                            startFee = rental.startFee,
                        )
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

    // Backend totalPrice'i gunlukUcret * yukariYuvarlanmis_gun_sayisi (min 1 gun) olarak
    // hesapliyor. Bu yuzden gunluk ucreti totalPrice / gun_sayisi ile geri turetebiliyoruz.
    // Turetilen deger AVAILABLE arac listesindeki pricePerDay ile birebir dogrulandi.
    private fun derivePricePerDay(rental: RentalResponseDto): Double? {
        // totalPrice/endDate yalniz DAILY planda dolu (PER_MINUTE/HOURLY'de her ikisi de
        // null olabilir); bu turetme yalniz DAILY icin anlamli, digerlerinde null doner.
        val totalPrice = rental.totalPrice ?: return null
        if (totalPrice <= 0.0) return null
        val start = parseIsoUtc(rental.startDate) ?: return null
        val end = rental.endDate?.let { parseIsoUtc(it) } ?: return null
        val days = Math.round((end - start).toDouble() / 86_400_000.0).coerceAtLeast(1L)
        return totalPrice / days
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

    private fun onLocationUpdated(location: ActiveRentalLatLng) {
        val previous = lastLocation
        if (previous != null) {
            val delta = haversineMeters(previous, location)
            _uiState.update { it.copy(distanceMeters = it.distanceMeters + delta) }
        }
        lastLocation = location
    }

    // Kiralamayi gercekten sonlandiran API cagrisi (returnVehicle) burada yapilmiyor;
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
                    distanceMeters = state.distanceMeters,
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
