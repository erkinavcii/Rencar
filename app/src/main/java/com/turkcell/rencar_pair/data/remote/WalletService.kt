package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface WalletService {
    @GET("wallet")
    suspend fun getWallet(): Response<WalletResponseDto>

    @POST("wallet/topup")
    suspend fun topup(@Body dto: TopupDto): Response<WalletResponseDto>

    @GET("cards")
    suspend fun getCards(): Response<List<CardResponseDto>>

    @POST("cards")
    suspend fun addCard(@Body dto: CreateCardDto): Response<CardResponseDto>

    @PATCH("cards/{id}/default")
    suspend fun setDefaultCard(@Path("id") id: String): Response<CardResponseDto>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): Response<Unit>
}
