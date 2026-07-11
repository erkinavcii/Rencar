package com.turkcell.rencar_pair.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val lifecycleOwner = LocalLifecycleOwner.current

    // ViewModel yalnizca ilk olusturuldugunda init{} ile yukleniyor; sekme
    // gecislerinde saveState/restoreState nedeniyle ayni ViewModel korunabildigi
    // icin (ornegin eski bir hata/bos state ile), her ON_RESUME'da tazeleniyor.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(HistoryIntent.Refresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistoryEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HistoryScreen(
        state             = uiState,
        onIntent          = viewModel::onIntent,
        onTabSelected     = onTabSelected,
        snackbarHostState = snackbarHostState,
        modifier          = modifier,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onIntent: (HistoryIntent) -> Unit,
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
                    val visibleTrips = state.visibleTrips
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
                            Spacer(Modifier.height(16.dp))
                            StatsRow(state = state)
                            Spacer(Modifier.height(16.dp))
                            SearchField(
                                query = state.searchQuery,
                                onQueryChange = { onIntent(HistoryIntent.SearchQueryChanged(it)) },
                            )
                            Spacer(Modifier.height(12.dp))
                            FilterRow(state = state, onIntent = onIntent)
                            Spacer(Modifier.height(18.dp))
                        }

                        // ── Trip cards ────────────────────────────────────────────
                        if (visibleTrips.isEmpty()) {
                            item {
                                Text(
                                    text      = "Aramanızla eşleşen yolculuk bulunamadı.",
                                    style     = bodyM,
                                    color     = TextHintDark,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp),
                                )
                            }
                        } else {
                            items(visibleTrips, key = { it.id }) { trip ->
                                TripCard(
                                    trip = trip,
                                    onClick = { onIntent(HistoryIntent.TripSelected(trip.id)) },
                                )
                                Spacer(Modifier.height(14.dp))
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    state.selectedTrip?.let { trip ->
        TripDetailBottomSheet(
            trip = trip,
            onDismiss = { onIntent(HistoryIntent.TripDetailDismissed) },
        )
    }
}

// ─── Stats Row (Bu Ay / Tüm Zamanlar) ────────────────────────────────────────────

@Composable
private fun StatsRow(state: HistoryUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Bu Ay",
            tripCount = state.monthlyTripCount,
            spending = state.monthlySpending,
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Tüm Zamanlar",
            tripCount = state.totalTripCount,
            spending = state.totalSpending,
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    tripCount: Int,
    spending: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text = label, style = labelS, color = TextTertiaryDark)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$tripCount yolculuk",
            style = titleL.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryDark,
        )
        Text(
            text = "₺${formatAmount(spending)}",
            style = bodyS,
            color = TextTertiaryDark,
        )
    }
}

// ─── Search Field ─────────────────────────────────────────────────────────────

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(width = 1.dp, color = BorderStrongDark, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextTertiaryDark)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = bodyS.copy(color = TextPrimaryDark),
            cursorBrush = SolidColor(TextPrimaryDark),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(text = "Araç adı veya plaka ara…", style = bodyS, color = TextHintDark)
                }
                innerTextField()
            },
        )
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Aramayı temizle",
                tint = TextTertiaryDark,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") },
            )
        }
    }
}

// ─── Filter Row (siralama + ay chip'leri) ────────────────────────────────────────

@Composable
private fun FilterRow(
    state: HistoryUiState,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SortToggleChip(
            selectedSort = state.selectedSort,
            onClick = { onIntent(HistoryIntent.SortToggled) },
        )
        MonthFilterDropdown(
            selectedMonthFilter = state.selectedMonthFilter,
            availableMonths = state.availableMonths,
            onMonthSelected = { onIntent(HistoryIntent.MonthFilterChanged(it)) },
        )
    }
}

@Composable
private fun MonthFilterDropdown(
    selectedMonthFilter: String?,
    availableMonths: List<Pair<String, String>>,
    onMonthSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = availableMonths.find { it.first == selectedMonthFilter }?.second ?: "Tüm Aylar"

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfacePressedDark)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = selectedLabel, style = labelM, color = TextPrimaryDark)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextTertiaryDark,
                modifier = Modifier.size(18.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = SurfaceElevatedDark,
        ) {
            DropdownMenuItem(
                text = { Text(text = "Tüm Aylar", color = TextPrimaryDark) },
                onClick = {
                    onMonthSelected(null)
                    expanded = false
                },
            )
            availableMonths.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(text = label, color = TextPrimaryDark) },
                    onClick = {
                        onMonthSelected(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SortToggleChip(
    selectedSort: HistorySortOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePressedDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Default.SwapVert,
            contentDescription = null,
            tint = PrimaryOnDark,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = if (selectedSort == HistorySortOption.DATE_DESC) "En Yeni" else "En Eski",
            style = labelM,
            color = TextPrimaryDark,
        )
    }
}

@Composable
private fun HistoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) Primary else SurfacePressedDark
    val contentColor = if (selected) TextOnPrimary else TextSecondaryDark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(text = label, style = labelM, color = contentColor)
    }
}

// ─── Trip Card ──────────────────────────────────────────────────────────────────

@Composable
private fun TripCard(trip: HistoryTrip, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
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

// ─── Trip Detail Bottom Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDetailBottomSheet(trip: HistoryTrip, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 30.dp),
        ) {
            Text(
                text = trip.vehicleName,
                style = headingL,
                color = TextPrimaryDark,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = trip.fullDateLabel,
                style = bodyS,
                color = TextTertiaryDark,
            )

            Spacer(Modifier.height(18.dp))

            DetailRow(icon = Icons.Default.ConfirmationNumber, label = "Plaka", value = trip.plate)
            Spacer(Modifier.height(12.dp))
            DetailRow(icon = Icons.Default.Schedule, label = "Süre", value = trip.durationLabel)
            Spacer(Modifier.height(12.dp))
            DetailRow(icon = Icons.Default.CalendarMonth, label = "Tarih", value = trip.fullDateLabel)

            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderSubtleDark),
            )
            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Toplam Ücret", style = bodyS, color = TextTertiaryDark)
                Text(
                    text = "₺${formatAmount(trip.price)}",
                    style = priceL,
                    color = TextPrimaryDark,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfacePressedDark)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextTertiaryDark, modifier = Modifier.size(18.dp))
        Column {
            Text(text = label, style = labelS, color = TextTertiaryDark)
            Text(text = value, style = titleS.copy(fontWeight = FontWeight.SemiBold), color = TextPrimaryDark)
        }
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
