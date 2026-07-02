package com.turkcell.rencar_pair.ui.onboarding

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
)

sealed interface OnboardingIntent {
    data object StartApp  : OnboardingIntent
    data object GoToLogin : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToHome  : OnboardingEffect
    data object NavigateToLogin : OnboardingEffect
}
