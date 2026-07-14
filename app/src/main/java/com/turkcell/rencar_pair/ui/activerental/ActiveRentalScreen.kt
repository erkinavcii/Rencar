package com.turkcell.rencar_pair.ui.activerental

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.common.map.GeoPoint
import com.turkcell.rencar_pair.ui.common.map.RencarMap
import com.turkcell.rencar_pair.ui.common.map.enableLiveLocation
import com.turkcell.rencar_pair.ui.theme.BackgroundDark
import com.turkcell.rencar_pair.ui.theme.ErrorDefault
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.SuccessStrongDark
import com.turkcell.rencar_pair.ui.theme.SurfaceDark
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextPrimaryDark
import com.turkcell.rencar_pair.ui.theme.TextTertiaryDark
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.displayS
import com.turkcell.rencar_pair.ui.theme.labelS
import com.turkcell.rencar_pair.ui.theme.statValue
import com.turkcell.rencar_pair.ui.theme.titleL
import kotlinx.coroutines.delay
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.maps.MapLibreMap

// Sabit Kadikoy merkezi, canli konum takibi (CameraMode.TRACKING) ilk GPS fix'i alana
// kadarki notr baslangic konumudur; Ana Harita ekraniyla ayni yaklasim.
private val INITIAL_CAMERA_TARGET = GeoPoint(40.9903, 29.0275)

@Composable
fun ActiveRentalRoute(
    onBack: () -> Unit,
    onNavigateToVehicleCondition: (
        rentalId: String,
        vehicleId: String,
        brand: String,
        model: String,
        plate: String,
        durationSeconds: Long,
        distanceMeters: Double,
    ) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveRentalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Home ekranindaki izin akisiyla ayni: bu ekran acildiginda konum izni yoksa
    // istenir. Onceden bu kontrol hic yoktu, canli konum izinsiz cagriliyordu.
    var hasLocationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        hasLocationPermission = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            hasLocationPermission = true
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
        while (true) {
            delay(1000L)
            viewModel.onIntent(ActiveRentalIntent.Tick)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToVehicleCondition ->
                    onNavigateToVehicleCondition(
                        effect.rentalId,
                        effect.vehicleId,
                        effect.brand,
                        effect.model,
                        effect.plate,
                        effect.durationSeconds,
                        effect.distanceMeters,
                    )
            }
        }
    }

    ActiveRentalScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        hasLocationPermission = hasLocationPermission,
        modifier = modifier,
    )
}

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalUiState,
    onIntent: (ActiveRentalIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    hasLocationPermission: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ActiveRentalMapView(
                modifier = Modifier.fillMaxSize(),
                hasLocationPermission = hasLocationPermission,
                onLocationUpdate = { location ->
                    onIntent(ActiveRentalIntent.LocationUpdated(ActiveRentalLatLng(location.latitude, location.longitude)))
                },
            )

            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(SuccessStrongDark, RoundedCornerShape(50)),
                    ) {
                        Spacer(modifier = Modifier.width(9.dp).height(9.dp))
                    }
                    Text(
                        text = "Kiralama aktif · ${state.brand} ${state.model}",
                        style = labelS,
                        color = TextPrimaryDark,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
            ) {
                if (state.isVehicleLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextPrimaryDark,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                    Text(
                        text = "Araç kilitli",
                        style = displayS,
                        color = TextPrimaryDark,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp),
                    )
                    Text(
                        text = "Sürüşe devam etmek için kilidi açınız",
                        style = bodyS,
                        color = TextTertiaryDark,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onIntent(ActiveRentalIntent.LockToggleClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(text = "Kilidi Aç", style = titleL, color = TextOnPrimary)
                    }
                } else {
                    Text(
                        text = "Geçen süre",
                        style = bodyS,
                        color = TextTertiaryDark,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Text(
                        text = state.elapsedTimeLabel,
                        style = displayS,
                        color = TextPrimaryDark,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "Anlık ücret",
                            value = "₺${"%.2f".format(state.liveCost).replace('.', ',')}",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Mesafe",
                            value = "%.1f km".format(state.distanceKm),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { onIntent(ActiveRentalIntent.LockToggleClicked) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text(text = "Kilitle", style = titleL)
                        }
                        Button(
                            onClick = { onIntent(ActiveRentalIntent.EndRentalClicked) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorDefault, contentColor = TextOnPrimary),
                        ) {
                            Text(text = "Kiralamayı Bitir", style = titleL, color = TextOnPrimary)
                        }
                    }
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
            .background(BackgroundDark)
            .padding(12.dp),
    ) {
        Text(text = label, style = bodyS, color = TextTertiaryDark)
        Text(text = value, style = statValue, color = Primary, modifier = Modifier.padding(top = 4.dp))
    }
}

// Bu ekrana ozel kucuk bir harita bileseni: yalnizca canli konum (mavi nokta) icin;
// arac marker'lari veya kat edilen yolun cizgisi bu adimda cizilmiyor (kapsam disi).
// hasLocationPermission false iken enableLiveLocation hic cagrilmaz (once izin yoktu,
// canli konum izinsiz cagriliyordu).
@Composable
private fun ActiveRentalMapView(
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean,
    onLocationUpdate: (Location) -> Unit,
) {
    val context = LocalContext.current
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    DisposableEffect(mapLibreMap, hasLocationPermission) {
        val map = mapLibreMap
        if (map == null || !hasLocationPermission) return@DisposableEffect onDispose {}
        val disposeLocation = enableLiveLocation(
            context = context,
            map = map,
            cameraMode = CameraMode.TRACKING,
            onLocationUpdate = onLocationUpdate,
        )
        onDispose { disposeLocation() }
    }

    RencarMap(
        modifier = modifier,
        initialCameraTarget = INITIAL_CAMERA_TARGET,
        onMapReady = { mapLibreMap = it },
    )
}
