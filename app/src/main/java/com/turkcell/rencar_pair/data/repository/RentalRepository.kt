package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.RentalResponseDto

interface RentalRepository {
    suspend fun createRental(vehicleId: String, endDate: String): Result<RentalResponseDto>
    suspend fun getRentalDetails(rentalId: String): Result<RentalResponseDto>
    suspend fun returnVehicle(rentalId: String): Result<RentalResponseDto>
}
