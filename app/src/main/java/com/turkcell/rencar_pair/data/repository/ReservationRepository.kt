package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.domain.model.Reservation

interface ReservationRepository {
    suspend fun reserveVehicle(vehicleId: String): Result<Reservation>
    suspend fun getActiveReservation(): Result<Reservation>
    suspend fun cancelReservation(id: String): Result<Unit>
}
