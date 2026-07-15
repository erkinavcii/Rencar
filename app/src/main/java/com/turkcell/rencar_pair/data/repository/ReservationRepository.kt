package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.ReservationResponseDto

interface ReservationRepository {
    suspend fun reserveVehicle(vehicleId: String): Result<ReservationResponseDto>
    suspend fun getActiveReservation(): Result<ReservationResponseDto>
    suspend fun cancelReservation(id: String): Result<Unit>
}
