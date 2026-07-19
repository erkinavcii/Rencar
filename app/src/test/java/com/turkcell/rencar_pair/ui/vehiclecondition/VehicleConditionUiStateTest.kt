package com.turkcell.rencar_pair.ui.vehiclecondition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleConditionUiStateTest {

    @Test
    fun `totalSides equals the number of VehicleSide entries`() {
        val state = VehicleConditionUiState()

        // VehicleSide: ON, ARKA, SOL, SAG (VehicleConditionContract.kt satir 4-9).
        assertEquals(4, state.totalSides)
    }

    @Test
    fun `checkedCount reflects the size of checkedSides`() {
        val state = VehicleConditionUiState(checkedSides = setOf(VehicleSide.ON, VehicleSide.ARKA))

        assertEquals(2, state.checkedCount)
    }

    @Test
    fun `remainingCount is totalSides minus checkedCount`() {
        val state = VehicleConditionUiState(checkedSides = setOf(VehicleSide.ON))

        assertEquals(3, state.remainingCount)
    }

    @Test
    fun `isConfirmEnabled is false when not all sides are checked`() {
        val state = VehicleConditionUiState(
            checkedSides = setOf(VehicleSide.ON, VehicleSide.ARKA, VehicleSide.SOL),
        )

        assertFalse(state.isConfirmEnabled)
    }

    @Test
    fun `isConfirmEnabled is false when all sides are checked but still submitting`() {
        val state = VehicleConditionUiState(
            checkedSides = VehicleSide.entries.toSet(),
            isSubmitting = true,
        )

        assertFalse(state.isConfirmEnabled)
    }

    @Test
    fun `isConfirmEnabled is false when all sides are checked but a side is still uploading`() {
        val state = VehicleConditionUiState(
            checkedSides = VehicleSide.entries.toSet(),
            uploadingSide = VehicleSide.SAG,
        )

        assertFalse(state.isConfirmEnabled)
    }

    @Test
    fun `isConfirmEnabled is true when all sides checked, not submitting, and nothing uploading`() {
        val state = VehicleConditionUiState(
            checkedSides = VehicleSide.entries.toSet(),
            isSubmitting = false,
            uploadingSide = null,
        )

        assertTrue(state.isConfirmEnabled)
    }
}
