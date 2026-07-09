package com.turkcell.rencar_pair.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.BuildConfig
import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.navigation.RencarBottomNavigation
import com.turkcell.rencar_pair.ui.theme.BackgroundLight
import com.turkcell.rencar_pair.ui.theme.BorderDefaultLight
import com.turkcell.rencar_pair.ui.theme.BorderStrongDark
import com.turkcell.rencar_pair.ui.theme.BorderSubtleDark
import com.turkcell.rencar_pair.ui.theme.CategoryEkonomik
import com.turkcell.rencar_pair.ui.theme.CategoryKonfor
import com.turkcell.rencar_pair.ui.theme.CategorySuv
import com.turkcell.rencar_pair.ui.theme.CategoryKullanimdaLight
import com.turkcell.rencar_pair.ui.theme.CategoryKullanımdaDark
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.PrimaryOnDark
import com.turkcell.rencar_pair.ui.theme.SuccessBackgroundDark
import com.turkcell.rencar_pair.ui.theme.SuccessBackgroundLight
import com.turkcell.rencar_pair.ui.theme.SuccessDefault
import com.turkcell.rencar_pair.ui.theme.SuccessStrong
import com.turkcell.rencar_pair.ui.theme.SuccessStrongDark
import com.turkcell.rencar_pair.ui.theme.SurfaceDark
import com.turkcell.rencar_pair.ui.theme.SurfaceElevatedDark
import com.turkcell.rencar_pair.ui.theme.SurfaceLight
import com.turkcell.rencar_pair.ui.theme.TextHintDark
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextPrimaryDark
import com.turkcell.rencar_pair.ui.theme.TextSecondaryDark
import com.turkcell.rencar_pair.ui.theme.TextTertiaryDark
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.headingL
import com.turkcell.rencar_pair.ui.theme.labelM
import com.turkcell.rencar_pair.ui.theme.labelS
import com.turkcell.rencar_pair.ui.theme.labelXS
import com.turkcell.rencar_pair.ui.theme.priceL
import com.turkcell.rencar_pair.ui.theme.statValue
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleXS
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private val KADIKOY_CENTER = MapLibreLatLng(40.9903, 29.0275)
private const val MAP_STYLE_URL = "https://api.maptiler.com/maps/streets-v4/style.json"

