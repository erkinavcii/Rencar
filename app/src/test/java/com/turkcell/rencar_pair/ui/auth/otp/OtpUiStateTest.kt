package com.turkcell.rencar_pair.ui.auth.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpUiStateTest {

    // --- isSubmitEnabled ---

    @Test
    fun `isSubmitEnabled is true for a 6-digit code when not loading`() {
        val state = OtpUiState(otpCode = "123456", isLoading = false)

        assertTrue(state.isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is false when code is shorter than 6 digits`() {
        val state = OtpUiState(otpCode = "12345", isLoading = false)

        assertFalse(state.isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is false while loading even with a 6-digit code`() {
        val state = OtpUiState(otpCode = "123456", isLoading = true)

        assertFalse(state.isSubmitEnabled)
    }

    // --- isTimerExpired: OtpContract.kt:10 duz esitlik ("== 0"), zaman damgasi degil ---

    @Test
    fun `isTimerExpired is true when timerSeconds is exactly zero`() {
        assertTrue(OtpUiState(timerSeconds = 0).isTimerExpired)
    }

    @Test
    fun `isTimerExpired is false when timerSeconds is one`() {
        assertFalse(OtpUiState(timerSeconds = 1).isTimerExpired)
    }

    @Test
    fun `isTimerExpired is false for the default timerSeconds value`() {
        assertFalse(OtpUiState().isTimerExpired)
    }

    @Test
    fun `isTimerExpired is false when timerSeconds is negative`() {
        assertFalse(OtpUiState(timerSeconds = -1).isTimerExpired)
    }

    // --- displayPhone ---

    @Test
    fun `displayPhone formats a 10-digit phone number with spacing`() {
        val state = OtpUiState(phone = "5551234567")

        assertEquals("+90 555 123 45 67", state.displayPhone)
    }

    @Test
    fun `displayPhone falls back to raw phone prefixed with +90 when length is not 10`() {
        val state = OtpUiState(phone = "555123")

        assertEquals("+90 555123", state.displayPhone)
    }
}
