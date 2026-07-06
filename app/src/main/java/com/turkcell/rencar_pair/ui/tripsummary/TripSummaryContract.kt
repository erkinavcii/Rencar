package com.turkcell.rencar_pair.ui.tripsummary

data class TripSummaryUiState(
    val rentalId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val totalPrice: Double = 0.0,
    val cardLabel: String = "",
    val isLoadingCard: Boolean = true,
    val isPaying: Boolean = false,
) {
    val durationMinutes: Long get() = durationSeconds / 60
    val distanceKm: Double get() = distanceMeters / 1000.0
}

sealed interface TripSummaryIntent {
    data object PayClicked : TripSummaryIntent
}

sealed interface TripSummaryEffect {
    data object NavigateHome : TripSummaryEffect
    data class ShowError(val message: String) : TripSummaryEffect
}
