package com.turkcell.rencar_pair.ui.payment.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.model.PayRentalDto
import com.turkcell.rencar_pair.data.payment.IyzicoPaymentEventBus
import com.turkcell.rencar_pair.data.repository.IyzicoRepository
import com.turkcell.rencar_pair.data.repository.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentCheckoutViewModel @Inject constructor(
    private val iyzicoRepository: IyzicoRepository,
    private val rentalRepository: RentalRepository,
    private val iyzicoPaymentEventBus: IyzicoPaymentEventBus,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val rentalId: String = savedStateHandle["rentalId"] ?: ""
    private val totalPrice: Double = savedStateHandle.get<String>("totalPrice")?.toDoubleOrNull() ?: 0.0

    // Checkout form initialize cevabindaki token; sonuc sorgusu bununla yapilir.
    private var token: String? = null

    private val _uiState = MutableStateFlow(PaymentCheckoutUiState())
    val uiState: StateFlow<PaymentCheckoutUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentCheckoutEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        initializeCheckoutForm()
    }

    fun onIntent(intent: PaymentCheckoutIntent) {
        when (intent) {
            PaymentCheckoutIntent.CloseClicked -> checkResult()
        }
    }

    private fun initializeCheckoutForm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = iyzicoRepository.initializeCheckoutForm(
                price = totalPrice,
                basketId = "rental-$rentalId",
                description = "RenCar yolculuk ödemesi",
            )
            result
                .onSuccess { response ->
                    val pageUrl = response.paymentPageUrl
                    if (pageUrl == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.send(PaymentCheckoutEffect.PaymentNotCompleted("Ödeme sayfası alınamadı."))
                        return@launch
                    }
                    token = response.token
                    _uiState.update { it.copy(isLoading = false, paymentPageUrl = pageUrl) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(PaymentCheckoutEffect.PaymentNotCompleted(error.message ?: "Ödeme başlatılamadı."))
                }
        }
    }

    // Kullanici iyzico sayfasinda odemeyi bitirip "Kapat"a bastiginda tek seferlik cagrilir;
    // otomatik polling yok - kullanici kendisi kapatir. Sonuc SUCCESS ise payRental(IYZICO)
    // burada (PaymentCheckoutViewModel'de) tamamlanir ve TripSummary'ye event bus ile haber
    // verilir - NavBackStackEntry'ler arasi SavedStateHandle ile tasima bu projede guvenilir
    // calismadi (bkz. docs/decisions.md).
    private fun checkResult() {
        val currentToken = token
        if (currentToken == null || _uiState.value.isCheckingResult) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingResult = true) }
            val result = iyzicoRepository.getCheckoutFormResult(currentToken)
            result
                .onSuccess { response ->
                    val paymentId = response.paymentId
                    if (response.paymentStatus == "SUCCESS" && paymentId != null) {
                        completePayment(paymentId)
                    } else {
                        _uiState.update { it.copy(isCheckingResult = false) }
                        _effect.send(PaymentCheckoutEffect.PaymentNotCompleted("Ödeme tamamlanmadı."))
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isCheckingResult = false) }
                    _effect.send(PaymentCheckoutEffect.PaymentNotCompleted(error.message ?: "Ödeme durumu sorgulanamadı."))
                }
        }
    }

    private suspend fun completePayment(iyzicoPaymentId: String) {
        val dto = PayRentalDto(method = "IYZICO", iyzicoPaymentId = iyzicoPaymentId)
        val payResult = rentalRepository.payRental(rentalId, dto)
        _uiState.update { it.copy(isCheckingResult = false) }
        payResult
            .onSuccess {
                iyzicoPaymentEventBus.notifyPaymentCompleted(rentalId)
                _effect.send(PaymentCheckoutEffect.PaymentCompleted)
            }
            .onFailure { error ->
                _effect.send(PaymentCheckoutEffect.PaymentNotCompleted(error.message ?: "Ödeme onaylanamadı."))
            }
    }
}
