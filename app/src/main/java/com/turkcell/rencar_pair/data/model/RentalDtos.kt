package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateRentalDto(
    val vehicleId: String,
    val endDate: String? = null, // yalniz DAILY planda zorunlu (ISO 8601)
    val plan: String = "DAILY" // PER_MINUTE, HOURLY, DAILY (verilmezse DAILY - geriye uyum)
)

@Serializable
data class RentalVehicleSummaryDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String
)

@Serializable
data class RentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryDto,
    val plan: String, // PER_MINUTE, HOURLY, DAILY
    val status: String, // PREPARING, ACTIVE, COMPLETED, CANCELLED
    val startDate: String, // eski alan adi (deprecated); startedAt ile ayni deger, geriye uyum icin duruyor
    val startedAt: String,
    val endDate: String?, // yalniz DAILY planda dolu
    val endedAt: String?, // yolculuk surerken null
    val totalPrice: Double?, // DAILY: olustururken kilitlenir. PER_MINUTE/HOURLY: finish'e kadar null
    val startFee: Double,
    val serviceFee: Double?, // finish'te hesaplanir; DAILY'de null
    val discountAmount: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val paymentStatus: String, // UNPAID, PAID
    val paymentMethod: String?, // WALLET, CARD
    val createdAt: String
)

@Serializable
data class FinishRentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryDto,
    val plan: String,
    val status: String,
    val startDate: String,
    val startedAt: String,
    val endDate: String?,
    val endedAt: String?,
    val totalPrice: Double?,
    val startFee: Double,
    val serviceFee: Double?,
    val discountAmount: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val paymentStatus: String,
    val paymentMethod: String?,
    val createdAt: String,
    val usageFee: Double,
    val elapsedSeconds: Int
)

@Serializable
data class ActiveRentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryDto,
    val plan: String,
    val status: String,
    val startDate: String,
    val startedAt: String,
    val endDate: String?,
    val endedAt: String?,
    val totalPrice: Double?,
    val startFee: Double,
    val serviceFee: Double?,
    val discountAmount: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val paymentStatus: String,
    val paymentMethod: String?,
    val createdAt: String,
    val elapsedSeconds: Int,
    val currentCost: Double
)

@Serializable
data class PayRentalDto(
    val method: String, // WALLET, CARD
    val cardId: String? = null, // yalniz CARD yonteminde zorunlu
    val discountCode: String? = null
)

@Serializable
data class PaidCardSummaryDto(
    val brand: String,
    val last4: String
)

@Serializable
data class PayRentalResponseDto(
    val rentalId: String,
    val paymentStatus: String,
    val method: String,
    val totalPrice: Double,
    val discountAmount: Double,
    val paidAmount: Double,
    val walletBalance: Double?, // yalniz WALLET yonteminde dolu
    val card: PaidCardSummaryDto? // yalniz CARD yonteminde dolu
)

@Serializable
data class RentalPhotoDto(
    val side: String, // FRONT, BACK, LEFT, RIGHT
    val imageUrl: String,
    val createdAt: String
)

@Serializable
data class RentalPhotosStateDto(
    val rentalId: String,
    val photos: List<RentalPhotoDto>,
    val uploadedCount: Int,
    val remainingSides: List<String>,
    val photosComplete: Boolean
)

@Serializable
data class RentalStatsResponseDto(
    val month: String,
    val tripCount: Int,
    val totalSpent: Double,
    val totalMinutes: Int,
    val totalKm: Double
)

@Serializable
data class RentalUserSummary(
    val id: String,
    val email: String,
    val fullName: String
)

@Serializable
data class RentalVehicleSummary(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val status: String
)

@Serializable
data class AdminRentalResponseDto(
    val id: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val user: RentalUserSummary,
    val vehicle: RentalVehicleSummary
)
