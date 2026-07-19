package com.turkcell.rencar_pair.ui.auth.register

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUiStateTest {

    private fun validState(overrides: RegisterUiState.() -> RegisterUiState = { this }) =
        RegisterUiState(
            fullName = "Ayşe Yılmaz",
            email = "ayse@example.com",
            phone = "5551234567",
            password = "123456",
            isLoading = false,
        ).overrides()

    @Test
    fun `isSubmitEnabled is true when every field is valid`() {
        assertTrue(validState().isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is false when fullName is blank`() {
        assertFalse(validState { copy(fullName = "   ") }.isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is false when email does not contain at sign`() {
        assertFalse(validState { copy(email = "not-an-email") }.isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is true for a superficially invalid email that merely contains at sign`() {
        // RegisterContract.kt:13 yalniz "@" iceriyor mu diye bakiyor, format dogrulamasi yok.
        assertTrue(validState { copy(email = "a@") }.isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is false when phone length is not 10`() {
        assertFalse(validState { copy(phone = "555123456") }.isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is false when password is shorter than 6 characters`() {
        assertFalse(validState { copy(password = "12345") }.isSubmitEnabled)
    }

    @Test
    fun `isSubmitEnabled is false while loading`() {
        assertFalse(validState { copy(isLoading = true) }.isSubmitEnabled)
    }
}