@Composable
fun HomeRoute(
    onTabSelected: (NavigationTab) -> Unit,
    onNavigateToReservation: (vehicleId: String, brand: String, model: String, plate: String, pricePerDay: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val isLocationAccuracyHigh = tokenManager.isLocationAccuracyHigh()
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var markerVehicleIds by remember { mutableStateOf<Map<Marker, String>>(emptyMap()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isDarkHome()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onIntent(HomeIntent.LocationPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            viewModel.onIntent(HomeIntent.LocationPermissionResult(true))
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.RequestLocationPermission -> permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
                HomeEffect.CenterOnUserLocation -> {
                    val map = mapLibreMap ?: return@collect
                    val userLoc = uiState.userLocation
                    if (userLoc != null) {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                MapLibreLatLng(userLoc.latitude, userLoc.longitude),
                                15.0,
                            ),
                        )
                    } else {
                        map.locationComponent.setCameraMode(CameraMode.TRACKING)
                        map.locationComponent.zoomWhileTracking(15.0)
                    }
                }
                is HomeEffect.CenterOnLocation -> {
                    val map = mapLibreMap ?: return@collect
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            MapLibreLatLng(effect.location.latitude, effect.location.longitude),
                            16.0,
                        ),
                    )
                }
                is HomeEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(uiState.hasLocationPermission, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (uiState.hasLocationPermission) {
            enableLocationComponent(context, map, isLocationAccuracyHigh) { location ->
                viewModel.onIntent(
                    HomeIntent.UserLocationChanged(LatLng(location.latitude, location.longitude)),
                )
            }
        }
    }

    // İlk açılışta kullanıcı konumuna tek seferlik zoom ve Logcat izi.
    var hasZoomedToUser by remember { mutableStateOf(false) }
    LaunchedEffect(mapLibreMap, uiState.userLocation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val location = uiState.userLocation ?: return@LaunchedEffect
        if (!hasZoomedToUser) {
            hasZoomedToUser = true
            android.util.Log.d("REN_MAP", "İlk zoom -> lat: ${location.latitude}, lng: ${location.longitude}")
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    MapLibreLatLng(location.latitude, location.longitude),
                    14.0,
                ),
            )
        }
    }

    LaunchedEffect(uiState.visibleVehicles, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        markerVehicleIds = renderVehicleMarkers(context, map, uiState.visibleVehicles, isDark)
    }

    LaunchedEffect(uiState.vehicles, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        fitCameraToVehicles(context, map, uiState.vehicles)
    }

    LaunchedEffect(mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.setOnMarkerClickListener { marker ->
            val vehicleId = markerVehicleIds[marker]
            if (vehicleId != null) {
                viewModel.onIntent(HomeIntent.VehicleSelected(vehicleId))
                true
            } else {
                false
            }
        }
    }

    HomeScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onTabSelected = onTabSelected,
        onNavigateToReservation = onNavigateToReservation,
        onMapReady = { mapLibreMap = it },
        onZoomIn = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomIn()) },
        onZoomOut = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomOut()) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    onNavigateToReservation: (vehicleId: String, brand: String, model: String, plate: String, pricePerDay: Int) -> Unit,
    onMapReady: (MapLibreMap) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        bottomBar = {
            RencarBottomNavigation(
                selectedTab = NavigationTab.HARITA,
                onTabSelected = onTabSelected,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            RencarMapView(modifier = Modifier.fillMaxSize(), onMapReady = onMapReady)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 18.dp),
                ) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onIntent(HomeIntent.SearchQueryChanged(it)) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MapZoomControls(
                            onZoomIn = { onZoomIn() },
                            onZoomOut = { onZoomOut() },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LocateMeButton(onClick = { onIntent(HomeIntent.LocateMeClicked) })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NearbyVehiclesCard(
                    nearbyCount = state.visibleVehicles.size,
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = { onIntent(HomeIntent.FilterSelected(it)) },
                    hasLocationPermission = state.hasLocationPermission,
                    nearestVehicleEtaMinutes = state.nearestVehicleEtaMinutes,
                    onFindNearestVehicleClicked = { onIntent(HomeIntent.FindNearestVehicleClicked) },
                )
            }
        }
    }

    state.selectedVehicle?.let { vehicle ->
        VehicleDetailBottomSheet(
            vehicle = vehicle,
            onDismiss = { onIntent(HomeIntent.VehicleDetailDismissed) },
            onReserveClick = {
                onNavigateToReservation(vehicle.id, vehicle.brand, vehicle.model, vehicle.plate, vehicle.pricePerDay)
            },
        )
    }
}

@Composable
private fun isDarkHome(): Boolean =
    MaterialTheme.colorScheme.background != Color(BackgroundLight.value)

