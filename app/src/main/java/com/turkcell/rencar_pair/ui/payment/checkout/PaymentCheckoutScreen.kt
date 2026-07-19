package com.turkcell.rencar_pair.ui.payment.checkout

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.BackgroundDark
import com.turkcell.rencar_pair.ui.theme.TextPrimaryDark
import com.turkcell.rencar_pair.ui.theme.headingXL

@Composable
fun PaymentCheckoutRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentCheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PaymentCheckoutEffect.PaymentCompleted -> onBack()
                is PaymentCheckoutEffect.PaymentNotCompleted -> {
                    snackbarHostState.showSnackbar(effect.message)
                    onBack()
                }
            }
        }
    }

    PaymentCheckoutScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCheckoutScreen(
    state: PaymentCheckoutUiState,
    onIntent: (PaymentCheckoutIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    // Sistem geri tusu da "Kapat" ile ayni davranisi tetikler: odeme sonucu once sorgulanir,
    // sessizce ekrandan cikilmaz.
    BackHandler { onIntent(PaymentCheckoutIntent.CloseClicked) }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "İyzico ile Öde", style = headingXL, color = TextPrimaryDark) },
                actions = {
                    IconButton(
                        onClick = { onIntent(PaymentCheckoutIntent.CloseClicked) },
                        modifier = Modifier.testTag("payment_checkout_close_button"),
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = TextPrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark),
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val pageUrl = state.paymentPageUrl
            if (pageUrl != null) {
                // factory yalnizca bu AndroidView ilk kompozisyona girdiginde (yani pageUrl
                // ilk kez non-null oldugunda) calisir; token/URL tekil ve degismedigi icin
                // update lambda'sina gerek yok - WebView kendi ic navigasyonunu (3DS/SMS
                // adimlari) serbestce yonetir, biz tekrar loadUrl cagirip sifirlamayiz.
                AndroidView(
                    factory = { context ->
                        val webView = WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                        }
                        // Iyzico'nun 3DS/SMS dogrulama akisi, odeme sayfasi ile bankanin
                        // dogrulama sayfasi (farkli alan adi) arasinda cookie tabanli oturum
                        // eslestirmesi kullaniyor. Third-party cookie'ler varsayilan kapali
                        // gelir; acilmazsa banka girilen kodu "gecersiz" olarak reddeder.
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(webView, true)
                        webView.loadUrl(pageUrl)
                        webView
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (state.isLoading || state.isCheckingResult) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
