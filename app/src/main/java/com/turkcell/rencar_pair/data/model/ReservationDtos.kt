package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateReservationDto(
    val vehicleId: String
)

@Serializable
data class ReservationVehicleSummaryDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String, // SEDAN, SUV, HATCHBACK, STATION, MINIVAN
    val latitude: Double,
    val longitude: Double,
    val pricePerMinute: Double
)

@Serializable
data class ReservationResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: ReservationVehicleSummaryDto,
    val status: String, // ACTIVE, CONVERTED, CANCELLED, EXPIRED
    val expiresAt: String,
    val remainingSeconds: Long,
    val createdAt: String
)
