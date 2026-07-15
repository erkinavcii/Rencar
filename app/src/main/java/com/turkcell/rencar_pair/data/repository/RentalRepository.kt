package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.ActiveRentalResponseDto
import com.turkcell.rencar_pair.data.model.FinishRentalResponseDto
import com.turkcell.rencar_pair.data.model.PayRentalDto
import com.turkcell.rencar_pair.data.model.PayRentalResponseDto
import com.turkcell.rencar_pair.data.model.RentalPhotosStateDto
import com.turkcell.rencar_pair.data.model.RentalResponseDto
import com.turkcell.rencar_pair.data.model.RentalStatsResponseDto

interface RentalRepository {
    suspend fun createRental(vehicleId: String, endDate: String?, plan: String = "DAILY"): Result<RentalResponseDto>
    suspend fun getMyRentals(): Result<List<RentalResponseDto>>
    suspend fun getRentalDetails(rentalId: String): Result<RentalResponseDto>

    // Eski uc: yalniz DAILY plani icin gecerli.
    suspend fun returnVehicle(rentalId: String): Result<RentalResponseDto>

    // Yalniz PREPARING durumundaki (henuz start edilmemis) yolculuk icin gecerli.
    suspend fun cancelPreparingRental(rentalId: String): Result<Unit>

    // PREPARING -> ACTIVE. 4 foto tamamlanmadan 409 doner.
    suspend fun startRental(rentalId: String): Result<RentalResponseDto>

    // ACTIVE -> COMPLETED, ucret dokumunu kilitler (PER_MINUTE/HOURLY icin).
    suspend fun finishRental(rentalId: String): Result<FinishRentalResponseDto>

    suspend fun payRental(rentalId: String, dto: PayRentalDto): Result<PayRentalResponseDto>

    suspend fun getPhotosState(rentalId: String): Result<RentalPhotosStateDto>

    suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        fileBytes: ByteArray,
        fileName: String = "photo.jpg",
    ): Result<RentalPhotosStateDto>

    // Aktif kiralama yoksa (404) basarili sonuc icinde null doner; gercek hatalarda
    // (ag/sunucu) Result.failure doner. GET /rentals'in aksine (bilinen 500 hatasi)
    // bu uc nokta canlida calisiyor - aktif kiralama kontrolu icin bu kullanilmali.
    suspend fun getActiveRental(): Result<ActiveRentalResponseDto?>

    suspend fun getStats(month: String? = null): Result<RentalStatsResponseDto>
}
