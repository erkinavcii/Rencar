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
            is VehicleConditionIntent.PhotoMockCaptured ->
                _uiState.update { it.copy(checkedSides = it.checkedSides + intent.side) }

            VehicleConditionIntent.ConfirmClicked -> confirm()
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
    }

    private fun returnVehicle(state: VehicleConditionUiState) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
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
        }
    }
}
