package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.model.LoginDto
import com.turkcell.rencar_pair.data.model.VerifyOtpDto
import com.turkcell.rencar_pair.data.remote.AdminApprovalService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Yuz eslestirme esigini gecen basvurulari "AI ile Anında Onayla" demo butonuyla
 * aninda onaylamak icin kullanilir. Backend'de bu proje icin tanimli sabit bir demo
 * admin hesabini kullanir; OTP dogrulamasi da backend'de gercek SMS yerine sabit
 * "123456" koduyla simule edilmektedir (bkz. docs/ml-face-matching.md).
 *
 * GUVENLIK NOTU (bilincli MVP kisayolu, gozden kacan bir hata degil): Asagidaki
 * ADMIN_PHONE/ADMIN_OTP sabitleri statik analiz/AI kod taramasi tarafindan
 * "hardcoded credentials" (CWE-798) olarak isaretlenecektir - bu tespit dogrudur.
 * Kapsam karari: ayri bir Python/ngrok back-channel servisi yerine dogrudan
 * uygulama-ici demo akisi kullanilmasi hocanin yonlendirmesiyle alindi ("uygulama
 * ici yeterli, demo icin ayri servise gerek yok"). Bu spesifik uygulama (demo admin
 * hesabinin istemciye gomulmesi) ise o kapsam karari icinde alinan bir muhendislik
 * kisayoludur. Gercek bir urette admin yetkisi asla istemciye gomulmez; onay karari
 * sunucu tarafinda (ornegin ayri bir backend servisinde) verilir - bkz.
 * docs/face_matching_architecture_plan.md (Yontem B / Zero-Trust alternatifi) ve
 * docs/decisions.md.
 */
@Singleton
class AdminApprovalRepository @Inject constructor(
    private val adminApprovalService: AdminApprovalService,
) {
    suspend fun approveViaAi(licenseId: String): Result<Unit> = try {
        val loginResponse = adminApprovalService.login(LoginDto(phone = ADMIN_PHONE))
        if (!loginResponse.isSuccessful) {
            Result.failure(Exception("Admin girişi başlatılamadı."))
        } else {
            val otpResponse = adminApprovalService.verifyOtp(
                VerifyOtpDto(phone = ADMIN_PHONE, code = ADMIN_OTP),
            )
            val adminToken = otpResponse.body()?.accessToken
            if (!otpResponse.isSuccessful || adminToken == null) {
                Result.failure(Exception("Admin doğrulaması başarısız."))
            } else {
                val approveResponse = adminApprovalService.approve(licenseId, "Bearer $adminToken")
                if (approveResponse.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        Exception(approveResponse.errorBody()?.string() ?: "Onay başarısız."),
                    )
                }
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        // Bilincli demo/MVP kisayolu - bkz. sinif basindaki GUVENLIK NOTU. Prod'da
        // asla yer almamali (CWE-798); dogru mimari docs/face_matching_architecture_plan.md'de.
        const val ADMIN_PHONE = "+905550000000"
        const val ADMIN_OTP = "123456"
    }
}
