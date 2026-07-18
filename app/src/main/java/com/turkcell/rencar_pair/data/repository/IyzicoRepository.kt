package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.CheckoutFormInitializeResponseDto
import com.turkcell.rencar_pair.data.model.IyzicoPaymentResponseDto

interface IyzicoRepository {
    // basketId "rental-<rentalId>" formatinda gonderilmeli; POST /rentals/:id/pay (IYZICO)
    // dogrulamasi bunu arar. buyer bilinerek gonderilmiyor (backend sandbox varsayilanini kullanir).
    suspend fun initializeCheckoutForm(
        price: Double,
        basketId: String,
        description: String? = null,
    ): Result<CheckoutFormInitializeResponseDto>

    // paymentStatus == "SUCCESS" oldugunda donen paymentId, POST /rentals/:id/pay'e iyzicoPaymentId olarak gecilir.
    suspend fun getCheckoutFormResult(token: String): Result<IyzicoPaymentResponseDto>
}
