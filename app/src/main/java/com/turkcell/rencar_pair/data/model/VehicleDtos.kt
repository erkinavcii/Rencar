package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateVehicleDto(
    val plate: String,
    val brand: String,
    val model: String,
    val type: String, // SEDAN, SUV, HATCHBACK, STATION, MINIVAN
    val pricePerDay: Double,
    val latitude: Double,
    val longitude: Double,
    val status: String? = null // AVAILABLE, RENTED, MAINTENANCE
)

@Serializable
data class VehicleResponseDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val status: String, // AVAILABLE, RENTED, RESERVED, MAINTENANCE
    val latitude: Double,
    val longitude: Double,
    val fuelPercent: Int,
    val rangeKm: Int,
    val transmission: String, // MANUAL, AUTOMATIC
    val seats: Int,
    val segment: String, // ECONOMY, COMFORT, SUV
    val pricePerHour: Double,
    val pricePerMinute: Double,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UpdateVehicleDto(
    val plate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val type: String? = null,
    val pricePerDay: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null
)

@Serializable
data class VehiclePositionDto(
    val vehicleId: String,
    val plate: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: String
)
