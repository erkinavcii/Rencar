package com.turkcell.rencar_pair.data.repository

import com.turkcell.rencar_pair.domain.model.User

interface AuthRepository {
    suspend fun login(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, code: String): Result<String>
    suspend fun register(email: String, password: String, fullName: String, phone: String): Result<Unit>
    suspend fun getMe(): Result<User>

    /**
     * Kullanicinin KENDI refresh token'i ile access/refresh token'i yeniler ve
     * TokenManager'a kaydeder. Rol JWT'ye giris aninda gomuldugunden, rol sunucuda
     * degistikten sonra (or. AI ile ehliyet onayi PENDING -> CUSTOMER) elimizdeki
     * token hala eski rolu tasir; bu cagri yeni rolu tasiyan token'i almak icindir.
     */
    suspend fun refreshSession(): Result<Unit>
}