// TODO: Gercek API arac telemetri alanlarini (yakit, menzil, vites, koltuk) saglamiyor.
// Backend bu alanlari eklediginde bu sabitler kaldirilip gercek degerlerle degistirilecek.
private const val PLACEHOLDER_FUEL_PERCENT = 72
private const val PLACEHOLDER_RANGE_KM = 480
private const val PLACEHOLDER_TRANSMISSION = "Manuel"
private const val PLACEHOLDER_SEAT_COUNT = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleDetailBottomSheet(
    vehicle: VehicleMarker,
    onDismiss: () -> Unit,
    onReserveClick: () -> Unit,
) {
    val isDark = isDarkHome()
    val sheetColor = if (isDark) SurfaceDark else SurfaceLight
    val cardColor = if (isDark) Color(0xFF1F262F) else Color(0xFFF4F6F9)
    val labelColor = if (isDark) TextTertiaryDark else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = if (isDark) TextPrimaryDark else MaterialTheme.colorScheme.onSurface
    val dividerColor = if (isDark) BorderSubtleDark else MaterialTheme.colorScheme.outlineVariant
    val trackColor = if (isDark) Color(0xFF2C333D) else BorderDefaultLight
    val fuelColor = if (isDark) SuccessStrongDark else SuccessDefault
    val reserveColor = if (isDark) PrimaryOnDark else Primary
    val badgeBg = if (isDark) SuccessBackgroundDark else SuccessBackgroundLight
    val badgeText = if (isDark) SuccessStrongDark else SuccessStrong

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 30.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "${vehicle.brand} ${vehicle.model}", style = headingL, color = valueColor)
                if (!vehicle.inUse) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(text = "MÜSAİT", style = labelXS, color = badgeText)
                    }
                }
            }

            Text(
                text = "${vehicle.plate} · yakınlarda",
                style = bodyS,
                color = labelColor,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            VehiclePhoto(
                vehicle = vehicle,
                cardColor = cardColor,
                placeholderTint = labelColor,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = labelColor,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(text = "Yakıt", style = labelS, color = labelColor)
                    }
                    Text(
                        text = "%$PLACEHOLDER_FUEL_PERCENT",
                        style = statValue,
                        color = valueColor,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .padding(top = 7.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(trackColor),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(PLACEHOLDER_FUEL_PERCENT / 100f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(fuelColor),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = labelColor,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(text = "Menzil", style = labelS, color = labelColor)
                    }
                    Text(
                        text = "~$PLACEHOLDER_RANGE_KM km",
                        style = statValue,
                        color = valueColor,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                    Text(
                        text = "Dolu depo",
                        style = labelS,
                        color = labelColor,
                        modifier = Modifier.padding(top = 9.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardColor)
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = valueColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Column {
                        Text(text = "Vites", style = labelS, color = labelColor)
                        Text(text = PLACEHOLDER_TRANSMISSION, style = titleXS, color = valueColor)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardColor)
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.EventSeat,
                        contentDescription = null,
                        tint = valueColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Column {
                        Text(text = "Koltuk", style = labelS, color = labelColor)
                        Text(text = "$PLACEHOLDER_SEAT_COUNT kişi", style = titleXS, color = valueColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Backend gunluk fiyat (pricePerDay) donduruyor; tasarim dakika/saat bazli
            // gosterim istedigi icin gercek gunluk fiyattan turetiliyor (uydurma sabit
            // degil, gercek pricePerDay'e orantili hesaplama).
            val pricePerHour = vehicle.pricePerDay / 24.0
            val pricePerMinute = vehicle.pricePerDay / 1440.0

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₺${"%.2f".format(pricePerMinute).replace('.', ',')}",
                        style = priceL,
                        color = valueColor,
                    )
                    Text(
                        text = "/dk",
                        style = bodyS,
                        color = labelColor,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp),
                    )
                }
                Text(
                    text = "Saatlik ₺${pricePerHour.toInt()}",
                    style = bodyS,
                    color = labelColor,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor),
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                OutlinedButton(
                    onClick = onReserveClick,
                    modifier = Modifier
                        .width(130.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.7.dp, reserveColor),
                ) {
                    Text(text = "Rezerve Et", style = titleL, color = reserveColor)
                }
                Button(
                    onClick = { /* TODO: gercek kiralama baslatma akisi sonraki adimda */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = TextOnPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Kilidi Aç", style = titleL, color = TextOnPrimary)
                }
            }
        }
    }
}

// Foto ismi konvansiyonu: res/drawable/car_{marka}_{model}.png (kucuk harf, bosluk -> alt cizgi).
// Bu isimde bir kaynak eklenirse otomatik gosterilir; eklenmediyse mevcut ikon placeholder'i kalir.
@Composable
private fun VehiclePhoto(
    vehicle: VehicleMarker,
    cardColor: Color,
    placeholderTint: Color,
) {
    val context = LocalContext.current
    val resId = remember(vehicle.brand, vehicle.model) {
        val resourceName = "car_${vehicle.brand}_${vehicle.model}"
            .lowercase()
            .replace(" ", "_")
        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            .takeIf { it != 0 }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor),
        contentAlignment = Alignment.Center,
    ) {
        if (resId != null) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "${vehicle.brand} ${vehicle.model}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = placeholderTint,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isDarkHome()
    val barColor = if (isDark) SurfaceElevatedDark else SurfaceLight
    val borderColor = if (isDark) BorderStrongDark else Color.Transparent
    val iconColor = if (isDark) TextTertiaryDark else MaterialTheme.colorScheme.onSurfaceVariant
    val hintColor = if (isDark) TextHintDark else MaterialTheme.colorScheme.onSurfaceVariant
    val filterBg = if (isDark) Color(0xFF262E39) else MaterialTheme.colorScheme.surfaceVariant
    val filterIconColor = if (isDark) TextSecondaryDark else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(barColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = iconColor,
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = bodyS.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Nereden araç alacaksın?",
                        style = bodyS,
                        color = hintColor,
                    )
                }
                innerTextField()
            },
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(filterBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filtrele",
                tint = filterIconColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun MapZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isDarkHome()
    val buttonColor = if (isDark) SurfaceElevatedDark else SurfaceLight
    val borderColor = if (isDark) BorderStrongDark else Color.Transparent
    val dividerColor = if (isDark) BorderStrongDark else MaterialTheme.colorScheme.outlineVariant
    val iconTint = if (isDark) TextSecondaryDark else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(buttonColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp)),
    ) {
        IconButton(onClick = onZoomIn, modifier = Modifier.size(46.dp)) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Yakınlaştır",
                tint = iconTint,
            )
        }
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(1.dp)
                .background(dividerColor),
        )
        IconButton(onClick = onZoomOut, modifier = Modifier.size(46.dp)) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Uzaklaştır",
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun LocateMeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = isDarkHome()
    val buttonColor = if (isDark) SurfaceElevatedDark else SurfaceLight
    val borderColor = if (isDark) BorderStrongDark else Color.Transparent
    val iconTint = if (isDark) PrimaryOnDark else Primary

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(buttonColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Konumuma git",
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun NearbyVehiclesCard(
    nearbyCount: Int,
    selectedFilter: CategoryFilter,
    onFilterSelected: (CategoryFilter) -> Unit,
    hasLocationPermission: Boolean,
    nearestVehicleEtaMinutes: Int?,
    onFindNearestVehicleClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isDarkHome()
    val subtitleColor = if (isDark) TextTertiaryDark else MaterialTheme.colorScheme.onSurfaceVariant
    val handleColor = if (isDark) Color(0xFF2C333D) else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(42.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(handleColor),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Text(
                    text = "Yakınında $nearbyCount araç",
                    style = headingL,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when {
                        nearestVehicleEtaMinutes != null -> "En yakın araç ~$nearestVehicleEtaMinutes dk uzaklıkta"
                        hasLocationPermission -> "Konumunuz aranıyor…"
                        else -> "Mesafe için konum izni gerekli"
                    },
                    style = bodyS,
                    color = subtitleColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                label = "Tümü",
                selected = selectedFilter == CategoryFilter.TUMU,
                onClick = { onFilterSelected(CategoryFilter.TUMU) },
            )
            FilterChip(
                label = "Ekonomik",
                dotColor = CategoryEkonomik,
                selected = selectedFilter == CategoryFilter.EKONOMIK,
                onClick = { onFilterSelected(CategoryFilter.EKONOMIK) },
            )
            FilterChip(
                label = "Konfor",
                dotColor = CategoryKonfor,
                selected = selectedFilter == CategoryFilter.KONFOR,
                onClick = { onFilterSelected(CategoryFilter.KONFOR) },
            )
            FilterChip(
                label = "SUV",
                dotColor = CategorySuv,
                selected = selectedFilter == CategoryFilter.SUV,
                onClick = { onFilterSelected(CategoryFilter.SUV) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFindNearestVehicleClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = TextOnPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "En Yakın Aracı Bul", style = titleL, color = TextOnPrimary)
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dotColor: Color? = null,
) {
    val isDark = isDarkHome()
    val inactiveBg = if (isDark) Color(0xFF222A33) else MaterialTheme.colorScheme.surfaceVariant
    val inactiveText = if (isDark) TextSecondaryDark else MaterialTheme.colorScheme.onSurfaceVariant
    val background = if (selected) Primary else inactiveBg
    val contentColor = if (selected) TextOnPrimary else inactiveText

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(dotColor),
            )
        }
        Text(text = label, style = labelM, color = contentColor)
    }
}

