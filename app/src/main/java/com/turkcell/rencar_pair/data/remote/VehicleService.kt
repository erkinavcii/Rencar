package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface VehicleService {
    @GET("vehicles")
    suspend fun getAvailableVehicles(
        @Query("type") type: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20
    ): Response<List<VehicleResponseDto>>

    @GET("vehicles/{id}")
    suspend fun getVehicleDetails(@Path("id") id: String): Response<VehicleResponseDto>

    @GET("vehicles/{id}/quote")
    suspend fun getQuote(
        @Path("id") id: String,
        @Query("plan") plan: String,
        @Query("minutes") minutes: Int
    ): Response<QuoteResponseDto>
}
