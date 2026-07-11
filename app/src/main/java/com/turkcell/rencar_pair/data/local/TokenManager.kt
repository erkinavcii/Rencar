package com.turkcell.rencar_pair.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rencar_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN  = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_NAME     = "user_name"
        private const val KEY_USER_PHONE    = "user_phone"
        private const val KEY_LOCATION_ACCURACY_HIGH = "location_accuracy_high"
    }

    // ── Tokens ────────────────────────────────────────────────────────────────

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String?  = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    // ── User profile ──────────────────────────────────────────────────────────

    /** Called after successful registration or when updating profile info. */
    fun saveUserProfile(name: String, phone: String) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_PHONE, phone)
            apply()
        }
    }

    fun getUserName(): String  = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, "") ?: ""

    // ── Settings ──────────────────────────────────────────────────────────────

    fun setLocationAccuracyHigh(isHigh: Boolean) {
        prefs.edit().putBoolean(KEY_LOCATION_ACCURACY_HIGH, isHigh).apply()
    }

    fun isLocationAccuracyHigh(): Boolean = prefs.getBoolean(KEY_LOCATION_ACCURACY_HIGH, true)

    // ── Logout ────────────────────────────────────────────────────────────────

    /** Clears all persisted session and profile data. */
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_NAME)
            remove(KEY_USER_PHONE)
            remove(KEY_LOCATION_ACCURACY_HIGH)
            apply()
        }
    }
}
