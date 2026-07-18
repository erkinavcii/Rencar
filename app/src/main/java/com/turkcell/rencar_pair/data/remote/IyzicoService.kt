package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.CheckoutFormInitializeResponseDto
import com.turkcell.rencar_pair.data.model.InitializeCheckoutFormDto
import com.turkcell.rencar_pair.data.model.IyzicoPaymentResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IyzicoService {
    @POST("iyzico/checkout-form/initialize")
    suspend fun initializeCheckoutForm(@Body dto: InitializeCheckoutFormDto): Response<CheckoutFormInitializeResponseDto>

    @GET("iyzico/checkout-form/result/{token}")
    suspend fun getCheckoutFormResult(@Path("token") token: String): Response<IyzicoPaymentResponseDto>
}
