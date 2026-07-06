package com.turkcell.rencar_pair.data.wallet

import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeWalletRepository @Inject constructor() : WalletRepository {

    private var balance = 340.0

    private val cards = mutableListOf(
        PaymentCard("1", "• 4291", "08/27", "VISA", true),
        PaymentCard("2", "• 7740", "11/26", "MC", false)
    )

    private val transactions = mutableListOf(
        WalletTransaction("1", "Renault Clio kiralama", "Bugün • 14:32", 110.50, false),
        WalletTransaction("2", "Bakiye yükleme", "Dün • 09:10", 200.00, true)
    )

    override suspend fun getBalance(): Result<Double> {
        delay(600) // Simulating network latency
        return Result.success(balance)
    }

    override suspend fun getCards(): Result<List<PaymentCard>> {
        delay(500)
        return Result.success(cards.toList())
    }

    override suspend fun getTransactions(): Result<List<WalletTransaction>> {
        delay(600)
        return Result.success(transactions.toList())
    }

    override suspend fun loadBalance(amount: Double, cardId: String): Result<Double> {
        delay(800)
        balance += amount
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        transactions.add(
            0,
            WalletTransaction(
                id = (transactions.size + 1).toString(),
                title = "Bakiye yükleme",
                date = "Bugün • $currentTime",
                amount = amount,
                isIncome = true
            )
        )
        return Result.success(balance)
    }

    override suspend fun payFromBalance(amount: Double, title: String): Result<Double> {
        delay(800)
        if (amount > balance) {
            return Result.failure(IllegalStateException("Yetersiz bakiye."))
        }
        balance -= amount
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        transactions.add(
            0,
            WalletTransaction(
                id = (transactions.size + 1).toString(),
                title = title,
                date = "Bugün • $currentTime",
                amount = amount,
                isIncome = false,
            ),
        )
        return Result.success(balance)
    }

    override suspend fun addCard(number: String, expiryDate: String): Result<PaymentCard> {
        delay(800)
        val cleanNumber = number.replace(" ", "")
        val lastFour = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else "0000"
        val cardType = if (cleanNumber.startsWith("4")) "VISA" else "MC"

        val newCard = PaymentCard(
            id = (cards.size + 1).toString(),
            number = "• $lastFour",
            expiryDate = expiryDate,
            type = cardType,
            isDefault = cards.isEmpty()
        )
        cards.add(newCard)
        return Result.success(newCard)
    }
}
