package com.turkcell.rencar_pair

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.turkcell.rencar_pair.data.session.SessionManager
import com.turkcell.rencar_pair.ui.navigation.RencarNavHost
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RencarpairTheme {
                RencarNavHost(
                    sessionManager = sessionManager,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
