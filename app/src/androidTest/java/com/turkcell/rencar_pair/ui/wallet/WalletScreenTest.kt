package com.turkcell.rencar_pair.ui.wallet

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalletScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(onIntent: (WalletIntent) -> Unit) {
        composeTestRule.setContent {
            var state by remember { mutableStateOf(WalletUiState()) }
            RencarpairTheme {
                WalletScreen(
                    state = state,
                    onIntent = { intent ->
                        state = when (intent) {
                            WalletIntent.ToggleAddCardDialog ->
                                state.copy(showAddCardDialog = !state.showAddCardDialog)
                            WalletIntent.ToggleLoadBalanceDialog ->
                                state.copy(showLoadBalanceDialog = !state.showLoadBalanceDialog)
                            is WalletIntent.CardNoChanged -> state.copy(cardNoInput = intent.value)
                            is WalletIntent.CardExpiryChanged -> state.copy(cardExpiryInput = intent.value)
                            is WalletIntent.CardHolderChanged -> state.copy(cardHolderInput = intent.value)
                            is WalletIntent.LoadAmountChanged -> state.copy(loadAmountInput = intent.value)
                            else -> state
                        }
                        onIntent(intent)
                    },
                    onTabSelected = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun ekleLinkineTiklaninceKartEklemeSheetiAcilir() {
        val intents = mutableListOf<WalletIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("wallet_add_card_link").performClick()

        assertTrue(intents.contains(WalletIntent.ToggleAddCardDialog))
        composeTestRule.onNodeWithTag("wallet_card_no_input").assertExists()
    }

    @Test
    fun kartBilgileriDolduruluncaKaydetTiklaninceSubmitAddCardTetiklenir() {
        val intents = mutableListOf<WalletIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("wallet_add_card_link").performClick()
        composeTestRule.onNodeWithTag("wallet_card_no_input").performTextInput("4111111111111111")
        composeTestRule.onNodeWithTag("wallet_card_expiry_input").performTextInput("12/28")
        composeTestRule.onNodeWithTag("wallet_card_holder_input").performTextInput("Ahmet Yılmaz")
        composeTestRule.onNodeWithTag("wallet_submit_add_card_button").performClick()

        assertTrue(intents.contains(WalletIntent.SubmitAddCard))
    }

    @Test
    fun bakiyeYukleTiklaninceLoadBalanceDialogAcilirVeTutarGirilebilir() {
        val intents = mutableListOf<WalletIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("wallet_load_balance_button").performClick()

        assertTrue(intents.contains(WalletIntent.ToggleLoadBalanceDialog))
        composeTestRule.onNodeWithTag("wallet_load_amount_input").performTextInput("200")
        composeTestRule.onNodeWithTag("wallet_submit_load_balance_button").performClick()

        assertTrue(intents.contains(WalletIntent.SubmitLoadBalance))
    }
}
