package com.turkcell.rencar_pair.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        state: HomeUiState = HomeUiState(),
        onIntent: (HomeIntent) -> Unit = {},
        onZoomIn: () -> Unit = {},
        onZoomOut: () -> Unit = {},
        onNavigateToActiveRental: (ActiveRentalSummary) -> Unit = {},
        onNavigateToReservation: (String, String, String, String, Int) -> Unit = { _, _, _, _, _ -> },
    ) {
        composeTestRule.setContent {
            RencarpairTheme {
                HomeScreen(
                    state = state,
                    onIntent = onIntent,
                    onTabSelected = {},
                    onNavigateToReservation = onNavigateToReservation,
                    onNavigateToActiveRental = onNavigateToActiveRental,
                    onMapReady = {},
                    onZoomIn = onZoomIn,
                    onZoomOut = onZoomOut,
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun aramaKutusunaYaziliyorVeSearchQueryChangedTetiklenir() {
        val intents = mutableListOf<HomeIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("home_search_input").performTextInput("Kadıköy")

        assertTrue(intents.any { it is HomeIntent.SearchQueryChanged && it.query == "Kadıköy" })
    }

    @Test
    fun filtreCipineTiklaninceFilterSelectedTetiklenir() {
        val intents = mutableListOf<HomeIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("home_filter_suv").performClick()

        assertTrue(intents.contains(HomeIntent.FilterSelected(CategoryFilter.SUV)))
    }

    @Test
    fun yakinlastirVeUzaklastirButonlariTiklamayiIletir() {
        var zoomInCalled = false
        var zoomOutCalled = false
        setContent(onZoomIn = { zoomInCalled = true }, onZoomOut = { zoomOutCalled = true })

        composeTestRule.onNodeWithTag("home_zoom_in_button").performClick()
        composeTestRule.onNodeWithTag("home_zoom_out_button").performClick()

        assertTrue(zoomInCalled)
        assertTrue(zoomOutCalled)
    }

    @Test
    fun konumumaGitTiklaninceLocateMeClickedTetiklenir() {
        val intents = mutableListOf<HomeIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("home_locate_me_button").performClick()

        assertTrue(intents.contains(HomeIntent.LocateMeClicked))
    }

    @Test
    fun enYakinAraciBulTiklaninceFindNearestVehicleClickedTetiklenir() {
        val intents = mutableListOf<HomeIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("home_find_nearest_button").performClick()

        assertTrue(intents.contains(HomeIntent.FindNearestVehicleClicked))
    }

    @Test
    fun aktifKiralamaBanneriTiklaninceNavigasyonTetiklenir() {
        val activeRental = ActiveRentalSummary(rentalId = "r1", vehicleId = "v1")
        var navigated: ActiveRentalSummary? = null
        setContent(
            state = HomeUiState(activeRental = activeRental),
            onNavigateToActiveRental = { navigated = it },
        )

        composeTestRule.onNodeWithTag("home_active_rental_banner").performClick()

        assertEquals(activeRental, navigated)
    }

    @Test
    fun secilenAracKartindaRezerveEtTiklaninceNavigasyonTetiklenir() {
        val vehicle = VehicleMarker(
            id = "v1",
            position = LatLng(40.99, 29.02),
            priceLabel = "₺5",
            category = VehicleCategory.EKONOMIK,
        )
        var navigated = false
        setContent(
            state = HomeUiState(vehicles = listOf(vehicle), selectedVehicleId = "v1"),
            onNavigateToReservation = { _, _, _, _, _ -> navigated = true },
        )

        composeTestRule.onNodeWithTag("home_reserve_button").performClick()

        assertTrue(navigated)
    }
}
