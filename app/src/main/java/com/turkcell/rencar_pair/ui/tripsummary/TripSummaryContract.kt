package com.turkcell.rencar_pair.ui.tripsummary

enum class PaymentMethod { WALLET, CARD, IYZICO }

data class TripSummaryUiState(
    val rentalId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val totalPrice: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.WALLET,
    val cardLabel: String = "",
    val isLoadingCard: Boolean = true,
    val isPaying: Boolean = false,
    // Odeme basarili olunca hemen Home'a gecmek yerine, ayni ekranda "Odeme
    // Onaylandi" ozet gorunumune gecilir; kullanici "Ana Sayfaya Don" ile
    // NavigateHome'u kendisi tetikler.
    val isPaid: Boolean = false,
) {
    val durationMinutes: Long get() = durationSeconds / 60
    val distanceKm: Double get() = distanceMeters / 1000.0
}

sealed interface TripSummaryIntent {
    data class PaymentMethodSelected(val method: PaymentMethod) : TripSummaryIntent
    data object PayClicked : TripSummaryIntent
    data object DoneClicked : TripSummaryIntent
}

sealed interface TripSummaryEffect {
    data class NavigateToIyzicoCheckout(val rentalId: String, val totalPrice: Double) : TripSummaryEffect
    data object NavigateHome : TripSummaryEffect
    data class ShowError(val message: String) : TripSummaryEffect
}
