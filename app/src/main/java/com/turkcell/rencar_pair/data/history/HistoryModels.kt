package com.turkcell.rencar_pair.data.history

import androidx.compose.ui.geometry.Offset

data class HistoryTrip(
    val id: String,
    val vehicleName: String,
    val dateLabel: String,
    val durationLabel: String,
    val price: Double,
    val routeStart: Offset,
    val routeEnd: Offset,
)

data class HistorySummary(
    val monthlyTripCount: Int,
    val monthlySpending: Double,
    val trips: List<HistoryTrip>,
)