package com.turkcell.rencar_pair.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        onIntent: (ProfileIntent) -> Unit,
        onTabSelected: (NavigationTab) -> Unit = {},
    ) {
        composeTestRule.setContent {
            var state by remember { mutableStateOf(ProfileUiState()) }
            RencarpairTheme {
                ProfileScreen(
                    state = state,
                    onIntent = { intent ->
                        state = when (intent) {
                            ProfileIntent.SettingsClicked -> state.copy(showSettingsDialog = true)
                            ProfileIntent.SettingsDismissed -> state.copy(showSettingsDialog = false)
                            is ProfileIntent.LocationAccuracyToggled ->
                                state.copy(isLocationAccuracyHigh = intent.isHigh)
                            else -> state
                        }
                        onIntent(intent)
                    },
                    onTabSelected = onTabSelected,
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }
    }

    @Test
    fun cikisYapTiklaninceLogoutTetiklenir() {
        val intents = mutableListOf<ProfileIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("profile_logout_button").performClick()

        assertTrue(intents.contains(ProfileIntent.Logout))
    }

    @Test
    fun duzenleButonuTiklaninceEditProfileTetiklenir() {
        val intents = mutableListOf<ProfileIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("profile_edit_button").performClick()

        assertTrue(intents.contains(ProfileIntent.EditProfile))
    }

    @Test
    fun odemeYontemleriTiklaninceCuzdanSekmesineGecilir() {
        var selectedTab: NavigationTab? = null
        setContent(onIntent = {}, onTabSelected = { selectedTab = it })

        composeTestRule.onNodeWithTag("profile_menu_payment_methods").performClick()

        assertTrue(selectedTab == NavigationTab.CUZDAN)
    }

    @Test
    fun ayarlarAcilirVeYuksekDogrulukSeciminceLocationAccuracyToggledTetiklenir() {
        val intents = mutableListOf<ProfileIntent>()
        setContent(onIntent = { intents += it })

        composeTestRule.onNodeWithTag("profile_menu_settings").performClick()
        composeTestRule.onNodeWithTag("profile_settings_high_accuracy").performClick()

        assertTrue(intents.contains(ProfileIntent.LocationAccuracyToggled(true)))
    }
}
