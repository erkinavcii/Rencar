package com.turkcell.rencar_pair.ui.tripsummary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.BackgroundDark
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.SuccessBackgroundDark
import com.turkcell.rencar_pair.ui.theme.SuccessStrongDark
import com.turkcell.rencar_pair.ui.theme.SurfaceDark
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextPrimaryDark
import com.turkcell.rencar_pair.ui.theme.TextSecondaryDark
import com.turkcell.rencar_pair.ui.theme.TextTertiaryDark
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.headingXL
import com.turkcell.rencar_pair.ui.theme.priceL
import com.turkcell.rencar_pair.ui.theme.statValue
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleS

@Composable
fun TripSummaryRoute(
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TripSummaryEffect.NavigateHome -> onNavigateHome()
                is TripSummaryEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    TripSummaryScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun TripSummaryScreen(
    state: TripSummaryUiState,
    onIntent: (TripSummaryIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SuccessBackgroundDark),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SuccessStrongDark,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Yolculuk tamamlandı", style = headingXL, color = TextPrimaryDark)
            Text(
                text = "${state.brand} ${state.model} · ${state.plate}",
                style = bodyS,
                color = TextTertiaryDark,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(label = "Süre", value = "${state.durationMinutes} dk", modifier = Modifier.weight(1f))
                StatCard(label = "Mesafe", value = "%.1f km".format(state.distanceKm), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Toplam", style = titleL, color = TextPrimaryDark)
                Text(text = "₺${"%.2f".format(state.totalPrice).replace('.', ',')}", style = priceL, color = TextPrimaryDark)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .clickable { /* Kart degistirme akisi bu adimda kapsam disi */ },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = TextSecondaryDark)
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = if (state.isLoadingCard) "Kart yükleniyor…" else state.cardLabel,
                        style = bodyS,
                        color = TextPrimaryDark,
                    )
                    Text(text = "Kişisel kart", style = bodyS, color = TextTertiaryDark)
                }
                Text(text = "Değiştir", style = titleS, color = Primary)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onIntent(TripSummaryIntent.PayClicked) },
                enabled = !state.isPaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
            ) {
                if (state.isPaying) {
                    CircularProgressIndicator(color = TextOnPrimary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "₺${"%.2f".format(state.totalPrice).replace('.', ',')} Öde",
                        style = titleL,
                        color = TextOnPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(12.dp),
    ) {
        Text(text = label, style = bodyS, color = TextTertiaryDark)
        Text(text = value, style = statValue, color = TextPrimaryDark, modifier = Modifier.padding(top = 4.dp))
    }
}
