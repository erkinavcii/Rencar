package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface LicenseService {
    @Multipart
    @POST("license/upload")
    suspend fun uploadLicense(
        @Part front: MultipartBody.Part,
        @Part back: MultipartBody.Part,
        @Part selfie: MultipartBody.Part
    ): Response<LicenseResponseDto>

    @GET("license/status")
    suspend fun getLicenseStatus(): Response<LicenseStatusResponseDto>

    // Admin helpers for easy developer validation
    @GET("admin/licenses")
    suspend fun adminGetLicenses(
        @Query("status") status: String? = null
    ): Response<List<AdminLicenseResponseDto>>

    @PATCH("admin/licenses/{id}/approve")
    suspend fun adminApproveLicense(
        @Path("id") id: String
    ): Response<AdminLicenseResponseDto>

    @PATCH("admin/licenses/{id}/reject")
    suspend fun adminRejectLicense(
        @Path("id") id: String,
        @Body dto: RejectLicenseDto
    ): Response<AdminLicenseResponseDto>
}
