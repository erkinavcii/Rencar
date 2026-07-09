package com.turkcell.rencar_pair.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.repository.AuthRepository
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
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        // Immediately show cached data (written at verifyOtp time) — no loading flash
        ProfileUiState(
            userName  = tokenManager.getUserName(),
            userPhone = tokenManager.getUserPhone(),
            isLocationAccuracyHigh = tokenManager.isLocationAccuracyHigh(),
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Refresh from server in background; cache is already visible
        refreshFromApi()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.Logout      -> logout()
            is ProfileIntent.EditProfile -> { /* TODO */ }
            ProfileIntent.SettingsClicked -> {
                _uiState.update {
                    it.copy(
                        showSettingsDialog = true,
                        isLocationAccuracyHigh = tokenManager.isLocationAccuracyHigh()
                    )
                }
            }
            ProfileIntent.SettingsDismissed -> {
                _uiState.update { it.copy(showSettingsDialog = false) }
            }
            is ProfileIntent.LocationAccuracyToggled -> {
                tokenManager.setLocationAccuracyHigh(intent.isHigh)
                _uiState.update { it.copy(isLocationAccuracyHigh = intent.isHigh) }
            }
        }
    }

    private fun refreshFromApi() {
        viewModelScope.launch {
            authRepository.getMe()
                .onSuccess { user ->
                    _uiState.update { it.copy(userName = user.fullName, userPhone = user.phone ?: it.userPhone) }
                }
                .onFailure { error ->
                    val msg = error.message.orEmpty()
                    if (msg.contains("Unauthorized") || msg.contains("401")) {
                        logout()
                    }
                }
        }
    }

    private fun logout() {
        tokenManager.clearTokens()
        viewModelScope.launch {
            _effect.send(ProfileEffect.NavigateToOnboarding)
        }
    }
}
