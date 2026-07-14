package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuoteResponseDto(
    val vehicleId: String,
    val plan: String, // DAILY, HOURLY, PER_MINUTE
    val minutes: Int,
    val usageFee: Double,
    val startFee: Double,
    val serviceFee: Double,
    val estimatedTotal: Double
)
