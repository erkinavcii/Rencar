package com.turkcell.rencar_pair.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private fun mockVehicles(): List<VehicleMarker> = listOf(
    VehicleMarker("v1", LatLng(40.9928, 29.0245), "₺28", VehicleCategory.EKONOMIK),
    VehicleMarker("v2", LatLng(40.9945, 29.0320), "₺38", VehicleCategory.KONFOR),
    VehicleMarker("v3", LatLng(40.9875, 29.0290), "₺32", VehicleCategory.SUV),
    VehicleMarker("v4", LatLng(40.9860, 29.0230), "₺26", VehicleCategory.EKONOMIK),
    VehicleMarker("v5", LatLng(40.9890, 29.0310), "Kullanımda", VehicleCategory.KONFOR, inUse = true),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(vehicles = mockVehicles()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.FilterSelected ->
                _uiState.update { it.copy(selectedFilter = intent.filter) }

            is HomeIntent.LocationPermissionResult ->
                _uiState.update { it.copy(hasLocationPermission = intent.granted) }

            HomeIntent.LocateMeClicked -> locateMe()

            HomeIntent.Logout -> logout()
        }
    }

    private fun locateMe() {
        if (_uiState.value.hasLocationPermission) {
            sendEffect(HomeEffect.CenterOnUserLocation)
        } else {
            sendEffect(HomeEffect.RequestLocationPermission)
        }
    }

    private fun logout() {
        tokenManager.clearTokens()
        sendEffect(HomeEffect.NavigateToOnboarding)
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
