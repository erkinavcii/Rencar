package com.turkcell.rencar_pair.ui.splash

data class SplashUiState(
    val isReady: Boolean = false,
)

sealed interface SplashIntent

// NavHost'un rota sabitlerini burada bilmek istemiyoruz; ViewModel yalnizca
// semantik hedefi bildirir, gercek rota esleme NavHost'ta yapilir (OTP akisindaki
// role: String -> rota deseniyle tutarli).
enum class SplashDestination { ONBOARDING, HOME, LICENSE }

sealed interface SplashEffect {
    data class NavigateTo(val destination: SplashDestination) : SplashEffect
}
