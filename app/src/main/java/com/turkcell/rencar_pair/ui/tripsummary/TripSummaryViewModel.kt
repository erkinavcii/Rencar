package com.turkcell.rencar_pair.ui.tripsummary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.model.PayRentalDto
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
    }

    fun onIntent(intent: TripSummaryIntent) {
        when (intent) {
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

    private fun pay() {
        val state = _uiState.value
        if (state.isPaying) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true) }
            // Gercek odeme: POST /rentals/{id}/pay. Ekranda kart gosterildiginden
            // yontem CARD (default kartin id'si ile); kart yoksa cuzdan bakiyesine
            // dusulur ki odeme yine de tamamlanabilsin (seed cuzdan bakiyesi var).
            val cardId = selectedCardId
            val dto = if (cardId != null) {
                PayRentalDto(method = "CARD", cardId = cardId)
            } else {
                PayRentalDto(method = "WALLET")
            }
            val result = rentalRepository.payRental(state.rentalId, dto)
            _uiState.update { it.copy(isPaying = false) }
            result
                .onSuccess { _uiState.update { current -> current.copy(isPaid = true) } }
                .onFailure { _effect.send(TripSummaryEffect.ShowError(it.message ?: "Ödeme yapılamadı.")) }
        }
    }
}