@Composable
private fun RencarMapView(
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapLibre.getInstance(context, BuildConfig.MAPTILER_API_KEY, WellKnownTileServer.MapTiler)
        MapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri("$MAP_STYLE_URL?key=${BuildConfig.MAPTILER_API_KEY}")) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(KADIKOY_CENTER, 14.0))
                    onMapReady(map)
                }
            }
            mapView
        },
    )
}

private fun enableLocationComponent(
    context: Context,
    map: MapLibreMap,
    isLocationAccuracyHigh: Boolean,
    onLocationUpdate: (Location) -> Unit,
) {
    val style = map.style ?: return
    val locationComponent = map.locationComponent

    val priority = if (isLocationAccuracyHigh) {
        LocationEngineRequest.PRIORITY_HIGH_ACCURACY
    } else {
        LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    val request = LocationEngineRequest.Builder(1000L)
        .setPriority(priority)
        .setFastestInterval(500L)
        .build()

    if (!locationComponent.isLocationComponentActivated) {
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(context, style)
                .useDefaultLocationEngine(true)
                .locationEngineRequest(request)
                .build(),
        )
    }
    locationComponent.isLocationComponentEnabled = true
    locationComponent.renderMode = RenderMode.NORMAL

    val engine = locationComponent.locationEngine

    // Yeni bir GPS fix'i gelene kadar ekranda sonsuza dek "Konumunuz araniyor..."
    // takili kalmamasi icin, sistemde onbellekte zaten bulunan son bilinen konum
    // (varsa) hemen kullanilir; ardindan canli guncellemeler dinlenmeye devam eder.
    engine?.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult) {
            result.lastLocation?.let(onLocationUpdate)
        }

        override fun onFailure(exception: Exception) = Unit
    })

    // "En yakin araci bul" ve alt karttaki gercek mesafe/sure gosterimi icin canli
    // kullanici konumu gerekiyor; bu callback her yeni GPS guncellemesinde tetiklenir.
    engine?.requestLocationUpdates(
        request,
        object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult) {
                result.lastLocation?.let(onLocationUpdate)
            }

            override fun onFailure(exception: Exception) = Unit
        },
        Looper.getMainLooper(),
    )
}

