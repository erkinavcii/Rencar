package com.turkcell.rencar_pair.data.remote

import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.data.model.RefreshTokenDto
import com.turkcell.rencar_pair.data.model.VehicleLocationPoint
import com.turkcell.rencar_pair.data.session.SessionManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aktif yolculuktaki aracin CANLI konumunu dinleyen Socket.IO istemcisi.
 *
 * Sunucu sozlesmesi: '/ws/locations' namespace'ine musteri token'iyla baglanilir; yalniz
 * KENDI aktif kiralamasindaki aracin karesi 'my-vehicle' event'iyle gelir (payload:
 * { ts, vehicle: { vehicleId, latitude, longitude, ... } }). Aktif kiralama yoksa event hic
 * gelmez - akis sessiz kalir. rentalId parametre olarak gecilmez, sunucu token'dan cikarir.
 *
 * AUTH: token handshake'te auth.token ile gider. EVENT_CONNECT_ERROR alinirsa (token suresi
 * dolmus olabilir) bir kez TokenAuthenticator'daki ile ayni refresh cagrisi yapilir; o da
 * basarisiz olursa oturum tamamen sonlandirilir (TokenManager.clearTokens +
 * SessionManager.notifySessionExpired) - bu, REST katmanindaki 401 davranisiyla tutarli olsun
 * diye eklendi, referans ornekte (gist) yoktu.
 */
@Singleton
class RideLocationClient @Inject constructor(
    private val tokenManager: TokenManager,
    private val authService: AuthService,
    private val sessionManager: SessionManager,
) {
    fun vehiclePositionStream(): Flow<VehicleLocationPoint> = callbackFlow {
        var socket: Socket? = null
        var triedRefresh = false

        fun teardown() {
            socket?.let {
                it.off()
                it.disconnect()
                it.close()
            }
            socket = null
        }

        fun connectWith(token: String) {
            val opts = IO.Options().apply {
                auth = mapOf("token" to token)
                forceNew = true
                reconnection = true
            }
            val s = IO.socket(ApiConfig.BASE_URL.trimEnd('/') + NAMESPACE, opts)

            s.on(MY_VEHICLE_EVENT) { args ->
                parsePoint(args)?.let { trySend(it) }
            }
            s.on(Socket.EVENT_CONNECT_ERROR) {
                if (!triedRefresh) {
                    triedRefresh = true
                    launch {
                        val fresh = refreshAccessToken()
                        teardown()
                        if (fresh != null) connectWith(fresh) else close()
                    }
                }
            }
            socket = s
            s.connect()
        }

        val token = tokenManager.getAccessToken()
        if (token == null) {
            close()
        } else {
            connectWith(token)
        }

        awaitClose { teardown() }
    }

    private suspend fun refreshAccessToken(): String? {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken == null) {
            tokenManager.clearTokens()
            sessionManager.notifySessionExpired()
            return null
        }

        val response = runCatching { authService.refreshTokens(RefreshTokenDto(refreshToken)) }.getOrNull()
        val tokens = response?.takeIf { it.isSuccessful }?.body()
        if (tokens == null) {
            tokenManager.clearTokens()
            sessionManager.notifySessionExpired()
            return null
        }

        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
        return tokens.accessToken
    }

    private fun parsePoint(args: Array<Any?>): VehicleLocationPoint? {
        val root = args.getOrNull(0) as? JSONObject ?: return null
        val vehicle = root.optJSONObject("vehicle") ?: return null
        val lat = vehicle.optDouble("latitude", Double.NaN)
        val lng = vehicle.optDouble("longitude", Double.NaN)
        if (lat.isNaN() || lng.isNaN()) return null
        return VehicleLocationPoint(latitude = lat, longitude = lng)
    }

    private companion object {
        const val NAMESPACE = "/ws/locations"
        const val MY_VEHICLE_EVENT = "my-vehicle"
    }
}
