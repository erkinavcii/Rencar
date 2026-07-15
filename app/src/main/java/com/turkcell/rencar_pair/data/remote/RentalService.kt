package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RentalService {
    @POST("rentals")
    suspend fun createRental(@Body dto: CreateRentalDto): Response<RentalResponseDto>

    @GET("rentals")
    suspend fun getMyRentals(): Response<List<RentalResponseDto>>

    @GET("rentals/{id}")
    suspend fun getRentalDetails(@Path("id") id: String): Response<RentalResponseDto>

    @DELETE("rentals/{id}")
    suspend fun cancelPreparingRental(@Path("id") id: String): Response<Unit>

    // Eski uc: yalniz DAILY plani icin gecerli (PER_MINUTE/HOURLY icin finish kullanilir).
    @POST("rentals/{id}/return")
    suspend fun returnVehicle(@Path("id") id: String): Response<RentalResponseDto>

    @POST("rentals/{id}/start")
    suspend fun startRental(@Path("id") id: String): Response<RentalResponseDto>

    @POST("rentals/{id}/finish")
    suspend fun finishRental(@Path("id") id: String): Response<FinishRentalResponseDto>

    @POST("rentals/{id}/pay")
    suspend fun payRental(@Path("id") id: String, @Body dto: PayRentalDto): Response<PayRentalResponseDto>

    @GET("rentals/{id}/photos")
    suspend fun getPhotosState(@Path("id") id: String): Response<RentalPhotosStateDto>

    @Multipart
    @POST("rentals/{id}/photos")
    suspend fun uploadPhoto(
        @Path("id") id: String,
        @Part("side") side: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<RentalPhotosStateDto>

    @GET("rentals/active")
    suspend fun getActiveRental(): Response<ActiveRentalResponseDto>

    @GET("rentals/stats")
    suspend fun getStats(@Query("month") month: String? = null): Response<RentalStatsResponseDto>
}
