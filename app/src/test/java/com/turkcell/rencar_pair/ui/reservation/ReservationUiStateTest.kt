package com.turkcell.rencar_pair.ui.reservation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationUiStateTest {

    // --- pricePerMinute / pricePerHour: yuvarlama yok, ham bolme (ReservationContract.kt:29-30) ---

    @Test
    fun `pricePerMinute divides daily price by 1440`() {
        val state = ReservationUiState(pricePerDay = 1440.0)

        assertEquals(1.0, state.pricePerMinute, 0.0)
    }

    @Test
    fun `pricePerHour divides daily price by 24`() {
        val state = ReservationUiState(pricePerDay = 240.0)

        assertEquals(10.0, state.pricePerHour, 0.0)
    }

    // --- estimatedDurationLabel ---

    @Test
    fun `estimatedDurationLabel returns 30 dk for DAKIKALIK plan`() {
        val state = ReservationUiState(selectedPlan = RentalPlan.DAKIKALIK)

        assertEquals("30 dk", state.estimatedDurationLabel)
    }

    @Test
    fun `estimatedDurationLabel returns 1 sa for SAATLIK plan`() {
        val state = ReservationUiState(selectedPlan = RentalPlan.SAATLIK)

        assertEquals("1 sa", state.estimatedDurationLabel)
    }

    @Test
    fun `estimatedDurationLabel returns 1 gun for GUNLUK plan`() {
        val state = ReservationUiState(selectedPlan = RentalPlan.GUNLUK)

        assertEquals("1 gün", state.estimatedDurationLabel)
    }

    // --- isConfirmEnabled ---

    @Test
    fun `isConfirmEnabled is true when terms accepted and not submitting`() {
        val state = ReservationUiState(termsAccepted = true, isSubmitting = false)

        assertTrue(state.isConfirmEnabled)
    }

    @Test
    fun `isConfirmEnabled is false when terms are not accepted`() {
        val state = ReservationUiState(termsAccepted = false, isSubmitting = false)

        assertFalse(state.isConfirmEnabled)
    }

    @Test
    fun `isConfirmEnabled is false while submitting even if terms are accepted`() {
        val state = ReservationUiState(termsAccepted = true, isSubmitting = true)

        assertFalse(state.isConfirmEnabled)
    }
}
