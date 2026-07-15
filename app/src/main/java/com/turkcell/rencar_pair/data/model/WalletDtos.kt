package com.turkcell.rencar_pair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WalletTransactionDto(
    val id: String,
    val type: String, // TOPUP, RENTAL_PAYMENT, REFERRAL_BONUS
    val amount: Double,
    val rentalId: String? = null,
    val description: String,
    val createdAt: String
)

@Serializable
data class WalletResponseDto(
    val id: String,
    val balance: Double,
    val transactions: List<WalletTransactionDto>
)

@Serializable
data class TopupDto(
    val amount: Double
)

@Serializable
data class CreateCardDto(
    val brand: String, // VISA, MASTERCARD
    val last4: String,
    val expMonth: Int,
    val expYear: Int
)

@Serializable
data class CardResponseDto(
    val id: String,
    val brand: String, // VISA, MASTERCARD
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean,
    val createdAt: String
)
