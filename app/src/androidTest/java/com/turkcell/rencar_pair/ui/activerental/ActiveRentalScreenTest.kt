package com.turkcell.rencar_pair.ui.activerental

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActiveRentalScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        state: ActiveRentalUiState,
        onIntent: (ActiveRentalIntent) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            RencarpairTheme {
                ActiveRentalScreen(
                    state = state,
                    onIntent = onIntent,
                    onBack = onBack,
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun geriButonuOnBackCagirir() {
        var backCalled = false
        setContent(state = ActiveRentalUiState(), onBack = { backCalled = true })

        composeTestRule.onNodeWithTag("active_rental_back_button").performClick()

        assertTrue(backCalled)
    }

    @Test
    fun kilitliArackenKilidiAcTiklaninceLockToggleTetiklenir() {
        val intents = mutableListOf<ActiveRentalIntent>()
        setContent(
            state = ActiveRentalUiState(isVehicleLocked = true),
            onIntent = { intents += it },
        )

        composeTestRule.onNodeWithTag("active_rental_unlock_button").performClick()

        assertTrue(intents.contains(ActiveRentalIntent.LockToggleClicked))
    }

    @Test
    fun kilitliDegilkenKiralamayiBitirTiklaninceEndRentalTetiklenir() {
        val intents = mutableListOf<ActiveRentalIntent>()
        setContent(
            state = ActiveRentalUiState(isVehicleLocked = false),
            onIntent = { intents += it },
        )

        composeTestRule.onNodeWithTag("active_rental_end_button").performClick()

        assertTrue(intents.contains(ActiveRentalIntent.EndRentalClicked))
    }

    @Test
    fun kilitliDegilkenKilitleTiklaninceLockToggleTetiklenir() {
        val intents = mutableListOf<ActiveRentalIntent>()
        setContent(
            state = ActiveRentalUiState(isVehicleLocked = false),
            onIntent = { intents += it },
        )

        composeTestRule.onNodeWithTag("active_rental_lock_button").performClick()

        assertTrue(intents.contains(ActiveRentalIntent.LockToggleClicked))
    }
}
