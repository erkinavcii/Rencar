package com.turkcell.rencar_pair.ui.history

import com.turkcell.rencar_pair.data.history.HistoryTrip
import java.util.Calendar
import java.util.Locale

enum class HistorySortOption {
    DATE_DESC,
    DATE_ASC,
}

private val monthLabelFormat = java.text.SimpleDateFormat("MMMM yyyy", Locale("tr", "TR"))

private fun HistoryTrip.monthKey(): String {
    val cal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
    return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
}

private fun HistoryTrip.monthLabel(): String =
    monthLabelFormat.format(startDateMillis).replaceFirstChar { it.uppercase() }

data class HistoryUiState(
    val isLoading: Boolean = false,
    val monthlyTripCount: Int = 0,
    val monthlySpending: Double = 0.0,
    val totalTripCount: Int = 0,
    val totalSpending: Double = 0.0,
    val trips: List<HistoryTrip> = emptyList(),
    val selectedSort: HistorySortOption = HistorySortOption.DATE_DESC,
    val searchQuery: String = "",
    val selectedMonthFilter: String? = null, // null = tüm aylar
    val selectedTripId: String? = null,
) {
    /** Ayları en yeniden en eskiye, tekrarsız listeler (filtre chip'leri için). */
    val availableMonths: List<Pair<String, String>>
        get() = trips
            .distinctBy { it.monthKey() }
            .sortedByDescending { it.startDateMillis }
            .map { it.monthKey() to it.monthLabel() }

    /** Arama + ay filtresi + sıralama uygulanmış, ekranda gösterilecek nihai liste. */
    val visibleTrips: List<HistoryTrip>
        get() {
            val query = searchQuery.trim().lowercase(Locale("tr", "TR"))
            val filtered = trips.filter { trip ->
                val matchesQuery = query.isBlank() ||
                    trip.vehicleName.lowercase(Locale("tr", "TR")).contains(query) ||
                    trip.plate.lowercase(Locale("tr", "TR")).contains(query)
                val matchesMonth = selectedMonthFilter == null || trip.monthKey() == selectedMonthFilter
                matchesQuery && matchesMonth
            }
            return when (selectedSort) {
                HistorySortOption.DATE_DESC -> filtered.sortedByDescending { it.startDateMillis }
                HistorySortOption.DATE_ASC -> filtered.sortedBy { it.startDateMillis }
            }
        }

    val selectedTrip: HistoryTrip?
        get() = trips.find { it.id == selectedTripId }

    val hasActiveFilter: Boolean
        get() = searchQuery.isNotBlank() || selectedMonthFilter != null
}

sealed interface HistoryIntent {
    data object LoadHistory : HistoryIntent
    data object Refresh : HistoryIntent
    data object SortToggled : HistoryIntent
    data class SearchQueryChanged(val query: String) : HistoryIntent
    data class MonthFilterChanged(val monthKey: String?) : HistoryIntent
    data class TripSelected(val tripId: String) : HistoryIntent
    data object TripDetailDismissed : HistoryIntent
}

sealed interface HistoryEffect {
    data class ShowMessage(val message: String) : HistoryEffect
}
