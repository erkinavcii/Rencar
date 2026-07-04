package com.turkcell.rencar_pair.ui.wallet

import com.turkcell.rencar_pair.data.wallet.PaymentCard
import com.turkcell.rencar_pair.data.wallet.WalletTransaction

data class WalletUiState(
    val balance: Double = 0.0,
    val cards: List<PaymentCard> = emptyList(),
    val transactions: List<WalletTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val showAddCardDialog: Boolean = false,
    val showLoadBalanceDialog: Boolean = false,
    // Add card inputs
    val cardNoInput: String = "",           // formatted display (with spaces)
    val cardNoRaw: String = "",             // digits only
    val cardExpiryInput: String = "",
    val cardHolderInput: String = "",
    // Load balance inputs
    val loadAmountInput: String = "",
    val selectedCardId: String? = null
)

sealed interface WalletIntent {
    data object LoadWalletData : WalletIntent
    data object RefreshWallet : WalletIntent

    // Add Card intents
    data class CardNoChanged(val value: String) : WalletIntent      // receives raw digits only
    data class CardExpiryChanged(val value: String) : WalletIntent
    data class CardHolderChanged(val value: String) : WalletIntent
    data object ToggleAddCardDialog : WalletIntent
    data object SubmitAddCard : WalletIntent

    // Load Balance intents
    data class LoadAmountChanged(val value: String) : WalletIntent
    data class SelectCardForLoad(val cardId: String) : WalletIntent
    data object ToggleLoadBalanceDialog : WalletIntent
    data object SubmitLoadBalance : WalletIntent
}

sealed interface WalletEffect {
    data class ShowMessage(val message: String) : WalletEffect
    data object NavigateBack : WalletEffect
}
