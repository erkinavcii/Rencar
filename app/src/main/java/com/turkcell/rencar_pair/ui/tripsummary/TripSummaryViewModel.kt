package com.turkcell.rencar_pair.ui.tripsummary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.model.PayRentalDto
import com.turkcell.rencar_pair.data.payment.IyzicoPaymentEventBus
import com.turkcell.rencar_pair.data.repository.RentalRepository
import com.turkcell.rencar_pair.data.wallet.WalletRepository
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
class TripSummaryViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val rentalRepository: RentalRepository,
    private val iyzicoPaymentEventBus: IyzicoPaymentEventBus,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Ekranda gosterilen default kartin id'si; odeme PayRentalDto'da bunu tasir.
    private var selectedCardId: String? = null

    private val _uiState = MutableStateFlow(
        TripSummaryUiState(
            rentalId = savedStateHandle["rentalId"] ?: "",
            brand = savedStateHandle["brand"] ?: "",
            model = savedStateHandle["model"] ?: "",
            plate = savedStateHandle["plate"] ?: "",
            durationSeconds = savedStateHandle.get<String>("durationSeconds")?.toLongOrNull() ?: 0L,
            distanceMeters = savedStateHandle.get<String>("distanceMeters")?.toDoubleOrNull() ?: 0.0,
            totalPrice = savedStateHandle.get<String>("totalPrice")?.toDoubleOrNull() ?: 0.0,
        ),
    )
    val uiState: StateFlow<TripSummaryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<TripSummaryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadDefaultCard()
        observePaymentCompletedEvents()
    }

    fun onIntent(intent: TripSummaryIntent) {
        when (intent) {
            is TripSummaryIntent.PaymentMethodSelected ->
                _uiState.update { it.copy(selectedPaymentMethod = intent.method) }
            TripSummaryIntent.PayClicked -> pay()
            TripSummaryIntent.DoneClicked ->
                viewModelScope.launch { _effect.send(TripSummaryEffect.NavigateHome) }
        }
    }

    private fun loadDefaultCard() {
        viewModelScope.launch {
            walletRepository.getCards()
                .onSuccess { cards ->
                    val defaultCard = cards.find { it.isDefault } ?: cards.firstOrNull()
                    selectedCardId = defaultCard?.id
                    _uiState.update {
                        it.copy(
                            isLoadingCard = false,
                            cardLabel = defaultCard?.let { card -> "${card.type} ${card.number}" } ?: "",
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoadingCard = false) } }
        }
    }

    // PaymentCheckout ekrani, payRental(IYZICO)'yu kendisi tamamlayip bu bus'a haber verir
    // (bkz. PaymentCheckoutViewModel.completePayment). NavBackStackEntry'ler arasi
    // SavedStateHandle ile sonuc tasima bu projede guvenilir calismadi (bkz. docs/decisions.md).
    private fun observePaymentCompletedEvents() {
        viewModelScope.launch {
            iyzicoPaymentEventBus.paymentCompleted.collect { rentalId ->
                if (rentalId == _uiState.value.rentalId) {
                    _uiState.update { it.copy(isPaid = true) }
                }
            }
        }
    }

    private fun pay() {
        val state = _uiState.value
        if (state.isPaying) return
        when (state.selectedPaymentMethod) {
            PaymentMethod.IYZICO ->
                viewModelScope.launch {
                    _effect.send(TripSummaryEffect.NavigateToIyzicoCheckout(state.rentalId, state.totalPrice))
                }
            PaymentMethod.CARD -> payWithCardOrWallet(cardId = selectedCardId, requireCard = true)
            PaymentMethod.WALLET -> payWithCardOrWallet(cardId = null, requireCard = false)
        }
    }

    private fun payWithCardOrWallet(cardId: String?, requireCard: Boolean) {
        viewModelScope.launch {
            if (requireCard && cardId == null) {
                _effect.send(TripSummaryEffect.ShowError("Kayıtlı kart bulunamadı."))
                return@launch
            }
            _uiState.update { it.copy(isPaying = true) }
            val dto = if (cardId != null) {
                PayRentalDto(method = "CARD", cardId = cardId)
            } else {
                PayRentalDto(method = "WALLET")
            }
            val result = rentalRepository.payRental(_uiState.value.rentalId, dto)
            _uiState.update { it.copy(isPaying = false) }
            result
                .onSuccess { _uiState.update { current -> current.copy(isPaid = true) } }
                .onFailure { error -> _effect.send(TripSummaryEffect.ShowError(error.message ?: "Ödeme yapılamadı.")) }
        }
    }
}
