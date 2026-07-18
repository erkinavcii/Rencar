package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.CheckoutFormInitializeResponseDto
import com.turkcell.rencar_pair.data.model.InitializeCheckoutFormDto
import com.turkcell.rencar_pair.data.model.IyzicoPaymentResponseDto
import com.turkcell.rencar_pair.data.remote.IyzicoService
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class IyzicoRepositoryImpl @Inject constructor(
    private val iyzicoService: IyzicoService,
) : IyzicoRepository {

    override suspend fun initializeCheckoutForm(
        price: Double,
        basketId: String,
        description: String?,
    ): Result<CheckoutFormInitializeResponseDto> = runCatching {
        val response = iyzicoService.initializeCheckoutForm(
            InitializeCheckoutFormDto(price = price, description = description, basketId = basketId)
        )
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
    }

    override suspend fun getCheckoutFormResult(token: String): Result<IyzicoPaymentResponseDto> = runCatching {
        val response = iyzicoService.getCheckoutFormResult(token)
        if (!response.isSuccessful) error(response.apiMessage())
        response.body() ?: error("Sunucudan bos yanit alindi.")
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
