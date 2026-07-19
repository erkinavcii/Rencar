package com.turkcell.rencar_pair.data.history

import androidx.compose.ui.geometry.Offset

data class HistoryTrip(
    val id: String,
    val vehicleName: String,
    val plate: String,
    val dateLabel: String,
    val fullDateLabel: String,
    val startDateMillis: Long,
    val durationLabel: String,
    val durationMinutes: Long,
    val price: Double,
    val paymentMethod: String?, // WALLET, CARD, IYZICO
    val routeStart: Offset,
    val routeEnd: Offset,
)

data class HistorySummary(
    val monthlyTripCount: Int,
    val monthlySpending: Double,
    val totalTripCount: Int,
    val totalSpending: Double,
    val trips: List<HistoryTrip>,
)