package com.turkcell.rencar_pair.data.wallet

import com.turkcell.rencar_pair.data.model.CreateCardDto
import com.turkcell.rencar_pair.data.model.TopupDto
import com.turkcell.rencar_pair.data.remote.WalletService
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletService: WalletService
) : WalletRepository {

    override suspend fun getBalance(): Result<Double> = runCatching {
        val response = walletService.getWallet()
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.balance ?: 0.0
    }

    override suspend fun getCards(): Result<List<PaymentCard>> = runCatching {
        val response = walletService.getCards()
        if (!response.isSuccessful) error(response.apiMessage())
        val dtos = response.body() ?: emptyList()
        dtos.map { dto ->
            PaymentCard(
                id = dto.id,
                number = "• ${dto.last4}",
                expiryDate = "%02d/%02d".format(dto.expMonth, dto.expYear % 100),
                type = if (dto.brand == "VISA") "VISA" else "MC",
                isDefault = dto.isDefault
            )
        }
    }

    override suspend fun getTransactions(): Result<List<WalletTransaction>> = runCatching {
        val response = walletService.getWallet()
        if (!response.isSuccessful) error(response.apiMessage())
        val txs = response.body()?.transactions ?: emptyList()
        txs.map { dto ->
            PaymentCard(id = "", number = "", expiryDate = "", type = "", isDefault = false) // dummy check
            WalletTransaction(
                id = dto.id,
                title = dto.description,
                date = formatIsoToDisplay(dto.createdAt),
                amount = Math.abs(dto.amount),
                isIncome = dto.amount >= 0.0
            )
        }
    }

    override suspend fun loadBalance(amount: Double, cardId: String): Result<Double> = runCatching {
        val response = walletService.topup(TopupDto(amount))
        if (!response.isSuccessful) error(response.apiMessage())
        response.body()?.balance ?: 0.0
    }

    override suspend fun addCard(number: String, expiryDate: String): Result<PaymentCard> = runCatching {
        val cleanNumber = number.replace(" ", "")
        val lastFour = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else "0000"
        val brand = if (cleanNumber.startsWith("4")) "VISA" else "MASTERCARD"
        
        val parts = expiryDate.split("/")
        val month = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val yearPart = parts.getOrNull(1)?.toIntOrNull() ?: 2028
        val year = if (yearPart < 100) 2000 + yearPart else yearPart

        val response = walletService.addCard(
            CreateCardDto(
                brand = brand,
                last4 = lastFour,
                expMonth = month,
                expYear = year
            )
        )
        if (!response.isSuccessful) error(response.apiMessage())
        val dto = response.body() ?: error("Sunucudan bos yanit alindi.")
        PaymentCard(
            id = dto.id,
            number = "• ${dto.last4}",
            expiryDate = "%02d/%02d".format(dto.expMonth, dto.expYear % 100),
            type = if (dto.brand == "VISA") "VISA" else "MC",
            isDefault = dto.isDefault
        )
    }

    override suspend fun payFromBalance(amount: Double, title: String): Result<Double> = runCatching {
        val balanceResult = getBalance().getOrThrow()
        if (amount > balanceResult) {
            error("Yetersiz bakiye.")
        }
        balanceResult - amount
    }

    private fun formatIsoToDisplay(isoStr: String): String {
        val cleanValue = isoStr.trim()
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        val normalized = try {
            val dotIdx = cleanValue.indexOf('.')
            val zIdx = cleanValue.indexOf('Z')
            if (dotIdx != -1) {
                val endIdx = if (zIdx != -1) zIdx else cleanValue.length
                val frac = cleanValue.substring(dotIdx + 1, endIdx)
                val cleanFrac = if (frac.length > 3) frac.substring(0, 3) else frac.padEnd(3, '0')
                cleanValue.substring(0, dotIdx) + "." + cleanFrac + (if (zIdx != -1) "Z" else "")
            } else {
                cleanValue
            }
        } catch (e: Exception) {
            cleanValue
        }

        for (pattern in formats) {
            val formatter = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = runCatching { formatter.parse(normalized) }.getOrNull()
            if (date != null) {
                val displayFormat = SimpleDateFormat("d MMM yyyy · HH:mm", Locale("tr", "TR"))
                return displayFormat.format(date)
            }
        }
        return isoStr
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
