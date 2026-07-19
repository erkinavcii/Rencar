package com.turkcell.rencar_pair.ui.auth.otp

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
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
class OtpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        initialState: OtpUiState = OtpUiState(phone = "5320000000"),
        onIntent: (OtpIntent) -> Unit,
    ) {
        composeTestRule.setContent {
            var state by remember { mutableStateOf(initialState) }
            RencarpairTheme {
                OtpScreen(
                    state = state,
                    onIntent = { intent ->
                        if (intent is OtpIntent.OtpCodeChanged) {
                            state = state.copy(otpCode = intent.value)
                        }
                        onIntent(intent)
                    },
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun kodBoskenSubmitButonuPasiftir() {
        setContent(onIntent = {})

        composeTestRule.onNodeWithTag("otp_submit_button").assertIsNotEnabled()
    }

    @Test
    fun altiHaneliKodGirilinceSubmitAktifOlurVeTiklaninceSubmitIntentiTetiklenir() {
        val intents = mutableListOf<OtpIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("otp_code_input").performTextInput("482913")
        composeTestRule.onNodeWithTag("otp_submit_button").assertIsEnabled()
        composeTestRule.onNodeWithTag("otp_submit_button").performClick()

        assertTrue(intents.contains(OtpIntent.Submit))
    }

    @Test
    fun sureDolduncaYenidenGonderButonuTiklaniyorVeResendCodeTetiklenir() {
        val intents = mutableListOf<OtpIntent>()
        setContent(
            initialState = OtpUiState(phone = "5320000000", timerSeconds = 0),
            onIntent = { intents += it },
        )

        composeTestRule.onNodeWithTag("otp_resend_button").performClick()

        assertTrue(intents.contains(OtpIntent.ResendCode))
    }

    @Test
    fun degistirLinkineTiklaninceChangePhoneTetiklenir() {
        val intents = mutableListOf<OtpIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("otp_change_phone_button").performTouchInput {
            click(Offset(width * 0.9f, height / 2f))
        }

        assertTrue(intents.contains(OtpIntent.ChangePhone))
    }
}
