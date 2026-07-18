package com.turkcell.rencar_pair.data.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

// PaymentCheckoutViewModel odemeyi (payRental IYZICO) tamamladiginda TripSummaryViewModel'e
// haber vermek icin kullanilir. NavBackStackEntry'ler arasi SavedStateHandle ile sonuc tasima
// bu projede guvenilir calismadi (bkz. docs/decisions.md); Hilt singleton + SharedFlow, iki
// ViewModel'in ayni instance'i paylastigindan emin oldugumuz basit bir alternatif.
@Singleton
class IyzicoPaymentEventBus @Inject constructor() {
    private val _paymentCompleted = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val paymentCompleted: SharedFlow<String> = _paymentCompleted.asSharedFlow()

    suspend fun notifyPaymentCompleted(rentalId: String) {
        _paymentCompleted.emit(rentalId)
    }
}
