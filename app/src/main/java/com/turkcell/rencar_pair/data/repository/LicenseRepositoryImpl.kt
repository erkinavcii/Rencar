package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.*
import com.turkcell.rencar_pair.data.remote.AuthService
import com.turkcell.rencar_pair.data.remote.LicenseService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseRepositoryImpl @Inject constructor(
    private val licenseService: LicenseService,
    private val authService: AuthService
) : LicenseRepository {

    override suspend fun uploadLicense(
        frontBytes: ByteArray,
        backBytes: ByteArray,
        selfieBytes: ByteArray,
    ): Result<LicenseResponseDto> {
        return try {
            val frontPart = MultipartBody.Part.createFormData(
                "front",
                "front.jpg",
                frontBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val backPart = MultipartBody.Part.createFormData(
                "back",
                "back.jpg",
                backBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            // API v2 /license/upload yuz benzerlik analizi icin selfie'yi de bekliyor;
            // alan adi UploadLicenseDto sozlesmesindeki "selfie" ile ayni olmali.
            val selfiePart = MultipartBody.Part.createFormData(
                "selfie",
                "selfie.jpg",
                selfieBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val response = licenseService.uploadLicense(frontPart, backPart, selfiePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Yükleme başarısız."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLicenseStatus(): Result<LicenseStatusResponseDto> {
        return try {
            val response = licenseService.getLicenseStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Durum alınamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun adminApproveLicense(id: String): Result<AdminLicenseResponseDto> {
        return try {
            val response = licenseService.adminApproveLicense(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Onaylama başarısız."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyLicenseId(): Result<String> {
        return try {
            val meResponse = authService.getMe()
            if (!meResponse.isSuccessful || meResponse.body() == null) {
                return Result.failure(Exception("Kullanıcı profili alınamadı."))
            }
            val myUserId = meResponse.body()!!.id

            val licensesResponse = licenseService.adminGetLicenses(status = null)
            if (!licensesResponse.isSuccessful || licensesResponse.body() == null) {
                return Result.failure(Exception("Lisans listesi alınamadı."))
            }

            val myLicense = licensesResponse.body()!!.firstOrNull { it.userId == myUserId }
            if (myLicense != null) {
                Result.success(myLicense.id)
            } else {
                Result.failure(Exception("Kullanıcıya ait ehliyet kaydı bulunamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
