package com.turkcell.rencar_pair.ui.payment.checkout

data class PaymentCheckoutUiState(
    val isLoading: Boolean = true, // initialize cagrisi surerken (paymentPageUrl henuz yok)
    val paymentPageUrl: String? = null,
    val isCheckingResult: Boolean = false, // "Kapat"a basildiktan sonra sonuc sorgulanirken
)

sealed interface PaymentCheckoutIntent {
    data object CloseClicked : PaymentCheckoutIntent
}

sealed interface PaymentCheckoutEffect {
    data object PaymentCompleted : PaymentCheckoutEffect
    data class PaymentNotCompleted(val message: String) : PaymentCheckoutEffect
}
