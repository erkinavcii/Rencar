package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.VehicleResponseDto
import com.turkcell.rencar_pair.data.remote.VehicleService
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleService: VehicleService,
) : VehicleRepository {

    override suspend fun getAvailableVehicles(): Result<List<VehicleResponseDto>> = runCatching {
        val response = vehicleService.getAvailableVehicles()
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: emptyList()
    }

    override suspend fun getVehicleDetails(id: String): Result<VehicleResponseDto> = runCatching {
        val response = vehicleService.getVehicleDetails(id)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun getQuote(id: String, plan: String, minutes: Int): Result<com.turkcell.rencar_pair.data.model.QuoteResponseDto> = runCatching {
        val response = vehicleService.getQuote(id, plan, minutes)
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