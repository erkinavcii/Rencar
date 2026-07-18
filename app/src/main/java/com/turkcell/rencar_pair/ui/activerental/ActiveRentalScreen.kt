package com.turkcell.rencar_pair.ui.activerental

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.common.map.GeoPoint
import com.turkcell.rencar_pair.ui.common.map.createCarMarkerBitmap
import com.turkcell.rencar_pair.ui.common.map.RencarMap
import com.turkcell.rencar_pair.ui.common.map.enableLiveLocation
import com.turkcell.rencar_pair.ui.common.map.fitCameraToPoints
import com.turkcell.rencar_pair.ui.theme.BackgroundLight
import com.turkcell.rencar_pair.ui.theme.BackgroundDark
import com.turkcell.rencar_pair.ui.theme.BorderDefaultDark
import com.turkcell.rencar_pair.ui.theme.BorderDefaultLight
import com.turkcell.rencar_pair.ui.theme.ErrorDefault
import com.turkcell.rencar_pair.ui.theme.InfoBackgroundDark
import com.turkcell.rencar_pair.ui.theme.InfoBackgroundLight
import com.turkcell.rencar_pair.ui.theme.InfoIconDark
import com.turkcell.rencar_pair.ui.theme.InfoIconLight
import com.turkcell.rencar_pair.ui.theme.InfoTextDark
import com.turkcell.rencar_pair.ui.theme.InfoTextLight
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.SurfaceElevatedDark
import com.turkcell.rencar_pair.ui.theme.SurfaceElevatedLight
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextPrimaryDark
import com.turkcell.rencar_pair.ui.theme.TextPrimaryLight
import com.turkcell.rencar_pair.ui.theme.TextTertiaryDark
import com.turkcell.rencar_pair.ui.theme.TextTertiaryLight
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.displayS
import com.turkcell.rencar_pair.ui.theme.headingL
import com.turkcell.rencar_pair.ui.theme.statValue
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleM
import kotlinx.coroutines.delay
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
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
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        hasLocationPermission = hasLocationPermission,
        modifier = modifier,
    )
}

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalUiState,
    onIntent: (ActiveRentalIntent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    hasLocationPermission: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val cardColor = if (isDark) SurfaceElevatedDark else SurfaceElevatedLight
    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textTertiary = if (isDark) TextTertiaryDark else TextTertiaryLight
    val borderColor = if (isDark) BorderDefaultDark else BorderDefaultLight
    val infoBg = if (isDark) InfoBackgroundDark else InfoBackgroundLight
    val infoIcon = if (isDark) InfoIconDark else InfoIconLight
    val infoText = if (isDark) InfoTextDark else InfoTextLight

    Scaffold(
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardColor)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = textPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Aktif Yolculuk", style = headingL, color = textPrimary)
                    Text(
                        text = "Süre ve ücret canlı işliyor",
                        style = bodyS,
                        color = textTertiary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                VehicleInfoCard(
                    brand = state.brand,
                    model = state.model,
                    plate = state.plate,
                    planLabel = state.planLabel,
                    cardColor = cardColor,
                    iconBg = backgroundColor,
                    textPrimary = textPrimary,
                    textTertiary = textTertiary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(20.dp)),
                ) {
                    ActiveRentalMapView(
                        modifier = Modifier.fillMaxSize(),
                        hasLocationPermission = hasLocationPermission,
                        vehicleLocation = state.vehicleLocation,
                        // Mesafe artik sunucudan periyodik sorgulanan gercek distanceKm ile
                        // gosteriliyor (bkz. ActiveRentalViewModel.pollActiveRental); bu
                        // callback yalnizca haritadaki mavi noktayi guncelliyor.
                        onLocationUpdate = {},
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isVehicleLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = textPrimary,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = "Araç kilitli",
                            style = displayS,
                            color = textPrimary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            text = "Sürüşe devam etmek için kilidi açınız",
                            style = bodyS,
                            color = textTertiary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardColor)
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "Geçen süre", style = bodyS, color = textTertiary)
                        Text(
                            text = state.elapsedTimeLabel,
                            style = displayS,
                            color = textPrimary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            text = "Başlangıç: ${state.startedAtLabel}",
                            style = bodyS,
                            color = textTertiary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatBlock(
                            label = "Anlık ücret",
                            value = "${"%.2f".format(state.currentCost).replace('.', ',')} ₺",
                            valueColor = Primary,
                            textTertiary = textTertiary,
                            modifier = Modifier.weight(1f),
                        )
                        StatBlock(
                            label = "Mesafe",
                            value = "%.1f km".format(state.distanceKm).replace('.', ','),
                            valueColor = textPrimary,
                            textTertiary = textTertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (state.startFee > 0.0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(infoBg)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = infoIcon,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Anlık ücrete ${"%.0f".format(state.startFee)} ₺ başlangıç ücreti dahildir; " +
                                    "kesin döküm bitirince çıkar.",
                                style = bodyS,
                                color = infoText,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                if (state.isVehicleLocked) {
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
                    OutlinedButton(
                        onClick = { onIntent(ActiveRentalIntent.LockToggleClicked) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(text = "Kilitle / Aç", style = titleM)
                    }
                    Button(
                        onClick = { onIntent(ActiveRentalIntent.EndRentalClicked) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorDefault, contentColor = TextOnPrimary),
                    ) {
                        Text(text = "Kiralamayı Bitir", style = titleM, color = TextOnPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoCard(
    brand: String,
    model: String,
    plate: String,
    planLabel: String,
    cardColor: Color,
    iconBg: Color,
    textPrimary: Color,
    textTertiary: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = textPrimary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = "$brand $model", style = titleL, color = textPrimary)
            val subtitle = if (planLabel.isBlank()) plate else "$plate · $planLabel"
            Text(text = subtitle, style = bodyS, color = textTertiary)
        }
    }
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
    valueColor: Color,
    textTertiary: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = bodyS, color = textTertiary)
        Text(text = value, style = statValue, color = valueColor, modifier = Modifier.padding(top = 4.dp))
    }
}

// Bu ekrana ozel kucuk bir harita bileseni: kiracinin kendi canli konumu (mavi nokta,
// telefon GPS'i) ve aracin backend'den Socket.IO ile gelen canli konumu (ayri bir marker)
// birlikte gosterilir. Kat edilen yolun cizgisi bu adimda cizilmiyor (kapsam disi).
// hasLocationPermission false iken enableLiveLocation hic cagrilmaz (once izin yoktu,
// canli konum izinsiz cagriliyordu).
//
// Kamera aracin konumunu takip eder (telefonun GPS'ini degil): telefon GPS'i (ozellikle
// emulator/masa basi testte) aracin gercek rotasindan tamamen farkli bir yerde olabilir,
// bu durumda CameraMode.TRACKING ile kamera telefona kilitlenince arac marker'i her zaman
// ekran disinda kalip hic gorunmuyordu (hareket etmiyormus gibi algilaniyordu). Telefonun
// mavi noktasi yine gosteriliyor (RenderMode.NORMAL), sadece kamerayi surmuyor.
@Composable
private fun ActiveRentalMapView(
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean,
    vehicleLocation: ActiveRentalLatLng?,
    onLocationUpdate: (Location) -> Unit,
) {
    val context = LocalContext.current
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var vehicleMarker by remember { mutableStateOf<Marker?>(null) }

    DisposableEffect(mapLibreMap, hasLocationPermission) {
        val map = mapLibreMap
        if (map == null || !hasLocationPermission) return@DisposableEffect onDispose {}
        val disposeLocation = enableLiveLocation(
            context = context,
            map = map,
            cameraMode = CameraMode.NONE,
            onLocationUpdate = onLocationUpdate,
        )
        onDispose { disposeLocation() }
    }

    // Arac marker'i her yeni konumda silinip yeniden eklenmez (bu, konumun
    // "isinlanmasina" yol aciyordu); ayni marker referansi tutulup konumu bir
    // onceki noktadan yeniye kademeli olarak (yaklasik 1 sn'de, backend'in
    // gonderim araligiyla uyumlu) kaydirilir. ic_car.xml yandan gorunuslu
    // oldugundan yon (bearing) rotasyonu uygulanmiyor - sabit yonde durur.
    LaunchedEffect(mapLibreMap, vehicleLocation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val location = vehicleLocation ?: return@LaunchedEffect
        val target = MapLibreLatLng(location.latitude, location.longitude)

        val marker = vehicleMarker
        if (marker == null) {
            val bitmap = createCarMarkerBitmap(context, Primary.toArgb())
            vehicleMarker = map.addMarker(
                MarkerOptions()
                    .position(target)
                    .icon(IconFactory.getInstance(context).fromBitmap(bitmap)),
            )
        } else {
            val start = marker.position
            val steps = 24
            repeat(steps) { step ->
                val fraction = (step + 1) / steps.toFloat()
                marker.position = MapLibreLatLng(
                    start.latitude + (target.latitude - start.latitude) * fraction,
                    start.longitude + (target.longitude - start.longitude) * fraction,
                )
                map.updateMarker(marker)
                delay(40L)
            }
        }

        fitCameraToPoints(context, map, listOf(GeoPoint(location.latitude, location.longitude)))
    }

    RencarMap(
        modifier = modifier,
        initialCameraTarget = INITIAL_CAMERA_TARGET,
        onMapReady = { mapLibreMap = it },
    )
}
