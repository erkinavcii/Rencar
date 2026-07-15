package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.CreateReservationDto
import com.turkcell.rencar_pair.data.model.ReservationResponseDto
import retrofit2.Response
import retrofit2.http.*

interface ReservationService {
    @POST("reservations")
    suspend fun reserveVehicle(@Body dto: CreateReservationDto): Response<ReservationResponseDto>

    @GET("reservations/active")
    suspend fun getActiveReservation(): Response<ReservationResponseDto>

    @DELETE("reservations/{id}")
    suspend fun cancelReservation(@Path("id") id: String): Response<Unit>
}
