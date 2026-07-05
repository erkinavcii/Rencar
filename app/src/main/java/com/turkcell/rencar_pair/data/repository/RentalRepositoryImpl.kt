package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.CreateRentalDto
import com.turkcell.rencar_pair.data.model.RentalResponseDto
import com.turkcell.rencar_pair.data.remote.RentalService
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class RentalRepositoryImpl @Inject constructor(
    private val rentalService: RentalService,
) : RentalRepository {

    override suspend fun createRental(vehicleId: String, endDate: String): Result<RentalResponseDto> = runCatching {
        val response = rentalService.createRental(CreateRentalDto(vehicleId = vehicleId, endDate = endDate))
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun getRentalDetails(rentalId: String): Result<RentalResponseDto> = runCatching {
        val response = rentalService.getRentalDetails(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun returnVehicle(rentalId: String): Result<RentalResponseDto> = runCatching {
        val response = rentalService.returnVehicle(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    // API hata body'sindeki "message" alanini parse eder.
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
