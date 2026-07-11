package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.model.RefreshTokenDto
import com.turkcell.rencar_pair.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val REFRESH_PATH = "auth/refresh"

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager,
    private val authServiceProvider: Provider<AuthService>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // auth/refresh cagrisinin kendisi 401 donerse tekrar refresh denemeye
        // calismak sonsuz donguye yol acar; bu durumda dogrudan vazgec.
        if (response.request.url.encodedPath.endsWith(REFRESH_PATH)) return null

        // Ayni istek zaten bir kez retry edildiyse (yeni token ile de basarisiz oldu) vazgec.
        if (responseCount(response) >= 2) return null

        val failedAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        return synchronized(this) {
            // Bu istek beklerken baska bir istek zaten token'i yenilemis olabilir;
            // bu durumda tekrar refresh cagirmak yerine mevcut yeni token ile devam et.
            val currentAccessToken = tokenManager.getAccessToken()
            if (currentAccessToken != null && currentAccessToken != failedAccessToken) {
                return@synchronized response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                tokenManager.clearTokens()
                sessionManager.notifySessionExpired()
                return@synchronized null
            }

            val refreshResult = runCatching {
                runBlocking { authServiceProvider.get().refreshTokens(RefreshTokenDto(refreshToken)) }
            }.getOrNull()

            val newTokens = refreshResult?.takeIf { it.isSuccessful }?.body()
            if (newTokens == null) {
                // Refresh token'in kendisi de gecersiz/suresi dolmus; kullanici gercekten cikis yapmis olmali.
                tokenManager.clearTokens()
                sessionManager.notifySessionExpired()
                return@synchronized null
            }

            tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
