package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.model.LoginDto
import com.turkcell.rencar_pair.data.model.RefreshTokenDto
import com.turkcell.rencar_pair.data.model.RegisterDto
import com.turkcell.rencar_pair.data.model.UserResponseDto
import com.turkcell.rencar_pair.data.model.VerifyOtpDto
import com.turkcell.rencar_pair.data.remote.AuthService
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun login(phone: String): Result<Unit> = runCatching {
        val response = authService.login(LoginDto(phone = phone))
        if (!response.isSuccessful) error(response.apiMessage())
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<String> = runCatching {
        val response = authService.verifyOtp(VerifyOtpDto(phone = phone, code = code))
        if (!response.isSuccessful) error(response.apiMessage())
        val body = response.body() ?: error("Sunucudan gecersiz yanit.")
        tokenManager.saveTokens(body.accessToken, body.refreshToken)
        // Cache profile immediately so ProfileScreen loads instantly before API call
        tokenManager.saveUserProfile(
            name  = body.user.fullName,
            phone = body.user.phone ?: phone,
        )
        body.user.role
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
    ): Result<Unit> = runCatching {
        val response = authService.register(RegisterDto(email, password, fullName, phone))
        if (!response.isSuccessful) error(response.apiMessage())
    }

    override suspend fun refreshSession(): Result<Unit> = runCatching {
        val refreshToken = tokenManager.getRefreshToken()
            ?: error("Oturum bulunamadi.")
        // /auth/refresh, refresh token'i BODY'de bekler; header'daki (muhtemelen eski
        // rollu) access token'in onemi yoktur. TokenAuthenticator refresh yolunda
        // yeniden refresh denemedigi icin dongu riski de yoktur.
        val response = authService.refreshTokens(RefreshTokenDto(refreshToken))
        if (!response.isSuccessful) error(response.apiMessage())
        val body = response.body() ?: error("Sunucudan gecersiz yanit.")
        tokenManager.saveTokens(body.accessToken, body.refreshToken)
    }

    override suspend fun getMe(): Result<UserResponseDto> = runCatching {
        val response = authService.getMe()
        if (!response.isSuccessful) error(response.apiMessage())
        val body = response.body() ?: error("Sunucudan gecersiz yanit.")
        // Refresh local cache with latest server data
        tokenManager.saveUserProfile(
            name  = body.fullName,
            phone = body.phone ?: tokenManager.getUserPhone(),
        )
        body
    }

    // Parses the "message" field from the API error body.
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
