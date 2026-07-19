package com.turkcell.rencar_pair.ui.history

import androidx.compose.ui.geometry.Offset
import com.turkcell.rencar_pair.data.history.HistoryTrip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryUiStateTest {

    private fun tripAt(
        id: String,
        vehicleName: String = "Test Vehicle",
        plate: String = "34 TE 000",
        startDateMillis: Long,
    ): HistoryTrip = HistoryTrip(
        id = id,
        vehicleName = vehicleName,
        plate = plate,
        dateLabel = "",
        fullDateLabel = "",
        startDateMillis = startDateMillis,
        durationLabel = "",
        durationMinutes = 0L,
        price = 0.0,
        paymentMethod = null,
        routeStart = Offset.Zero,
        routeEnd = Offset.Zero,
    )

    // Calendar.MONTH 0-indeksli, private monthKey() ile ayni API kullaniliyor.
    private fun epochMillisFor(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }.timeInMillis

    // private monthKey()'in ayni mantikla yeniden uretimi (dogrudan cagrilamiyor).
    private fun expectedMonthKey(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
    }

    // private monthLabel()'in ayni mantikla yeniden uretimi.
    private fun expectedMonthLabel(epochMillis: Long): String =
        SimpleDateFormat("MMMM yyyy", Locale("tr", "TR"))
            .format(Date(epochMillis))
            .replaceFirstChar { it.uppercase() }

    private val januaryTripA = tripAt(
        id = "a",
        vehicleName = "Toyota Corolla",
        plate = "34 ABC 12",
        startDateMillis = epochMillisFor(2026, 0, 15),
    )
    private val januaryTripB = tripAt(
        id = "b",
        vehicleName = "Renault Clio",
        plate = "34 XYZ 99",
        startDateMillis = epochMillisFor(2026, 0, 20),
    )
    private val marchTripC = tripAt(
        id = "c",
        vehicleName = "Fiat Egea",
        plate = "06 DEF 34",
        startDateMillis = epochMillisFor(2026, 2, 5),
    )

    // --- visibleTrips ---

    @Test
    fun `visibleTrips filters by vehicle name case-insensitively`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB, marchTripC), searchQuery = "corolla")

        val visible = state.visibleTrips

        assertEquals(listOf("a"), visible.map { it.id })
    }

    @Test
    fun `visibleTrips filters by plate case-insensitively`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB, marchTripC), searchQuery = "def 34")

        val visible = state.visibleTrips

        assertEquals(listOf("c"), visible.map { it.id })
    }

    @Test
    fun `visibleTrips returns every trip when search query is blank`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB, marchTripC), searchQuery = "")

        val visible = state.visibleTrips

        assertEquals(3, visible.size)
    }

    @Test
    fun `visibleTrips filters by selected month`() {
        val state = HistoryUiState(
            trips = listOf(januaryTripA, januaryTripB, marchTripC),
            selectedMonthFilter = expectedMonthKey(januaryTripA.startDateMillis),
        )

        val visible = state.visibleTrips

        assertEquals(setOf("a", "b"), visible.map { it.id }.toSet())
    }

    @Test
    fun `visibleTrips combines search query and month filter`() {
        val state = HistoryUiState(
            trips = listOf(januaryTripA, januaryTripB, marchTripC),
            searchQuery = "renault",
            selectedMonthFilter = expectedMonthKey(januaryTripA.startDateMillis),
        )

        val visible = state.visibleTrips

        assertEquals(listOf("b"), visible.map { it.id })
    }

    @Test
    fun `visibleTrips sorts newest first by default`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB), selectedSort = HistorySortOption.DATE_DESC)

        val visible = state.visibleTrips

        assertEquals(listOf("b", "a"), visible.map { it.id })
    }

    @Test
    fun `visibleTrips sorts oldest first when DATE_ASC is selected`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB), selectedSort = HistorySortOption.DATE_ASC)

        val visible = state.visibleTrips

        assertEquals(listOf("a", "b"), visible.map { it.id })
    }

    // --- availableMonths ---

    @Test
    fun `availableMonths returns distinct months sorted newest first`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB, marchTripC))

        val months = state.availableMonths

        assertEquals(
            listOf(
                expectedMonthKey(marchTripC.startDateMillis) to expectedMonthLabel(marchTripC.startDateMillis),
                expectedMonthKey(januaryTripA.startDateMillis) to expectedMonthLabel(januaryTripA.startDateMillis),
            ),
            months,
        )
    }

    // --- selectedTrip ---

    @Test
    fun `selectedTrip returns the trip matching selectedTripId`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB), selectedTripId = "b")

        assertEquals("b", state.selectedTrip?.id)
    }

    @Test
    fun `selectedTrip returns null when selectedTripId matches nothing`() {
        val state = HistoryUiState(trips = listOf(januaryTripA, januaryTripB), selectedTripId = "does-not-exist")

        assertNull(state.selectedTrip)
    }

    // --- hasActiveFilter ---

    @Test
    fun `hasActiveFilter is false for the default state`() {
        assertFalse(HistoryUiState().hasActiveFilter)
    }

    @Test
    fun `hasActiveFilter is true when search query is not blank`() {
        assertTrue(HistoryUiState(searchQuery = "corolla").hasActiveFilter)
    }

    @Test
    fun `hasActiveFilter is true when a month filter is selected`() {
        assertTrue(HistoryUiState(selectedMonthFilter = "2026-0").hasActiveFilter)
    }

    @Test
    fun `hasActiveFilter is false when search query is only whitespace`() {
        assertFalse(HistoryUiState(searchQuery = "   ").hasActiveFilter)
    }
}
