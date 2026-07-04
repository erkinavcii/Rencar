package com.turkcell.rencar_pair.ui.home

data class LatLng(
    val latitude: Double,
    val longitude: Double,
)

enum class VehicleCategory {
    EKONOMIK,
    KONFOR,
    SUV,
}

enum class CategoryFilter {
    TUMU,
    EKONOMIK,
    KONFOR,
    SUV,
}

data class VehicleMarker(
    val id: String,
    val position: LatLng,
    val priceLabel: String,
    val category: VehicleCategory,
    val inUse: Boolean = false,
)

data class HomeUiState(
    val vehicles: List<VehicleMarker> = emptyList(),
    val selectedFilter: CategoryFilter = CategoryFilter.TUMU,
    val hasLocationPermission: Boolean = false,
) {
    val visibleVehicles: List<VehicleMarker>
        get() = if (selectedFilter == CategoryFilter.TUMU) {
            vehicles
        } else {
            vehicles.filter { it.category.name == selectedFilter.name }
        }
}

sealed interface HomeIntent {
    data class FilterSelected(val filter: CategoryFilter) : HomeIntent
    data class LocationPermissionResult(val granted: Boolean) : HomeIntent
    data object LocateMeClicked : HomeIntent
    data object Logout : HomeIntent
}

sealed interface HomeEffect {
    data object NavigateToOnboarding : HomeEffect
    data object RequestLocationPermission : HomeEffect
    data object CenterOnUserLocation : HomeEffect
}
