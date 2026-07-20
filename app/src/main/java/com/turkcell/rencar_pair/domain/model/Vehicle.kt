package com.turkcell.rencar_pair.domain.model

data class Vehicle(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val pricePerDay: Double,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val fuelPercent: Int,
    val rangeKm: Int,
    val transmission: String,
    val seats: Int,
    val segment: String,
)
