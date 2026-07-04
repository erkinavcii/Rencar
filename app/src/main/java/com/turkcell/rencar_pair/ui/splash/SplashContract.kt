package com.turkcell.rencar_pair.ui.splash

data class SplashUiState(
    val isReady: Boolean = false,
)

sealed interface SplashIntent

sealed interface SplashEffect {
    data object Finished : SplashEffect
}
