package com.turkcell.rencar_pair.ui.tripsummary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

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
        }
    }

    private fun loadDefaultCard() {
        viewModelScope.launch {
            walletRepository.getCards()
                .onSuccess { cards ->
                    val defaultCard = cards.find { it.isDefault } ?: cards.firstOrNull()
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
            val title = "${state.brand} ${state.model} kiralama"
            val result = walletRepository.payFromBalance(state.totalPrice, title)
            _uiState.update { it.copy(isPaying = false) }
            result
                .onSuccess { _effect.send(TripSummaryEffect.NavigateHome) }
                .onFailure { _effect.send(TripSummaryEffect.ShowError(it.message ?: "Ödeme yapılamadı.")) }
        }
    }
}
