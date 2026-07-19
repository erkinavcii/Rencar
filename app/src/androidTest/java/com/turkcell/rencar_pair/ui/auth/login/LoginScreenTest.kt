package com.turkcell.rencar_pair.ui.auth.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(onIntent: (LoginIntent) -> Unit) {
        composeTestRule.setContent {
            var state by remember { mutableStateOf(LoginUiState()) }
            RencarpairTheme {
                LoginScreen(
                    state = state,
                    onIntent = { intent ->
                        if (intent is LoginIntent.PhoneNumberChanged) {
                            state = state.copy(phoneNumber = intent.value)
                        }
                        onIntent(intent)
                    },
                    onBack = {},
                    onNavigateToRegister = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun telefonNumarasiEksikkenSubmitButonuPasiftir() {
        setContent(onIntent = {})

        composeTestRule.onNodeWithTag("login_submit_button").assertIsNotEnabled()
    }

    @Test
    fun onHaneliTelefonGirilinceSubmitAktifOlurVeTiklaninceSubmitIntentiTetiklenir() {
        val intents = mutableListOf<LoginIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("login_phone_input").performTextInput("5320000000")
        composeTestRule.onNodeWithTag("login_submit_button").assertIsEnabled()
        composeTestRule.onNodeWithTag("login_submit_button").performClick()

        assertTrue(intents.contains(LoginIntent.Submit))
    }

    @Test
    fun kayitOlLinkineTiklaninceNavigasyonTetiklenir() {
        var navigated = false
        composeTestRule.setContent {
            RencarpairTheme {
                LoginScreen(
                    state = LoginUiState(),
                    onIntent = {},
                    onBack = {},
                    onNavigateToRegister = { navigated = true },
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        composeTestRule.onNodeWithTag("login_register_link").performTouchInput {
            click(Offset(width * 0.9f, height / 2f))
        }

        assertTrue(navigated)
    }
}
