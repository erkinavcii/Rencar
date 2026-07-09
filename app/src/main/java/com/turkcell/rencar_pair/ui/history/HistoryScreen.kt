package com.turkcell.rencar_pair.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.data.history.HistoryTrip
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.navigation.RencarBottomNavigation
import com.turkcell.rencar_pair.ui.theme.*
import java.util.Locale

// ─── Route ────────────────────────────────────────────────────────────────────

@Composable
fun HistoryRoute(
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistoryEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HistoryScreen(
        state             = uiState,
        onTabSelected     = onTabSelected,
        snackbarHostState = snackbarHostState,
        modifier          = modifier,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onTabSelected: (NavigationTab) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            RencarBottomNavigation(
                selectedTab   = NavigationTab.GECMIS,
                onTabSelected = onTabSelected,
            )
        },
        containerColor = BackgroundDark,
        modifier = modifier,
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading && state.trips.isEmpty() -> {
                    CircularProgressIndicator(
                        color = PrimaryOnDark,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                state.trips.isEmpty() -> {
                    Text(
                        text      = "Henüz kiralama geçmişiniz yok.",
                        style     = bodyM,
                        color     = TextHintDark,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        // ── Header ────────────────────────────────────────────────
                        item {
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text  = "Kiralamalarım",
                                style = headingXL.copy(
                                    fontSize   = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                                color = TextPrimaryDark,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text  = "Bu ay ${state.monthlyTripCount} yolculuk · " +
                                        "₺${formatAmount(state.monthlySpending)} harcama",
                                style = bodyM,
                                color = TextTertiaryDark,
                            )
                            Spacer(Modifier.height(22.dp))
                        }

                        // ── Trip cards ────────────────────────────────────────────
                        items(state.trips, key = { it.id }) { trip ->
                            TripCard(trip)
                            Spacer(Modifier.height(14.dp))
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

// ─── Trip Card ──────────────────────────────────────────────────────────────────

@Composable
private fun TripCard(trip: HistoryTrip) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .padding(14.dp),
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RouteThumbnail(start = trip.routeStart, end = trip.routeEnd)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = trip.vehicleName,
                style = titleL.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryDark,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = trip.dateLabel,
                style = bodyS,
                color = TextHintDark,
            )
            Spacer(Modifier.height(10.dp))
            InfoChip(trip.durationLabel)
        }

        Text(
            text  = "₺${formatAmount(trip.price)}",
            style = titleL.copy(fontWeight = FontWeight.ExtraBold),
            color = TextPrimaryDark,
        )
    }
}

// ─── Info Chip (süre pili) ───────────────────────────────────────

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfacePressedDark)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text  = text,
            style = labelS.copy(fontWeight = FontWeight.Bold),
            color = TextSecondaryDark,
        )
    }
}

// ─── Route Thumbnail (mini rota önizlemesi) ─────────────────────────────────────

@Composable
private fun RouteThumbnail(start: Offset, end: Offset) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundDark),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gridColor = Color.White.copy(alpha = 0.07f)

            // Zayıf (faint) harita ızgarası
            val cols = 3
            val rows = 3
            for (i in 1 until cols) {
                val x = w / cols * i
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1.dp.toPx())
            }
            for (i in 1 until rows) {
                val y = h / rows * i
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }

            val startPoint = Offset(start.x * w, start.y * h)
            val endPoint   = Offset(end.x * w, end.y * h)

            // Rota çizgisi
            drawLine(
                color       = PrimaryOnDark,
                start       = startPoint,
                end         = endPoint,
                strokeWidth = 2.5.dp.toPx(),
                cap         = StrokeCap.Round,
            )

            // Başlangıç noktası (mavi)
            drawCircle(color = PrimaryOnDark, radius = 4.dp.toPx(), center = startPoint)
            // Bitiş noktası (yeşil)
            drawCircle(color = SuccessStrongDark, radius = 4.dp.toPx(), center = endPoint)
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatAmount(amount: Double): String =
    String.format(Locale("tr", "TR"), "%,.2f", amount)