private const val CAMERA_FIT_PADDING_DP = 56

// Sabit Kadikoy merkezi yalnizca veri gelene kadarki nötr baslangic konumudur (bu sirada
// isLoading spinner haritayi zaten ortuyor); araclar yuklenince kamera hepsini kapsayacak
// sekilde otomatik kaydirilir.
private fun fitCameraToVehicles(context: Context, map: MapLibreMap, vehicles: List<VehicleMarker>) {
    when {
        vehicles.isEmpty() -> Unit
        vehicles.size == 1 -> {
            val point = vehicles.first().position
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(MapLibreLatLng(point.latitude, point.longitude), 14.0),
            )
        }
        else -> {
            val bounds = LatLngBounds.Builder().apply {
                vehicles.forEach { include(MapLibreLatLng(it.position.latitude, it.position.longitude)) }
            }.build()
            val paddingPx = (CAMERA_FIT_PADDING_DP * context.resources.displayMetrics.density).toInt()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
        }
    }
}

private fun renderVehicleMarkers(
    context: Context,
    map: MapLibreMap,
    vehicles: List<VehicleMarker>,
    isDark: Boolean,
): Map<Marker, String> {
    map.markers.toList().forEach { map.removeMarker(it) }
    val iconFactory = IconFactory.getInstance(context)
    val markerToVehicleId = mutableMapOf<Marker, String>()
    vehicles.forEach { vehicle ->
        val color = when {
            vehicle.inUse -> if (isDark) CategoryKullanımdaDark else CategoryKullanimdaLight
            vehicle.category == VehicleCategory.EKONOMIK -> CategoryEkonomik
            vehicle.category == VehicleCategory.KONFOR -> CategoryKonfor
            else -> CategorySuv
        }
        val bitmap = createPriceMarkerBitmap(vehicle.priceLabel, color.toArgb())
        val marker = map.addMarker(
            MarkerOptions()
                .position(MapLibreLatLng(vehicle.position.latitude, vehicle.position.longitude))
                .icon(iconFactory.fromBitmap(bitmap)),
        )
        markerToVehicleId[marker] = vehicle.id
    }
    return markerToVehicleId
}

private fun createPriceMarkerBitmap(label: String, backgroundColor: Int): Bitmap {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 32f
        isFakeBoldText = true
    }
    val paddingH = 24f
    val paddingV = 16f
    val tailHeight = 14f
    val tailWidth = 18f
    val textWidth = textPaint.measureText(label)
    val width = (textWidth + paddingH * 2).toInt().coerceAtLeast(60)
    val bubbleHeight = (textPaint.textSize + paddingV * 2)
    val height = (bubbleHeight + tailHeight).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundColor }

    val bubbleRect = RectF(0f, 0f, width.toFloat(), bubbleHeight)
    canvas.drawRoundRect(bubbleRect, bubbleHeight / 2.5f, bubbleHeight / 2.5f, backgroundPaint)

    val tailPath = android.graphics.Path().apply {
        val centerX = width / 2f
        moveTo(centerX - tailWidth / 2f, bubbleHeight - 2f)
        lineTo(centerX + tailWidth / 2f, bubbleHeight - 2f)
        lineTo(centerX, bubbleHeight + tailHeight)
        close()
    }
    canvas.drawPath(tailPath, backgroundPaint)

    canvas.drawText(
        label,
        paddingH,
        bubbleHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f,
        textPaint,
    )
    return bitmap
}
