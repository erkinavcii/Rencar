package com.turkcell.rencar_pair.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _effect = Channel<WalletEffect>(Channel.BUFFERED)
    val effect: Flow<WalletEffect> = _effect.receiveAsFlow()

    init {
        onIntent(WalletIntent.LoadWalletData)
    }

    fun onIntent(intent: WalletIntent) {
        when (intent) {
            is WalletIntent.LoadWalletData -> loadAllData()
            is WalletIntent.RefreshWallet -> loadAllData()
            is WalletIntent.CardNoChanged -> {
                val raw = intent.value.filter { it.isDigit() }.take(16)
                val formatted = raw.chunked(4).joinToString(" ")
                _uiState.update { it.copy(cardNoRaw = raw, cardNoInput = formatted) }
            }
            is WalletIntent.CardExpiryChanged -> _uiState.update { it.copy(cardExpiryInput = intent.value) }
            is WalletIntent.CardHolderChanged -> _uiState.update { it.copy(cardHolderInput = intent.value) }
            is WalletIntent.ToggleAddCardDialog -> _uiState.update {
                it.copy(
                    showAddCardDialog = !it.showAddCardDialog,
                    cardNoInput = "",
                    cardNoRaw = "",
                    cardExpiryInput = "",
                    cardHolderInput = ""
                )
            }
            is WalletIntent.SubmitAddCard -> submitAddCard()
            is WalletIntent.LoadAmountChanged -> _uiState.update { it.copy(loadAmountInput = intent.value) }
            is WalletIntent.SelectCardForLoad -> _uiState.update { it.copy(selectedCardId = intent.cardId) }
            is WalletIntent.ToggleLoadBalanceDialog -> _uiState.update {
                val firstCardId = it.cards.firstOrNull()?.id
                it.copy(
                    showLoadBalanceDialog = !it.showLoadBalanceDialog,
                    loadAmountInput = "",
                    selectedCardId = firstCardId
                )
            }
            is WalletIntent.SubmitLoadBalance -> submitLoadBalance()
        }
    }

    private fun loadAllData() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val balanceRes = walletRepository.getBalance()
            val cardsRes = walletRepository.getCards()
            val txRes = walletRepository.getTransactions()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    balance = balanceRes.getOrDefault(state.balance),
                    cards = cardsRes.getOrDefault(state.cards),
                    transactions = txRes.getOrDefault(state.transactions),
                    selectedCardId = cardsRes.getOrNull()?.firstOrNull()?.id ?: state.selectedCardId
                )
            }
        }
    }

    private fun submitAddCard() {
        val state = _uiState.value
        if (state.cardNoInput.isBlank() || state.cardExpiryInput.isBlank()) {
            viewModelScope.launch {
                _effect.send(WalletEffect.ShowMessage("Lütfen tüm alanları doldurun."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            walletRepository.addCard(state.cardNoInput, state.cardExpiryInput)
                .onSuccess {
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            showAddCardDialog = false,
                            cardNoInput = "",
                            cardExpiryInput = ""
                        )
                    }
                    _effect.send(WalletEffect.ShowMessage("Kartınız başarıyla eklendi."))
                    loadAllData()
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(WalletEffect.ShowMessage(it.message ?: "Kart eklenemedi."))
                }
        }
    }

    private fun submitLoadBalance() {
        val state = _uiState.value
        val amount = state.loadAmountInput.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            viewModelScope.launch {
                _effect.send(WalletEffect.ShowMessage("Lütfen geçerli bir tutar girin."))
            }
            return
        }

        val cardId = state.selectedCardId
        if (cardId == null) {
            viewModelScope.launch {
                _effect.send(WalletEffect.ShowMessage("Lütfen bakiye yüklenecek kartı seçin."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            walletRepository.loadBalance(amount, cardId)
                .onSuccess { newBalance ->
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            balance = newBalance,
                            showLoadBalanceDialog = false,
                            loadAmountInput = ""
                        )
                    }
                    _effect.send(WalletEffect.ShowMessage("Bakiye başarıyla yüklendi."))
                    loadAllData()
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(WalletEffect.ShowMessage(it.message ?: "Bakiye yükleme başarısız."))
                }
        }
    }
}
