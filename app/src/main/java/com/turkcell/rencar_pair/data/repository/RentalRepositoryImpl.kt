package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.ActiveRentalResponseDto
import com.turkcell.rencar_pair.data.model.CreateRentalDto
import com.turkcell.rencar_pair.data.model.FinishRentalResponseDto
import com.turkcell.rencar_pair.data.model.PayRentalDto
import com.turkcell.rencar_pair.data.model.PayRentalResponseDto
import com.turkcell.rencar_pair.data.model.RentalPhotosStateDto
import com.turkcell.rencar_pair.data.model.RentalResponseDto
import com.turkcell.rencar_pair.data.model.RentalStatsResponseDto
import com.turkcell.rencar_pair.data.model.VehicleLocationPoint
import com.turkcell.rencar_pair.data.remote.RentalService
import com.turkcell.rencar_pair.data.remote.RideLocationClient
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class RentalRepositoryImpl @Inject constructor(
    private val rentalService: RentalService,
    private val rideLocationClient: RideLocationClient,
) : RentalRepository {

    override suspend fun createRental(vehicleId: String, endDate: String?, plan: String): Result<RentalResponseDto> =
        runCatching {
            val response = rentalService.createRental(
                CreateRentalDto(vehicleId = vehicleId, endDate = endDate, plan = plan)
            )
            if (!response.isSuccessful) error(response.apiMessage())
            response.body() ?: error("Sunucudan bos yanit alindi.")
        }

    override suspend fun getMyRentals(): Result<List<RentalResponseDto>> = runCatching {
        val response = rentalService.getMyRentals()
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: emptyList()
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

    override suspend fun cancelPreparingRental(rentalId: String): Result<Unit> = runCatching {
        val response = rentalService.cancelPreparingRental(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
    }

    override suspend fun startRental(rentalId: String): Result<RentalResponseDto> = runCatching {
        val response = rentalService.startRental(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun finishRental(rentalId: String): Result<FinishRentalResponseDto> = runCatching {
        val response = rentalService.finishRental(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun payRental(rentalId: String, dto: PayRentalDto): Result<PayRentalResponseDto> = runCatching {
        val response = rentalService.payRental(rentalId, dto)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun getPhotosState(rentalId: String): Result<RentalPhotosStateDto> = runCatching {
        val response = rentalService.getPhotosState(rentalId)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        fileBytes: ByteArray,
        fileName: String,
    ): Result<RentalPhotosStateDto> = runCatching {
        val sideBody = side.toRequestBody("text/plain".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            "file",
            fileName,
            fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        val response = rentalService.uploadPhoto(rentalId, sideBody, filePart)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override fun vehiclePositionStream(): Flow<VehicleLocationPoint> =
        rideLocationClient.vehiclePositionStream()

    override suspend fun getActiveRental(): Result<ActiveRentalResponseDto?> = runCatching {
        val response = rentalService.getActiveRental()
        if (response.code() == 404) return@runCatching null
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()
    }

    override suspend fun getStats(month: String?): Result<RentalStatsResponseDto> = runCatching {
        val response = rentalService.getStats(month)
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
