package com.turkcell.rencar_pair.data.wallet

data class PaymentCard(
    val id: String,
    val number: String,
    val expiryDate: String,
    val type: String, // VISA, MC
    val isDefault: Boolean
)

data class WalletTransaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: Double,
    val isIncome: Boolean
)

interface WalletRepository {
    suspend fun getBalance(): Result<Double>
    suspend fun getCards(): Result<List<PaymentCard>>
    suspend fun getTransactions(): Result<List<WalletTransaction>>
    suspend fun loadBalance(amount: Double, cardId: String): Result<Double>
    suspend fun addCard(number: String, expiryDate: String): Result<PaymentCard>
    suspend fun payFromBalance(amount: Double, title: String): Result<Double>
}
