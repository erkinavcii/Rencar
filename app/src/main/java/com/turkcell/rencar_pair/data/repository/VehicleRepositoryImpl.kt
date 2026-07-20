package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.QuoteResponseDto
import com.turkcell.rencar_pair.data.model.VehicleResponseDto
import com.turkcell.rencar_pair.data.remote.VehicleService
import com.turkcell.rencar_pair.domain.model.Quote
import com.turkcell.rencar_pair.domain.model.Vehicle
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

private fun VehicleResponseDto.toDomain(): Vehicle = Vehicle(
    id = id,
    plate = plate,
    brand = brand,
    model = model,
    pricePerDay = pricePerDay,
    status = status,
    latitude = latitude,
    longitude = longitude,
    fuelPercent = fuelPercent,
    rangeKm = rangeKm,
    transmission = transmission,
    seats = seats,
    segment = segment,
)

private fun QuoteResponseDto.toDomain(): Quote = Quote(
    vehicleId = vehicleId,
    plan = plan,
    minutes = minutes,
    usageFee = usageFee,
    startFee = startFee,
    serviceFee = serviceFee,
    estimatedTotal = estimatedTotal,
)

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleService: VehicleService,
) : VehicleRepository {

    override suspend fun getAvailableVehicles(page: Int, limit: Int): Result<List<Vehicle>> = runCatching {
        val response = vehicleService.getAvailableVehicles(page = page, limit = limit)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getVehicleDetails(id: String): Result<Vehicle> = runCatching {
        val response = vehicleService.getVehicleDetails(id)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun getQuote(id: String, plan: String, minutes: Int): Result<Quote> = runCatching {
        val response = vehicleService.getQuote(id, plan, minutes)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.toDomain() ?: error("Sunucudan bos yanit alindi.")
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