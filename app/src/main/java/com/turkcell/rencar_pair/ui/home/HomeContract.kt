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
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Int = 0,
)

data class HomeUiState(
    val vehicles: List<VehicleMarker> = emptyList(),
    val selectedFilter: CategoryFilter = CategoryFilter.TUMU,
    val hasLocationPermission: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedVehicleId: String? = null,
) {
    val visibleVehicles: List<VehicleMarker>
        get() = if (selectedFilter == CategoryFilter.TUMU) {
            vehicles
        } else {
            vehicles.filter { it.category.name == selectedFilter.name }
        }

    val selectedVehicle: VehicleMarker?
        get() = vehicles.find { it.id == selectedVehicleId }
}

sealed interface HomeIntent {
    data class FilterSelected(val filter: CategoryFilter) : HomeIntent
    data class LocationPermissionResult(val granted: Boolean) : HomeIntent
    data class SearchQueryChanged(val query: String) : HomeIntent
    data class VehicleSelected(val vehicleId: String) : HomeIntent
    data object VehicleDetailDismissed : HomeIntent
    data object LocateMeClicked : HomeIntent
}

sealed interface HomeEffect {
    data object RequestLocationPermission : HomeEffect
    data object CenterOnUserLocation : HomeEffect
    data class ShowError(val message: String) : HomeEffect
}
