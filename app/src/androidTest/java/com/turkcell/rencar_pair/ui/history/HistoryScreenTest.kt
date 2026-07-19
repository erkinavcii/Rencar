package com.turkcell.rencar_pair.ui.history

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.data.history.HistoryTrip
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleTrip = HistoryTrip(
        id = "t1",
        vehicleName = "Renault Clio",
        plate = "34 AB 123",
        dateLabel = "12 Tem",
        fullDateLabel = "12 Temmuz 2026",
        startDateMillis = 1_752_300_000_000L,
        durationLabel = "45 dk",
        durationMinutes = 45L,
        price = 120.0,
        paymentMethod = "WALLET",
        routeStart = Offset(0.2f, 0.2f),
        routeEnd = Offset(0.8f, 0.8f),
    )

    private fun setContent(
        state: HistoryUiState,
        onIntent: (HistoryIntent) -> Unit,
    ) {
        composeTestRule.setContent {
            RencarpairTheme {
                HistoryScreen(
                    state = state,
                    onIntent = onIntent,
                    onTabSelected = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun aramaKutusunaYaziliyorVeSearchQueryChangedTetiklenir() {
        val intents = mutableListOf<HistoryIntent>()
        setContent(state = HistoryUiState(trips = listOf(sampleTrip)), onIntent = { intents += it })

        composeTestRule.onNodeWithTag("history_search_input").performTextInput("Clio")

        assertTrue(intents.any { it is HistoryIntent.SearchQueryChanged && it.query == "Clio" })
    }

    @Test
    fun siralamaTiklaninceSortToggledTetiklenir() {
        val intents = mutableListOf<HistoryIntent>()
        setContent(state = HistoryUiState(trips = listOf(sampleTrip)), onIntent = { intents += it })

        composeTestRule.onNodeWithTag("history_sort_toggle").performClick()

        assertTrue(intents.contains(HistoryIntent.SortToggled))
    }

    @Test
    fun yolculukKartinaTiklaninceTripSelectedTetiklenir() {
        val intents = mutableListOf<HistoryIntent>()
        setContent(state = HistoryUiState(trips = listOf(sampleTrip)), onIntent = { intents += it })

        composeTestRule.onNodeWithTag("history_trip_card_t1").performClick()

        assertTrue(intents.contains(HistoryIntent.TripSelected("t1")))
    }
}
