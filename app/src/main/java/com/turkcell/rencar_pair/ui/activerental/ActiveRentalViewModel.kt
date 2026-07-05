package com.turkcell.rencar_pair.ui.activerental

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
    }

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            ActiveRentalIntent.Tick ->
                _uiState.update { it.copy(nowEpochMillis = System.currentTimeMillis()) }

            is ActiveRentalIntent.LocationUpdated -> onLocationUpdated(intent.location)

            // Kilitle/Ac icin backend'de bir uc nokta yok (RentalService'te karsiligi
            // yok); mevcut "Kilidi Ac" karariyla tutarli sekilde no-op birakildi.
            ActiveRentalIntent.LockToggleClicked -> Unit

            ActiveRentalIntent.EndRentalClicked -> endRental()
        }
    }

    private fun loadRentalStart() {
        viewModelScope.launch {
            val state = _uiState.value
            rentalRepository.getRentalDetails(state.rentalId)
                .onSuccess { rental ->
                    val startMillis = parseIsoUtc(rental.startDate) ?: System.currentTimeMillis()
                    _uiState.update { it.copy(startEpochMillis = startMillis) }
                }
                .onFailure {
                    // Rezervasyon az once bu ekrana yonlendirdiginden startDate her zaman
                    // gercek olmali; alinamazsa suredigen sayaci "simdi" baslatilarak
                    // ekranin kullanilamaz hale gelmesi engelleniyor.
                    _uiState.update { it.copy(startEpochMillis = System.currentTimeMillis()) }
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

    private fun endRental() {
        val state = _uiState.value
        if (state.isEnding) return
        viewModelScope.launch {
            _uiState.update { it.copy(isEnding = true) }
            val result = rentalRepository.returnVehicle(state.rentalId)
            _uiState.update { it.copy(isEnding = false) }
            result
                .onSuccess { rental ->
                    _effect.send(
                        ActiveRentalEffect.NavigateToTripSummary(
                            rentalId = rental.id,
                            brand = state.brand,
                            model = state.model,
                            plate = state.plate,
                            durationSeconds = state.elapsedSeconds,
                            distanceMeters = state.distanceMeters,
                            totalPrice = rental.totalPrice,
                        ),
                    )
                }
                .onFailure { _effect.send(ActiveRentalEffect.ShowError(it.message ?: "Kiralama bitirilemedi.")) }
        }
    }

    private fun parseIsoUtc(value: String): Long? {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return runCatching { formatter.parse(value)?.time }.getOrNull()
    }
}
