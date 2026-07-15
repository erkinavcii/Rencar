package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.CreateReservationDto
import com.turkcell.rencar_pair.data.model.ReservationResponseDto
import com.turkcell.rencar_pair.data.remote.ReservationService
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepositoryImpl @Inject constructor(
    private val reservationService: ReservationService
) : ReservationRepository {

    override suspend fun reserveVehicle(vehicleId: String): Result<ReservationResponseDto> = runCatching {
        val response = reservationService.reserveVehicle(CreateReservationDto(vehicleId))
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun getActiveReservation(): Result<ReservationResponseDto> = runCatching {
        val response = reservationService.getActiveReservation()
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun cancelReservation(id: String): Result<Unit> = runCatching {
        val response = reservationService.cancelReservation(id)
        if (!response.isSuccessful) error(response.apiMessage())
        Unit
    }

    private fun Response<*>.apiMessage(): String {
        val bodyString = errorBody()?.string()
        if (!bodyString.isNullOrBlank()) {
            try {
                return JSONObject(bodyString).getString("message")
            } catch (_: Exception) { }
        }
        return message().ifBlank { "Bir hata olustu. (${code()})" }
    }
}
