package com.turkcell.rencar_pair.ui.history

import com.turkcell.rencar_pair.data.history.HistoryTrip

data class HistoryUiState(
    val isLoading: Boolean = false,
    val monthlyTripCount: Int = 0,
    val monthlySpending: Double = 0.0,
    val trips: List<HistoryTrip> = emptyList(),
)

sealed interface HistoryIntent {
    data object LoadHistory : HistoryIntent
    data object Refresh : HistoryIntent
}

sealed interface HistoryEffect {
    data class ShowMessage(val message: String) : HistoryEffect
}