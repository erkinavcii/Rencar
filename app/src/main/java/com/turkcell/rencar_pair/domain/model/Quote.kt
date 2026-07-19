package com.turkcell.rencar_pair.domain.model

data class Quote(
    val vehicleId: String,
    val plan: String,
    val minutes: Int,
    val usageFee: Double,
    val startFee: Double,
    val serviceFee: Double,
    val estimatedTotal: Double,
)
