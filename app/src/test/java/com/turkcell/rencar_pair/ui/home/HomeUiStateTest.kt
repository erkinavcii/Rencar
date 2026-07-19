package com.turkcell.rencar_pair.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {

    private val referencePoint = LatLng(latitude = 41.0, longitude = 29.0)

    private fun vehicleAt(
        id: String,
        latitude: Double,
        longitude: Double = referencePoint.longitude,
        category: VehicleCategory = VehicleCategory.SUV,
    ): VehicleMarker = VehicleMarker(
        id = id,
        position = LatLng(latitude = latitude, longitude = longitude),
        priceLabel = "",
        category = category,
    )

    // --- nearbyVehicleCount: visibleMapBounds null iken haversineMeters <= 5km fallback'i ---

    @Test
    fun `nearbyVehicleCount includes vehicle well within 5km radius when bounds are null`() {
        val state = HomeUiState(
            vehicles = listOf(vehicleAt("v1", LAT_4000M_NORTH)),
            userLocation = referencePoint,
            visibleMapBounds = null,
        )

        val count = state.nearbyVehicleCount

        assertEquals(1, count)
    }

    @Test
    fun `nearbyVehicleCount excludes vehicle just past 5km radius when bounds are null`() {
        val state = HomeUiState(
            vehicles = listOf(vehicleAt("v1", LAT_5001M_NORTH)),
            userLocation = referencePoint,
            visibleMapBounds = null,
        )

        val count = state.nearbyVehicleCount

        assertEquals(0, count)
    }

    @Test
    fun `nearbyVehicleCount includes vehicle at the 5km boundary when bounds are null`() {
        // HomeContract.kt:134 karsilastirmasi "<=" kullaniyor, sinirdaki arac da sayilmali.
        val state = HomeUiState(
            vehicles = listOf(vehicleAt("v1", LAT_5000M_NORTH)),
            userLocation = referencePoint,
            visibleMapBounds = null,
        )

        val count = state.nearbyVehicleCount

        assertEquals(1, count)
    }

    @Test
    fun `nearbyVehicleCount includes vehicle at the exact same location as the user`() {
        val state = HomeUiState(
            vehicles = listOf(vehicleAt("v1", referencePoint.latitude, referencePoint.longitude)),
            userLocation = referencePoint,
            visibleMapBounds = null,
        )

        val count = state.nearbyVehicleCount

        assertEquals(1, count)
    }

    // --- visibleVehicles: kategori filtresi ---

    private fun oneVehiclePerCategory() = listOf(
        vehicleAt(id = "economic", latitude = referencePoint.latitude, category = VehicleCategory.EKONOMIK),
        vehicleAt(id = "comfort", latitude = referencePoint.latitude, category = VehicleCategory.KONFOR),
        vehicleAt(id = "suv", latitude = referencePoint.latitude, category = VehicleCategory.SUV),
    )

    @Test
    fun `visibleVehicles returns every category when filter is TUMU`() {
        val state = HomeUiState(vehicles = oneVehiclePerCategory(), selectedFilter = CategoryFilter.TUMU)

        val visible = state.visibleVehicles

        assertEquals(3, visible.size)
    }

    @Test
    fun `visibleVehicles returns only EKONOMIK vehicles when filter is EKONOMIK`() {
        val state = HomeUiState(vehicles = oneVehiclePerCategory(), selectedFilter = CategoryFilter.EKONOMIK)

        val visible = state.visibleVehicles

        assertEquals(listOf("economic"), visible.map { it.id })
    }

    @Test
    fun `visibleVehicles returns only KONFOR vehicles when filter is KONFOR`() {
        val state = HomeUiState(vehicles = oneVehiclePerCategory(), selectedFilter = CategoryFilter.KONFOR)

        val visible = state.visibleVehicles

        assertEquals(listOf("comfort"), visible.map { it.id })
    }

    @Test
    fun `visibleVehicles returns only SUV vehicles when filter is SUV`() {
        val state = HomeUiState(vehicles = oneVehiclePerCategory(), selectedFilter = CategoryFilter.SUV)

        val visible = state.visibleVehicles

        assertEquals(listOf("suv"), visible.map { it.id })
    }

    // --- nearestVehicle / nearestVehicleDistanceMeters / nearestVehicleEtaMinutes ---

    @Test
    fun `nearestVehicle returns null when vehicle list is empty`() {
        val state = HomeUiState(vehicles = emptyList(), userLocation = referencePoint)

        assertNull(state.nearestVehicle)
    }

    @Test
    fun `nearestVehicleDistanceMeters returns null when vehicle list is empty`() {
        val state = HomeUiState(vehicles = emptyList(), userLocation = referencePoint)

        assertNull(state.nearestVehicleDistanceMeters)
    }

    @Test
    fun `nearestVehicleEtaMinutes returns null when vehicle list is empty`() {
        val state = HomeUiState(vehicles = emptyList(), userLocation = referencePoint)

        assertNull(state.nearestVehicleEtaMinutes)
    }

    @Test
    fun `nearestVehicle returns the only vehicle when list has a single entry`() {
        val state = HomeUiState(
            vehicles = listOf(vehicleAt("v1", LAT_2000M_NORTH)),
            userLocation = referencePoint,
        )

        val distance = state.nearestVehicleDistanceMeters

        assertEquals("v1", state.nearestVehicle?.id)
        assertEquals(2000.0, distance!!, DISTANCE_DELTA_METERS)
        // 25 km/h varsayimiyla (HomeContract.kt:50-52): 2000m / 416.667 m/dk = 4.8 dk -> 5.
        assertEquals(5, state.nearestVehicleEtaMinutes)
    }

    @Test
    fun `nearestVehicle selects the closest of multiple vehicles`() {
        val near = vehicleAt("near", LAT_1000M_NORTH)
        val mid = vehicleAt("mid", LAT_3000M_NORTH)
        val far = vehicleAt("far", LAT_6000M_NORTH)
        val state = HomeUiState(vehicles = listOf(far, mid, near), userLocation = referencePoint)

        val distance = state.nearestVehicleDistanceMeters

        assertEquals("near", state.nearestVehicle?.id)
        assertEquals(1000.0, distance!!, DISTANCE_DELTA_METERS)
    }

    // --- GeoBounds.contains ---

    private val sampleBounds = GeoBounds(north = 41.1, south = 41.0, east = 29.1, west = 29.0)

    @Test
    fun `GeoBounds contains returns true for a point inside the bounds`() {
        assertTrue(sampleBounds.contains(LatLng(latitude = 41.05, longitude = 29.05)))
    }

    @Test
    fun `GeoBounds contains returns false for a point outside the bounds`() {
        assertFalse(sampleBounds.contains(LatLng(latitude = 41.2, longitude = 29.05)))
    }

    @Test
    fun `GeoBounds contains returns true for a point exactly on the boundary`() {
        // "in south..north" / "in west..east" kapali (inclusive) aralik (HomeContract.kt:38-39).
        assertTrue(sampleBounds.contains(LatLng(latitude = sampleBounds.north, longitude = 29.05)))
    }

    private companion object {
        // referencePoint'ten kuzeye dogru sabit mesafedeki enlemler (great-circle formulu).
        private const val LAT_1000M_NORTH = 41.00899321605919
        private const val LAT_2000M_NORTH = 41.01798643211838
        private const val LAT_3000M_NORTH = 41.02697964817756
        private const val LAT_4000M_NORTH = 41.03597286423675
        private const val LAT_5000M_NORTH = 41.044966080295936
        private const val LAT_5001M_NORTH = 41.044975073512
        private const val LAT_6000M_NORTH = 41.053959296355124

        private const val DISTANCE_DELTA_METERS = 0.01
    }
}
