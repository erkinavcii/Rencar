package com.turkcell.rencar_pair.ui.vehiclecondition

// apiName, POST /rentals/{id}/photos ucunun bekledigi side degeriyle birebir eslesir.
enum class VehicleSide(val label: String, val apiName: String) {
    ON("Ön", "FRONT"),
    ARKA("Arka", "BACK"),
    SOL("Sol", "LEFT"),
    SAG("Sağ", "RIGHT"),
}

enum class VehicleConditionMode {
    BEFORE,
    AFTER,
}

data class VehicleConditionUiState(
    val mode: VehicleConditionMode = VehicleConditionMode.BEFORE,
    val rentalId: String = "",
    val vehicleId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Double = 0.0,
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val checkedSides: Set<VehicleSide> = emptySet(),
    val uploadingSide: VehicleSide? = null,
    val isSubmitting: Boolean = false,
) {
    val totalSides: Int = VehicleSide.entries.size
    val checkedCount: Int get() = checkedSides.size
    val remainingCount: Int get() = totalSides - checkedCount
    val isConfirmEnabled: Boolean
        get() = checkedCount == totalSides && !isSubmitting && uploadingSide == null
}

sealed interface VehicleConditionIntent {
    // Kamera/galeriden secilen gercek gorsel; BEFORE modunda sunucuya yuklenir.
    data class PhotoCaptured(val side: VehicleSide, val bytes: ByteArray) : VehicleConditionIntent
    data object ConfirmClicked : VehicleConditionIntent
    // Ekrandaki ok butonu veya sistem geri tusu; BEFORE modunda PREPARING kiralamayi
    // iptal etmek icin kullanilir (bkz. docs/decisions.md).
    data object BackClicked : VehicleConditionIntent
}

sealed interface VehicleConditionEffect {
    data object NavigateBack : VehicleConditionEffect

    data class NavigateToActiveRental(
        val rentalId: String,
        val vehicleId: String,
        val brand: String,
        val model: String,
        val plate: String,
        val pricePerDay: Double,
    ) : VehicleConditionEffect

    data class NavigateToTripSummary(
        val rentalId: String,
        val brand: String,
        val model: String,
        val plate: String,
        val durationSeconds: Long,
        val distanceMeters: Double,
        val totalPrice: Double,
    ) : VehicleConditionEffect

    data class ShowError(val message: String) : VehicleConditionEffect
}
