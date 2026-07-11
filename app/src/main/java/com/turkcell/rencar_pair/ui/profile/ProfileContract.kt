package com.turkcell.rencar_pair.ui.profile

// ─── UiState ──────────────────────────────────────────────────────────────────

data class ProfileUiState(
    val userName: String        = "",
    val userPhone: String       = "",
    // License info — in future replace with a real API call
    val isLicenseApproved: Boolean = true,
    val licenseClass: String    = "B sınıfı · geçerli",
    val isLocationAccuracyHigh: Boolean = true,
    val showSettingsDialog: Boolean = false,
)

// ─── Intent ───────────────────────────────────────────────────────────────────

sealed interface ProfileIntent {
    data object Logout      : ProfileIntent
    data object EditProfile : ProfileIntent
    data object SettingsClicked : ProfileIntent
    data object SettingsDismissed : ProfileIntent
    data class LocationAccuracyToggled(val isHigh: Boolean) : ProfileIntent
}

// ─── Effect ───────────────────────────────────────────────────────────────────

sealed interface ProfileEffect {
    data object NavigateToOnboarding : ProfileEffect
}
