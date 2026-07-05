package com.turkcell.rencar_pair.ui.activerental

import android.content.Context
import android.location.Location
import android.os.Looper
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.BuildConfig
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
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val MAP_STYLE_URL = "https://api.maptiler.com/maps/streets-v4/style.json"

@Composable
fun ActiveRentalRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveRentalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            viewModel.onIntent(ActiveRentalIntent.Tick)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToTripSummary -> {
                    // Yolculuk Tamamlandi ekrani henuz eklenmedi (siradaki adimda gelecek);
                    // simdilik geri donuyoruz.
                    snackbarHostState.showSnackbar(
                        "Kiralama tamamlandı. Toplam ücret: ₺${"%.2f".format(effect.totalPrice)}",
                    )
                    onBack()
                }
                is ActiveRentalEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ActiveRentalScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalUiState,
    onIntent: (ActiveRentalIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
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
                        Text(text = "Kilitle / Aç", style = titleL)
                    }
                    Button(
                        onClick = { onIntent(ActiveRentalIntent.EndRentalClicked) },
                        enabled = !state.isEnding,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorDefault, contentColor = TextOnPrimary),
                    ) {
                        if (state.isEnding) {
                            CircularProgressIndicator(color = TextOnPrimary, strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
                        } else {
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

// Home ekranindaki RencarMapView/enableLocationComponent ile ayni kuruluma sahip,
// bu ekrana ozel kucuk bir harita bileseni: yalnizca canli konum (mavi nokta) icin;
// arac marker'lari veya kat edilen yolun cizgisi bu adimda cizilmiyor (kapsam disi).
@Composable
private fun ActiveRentalMapView(
    modifier: Modifier = Modifier,
    onLocationUpdate: (Location) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapLibre.getInstance(context, BuildConfig.MAPTILER_API_KEY, WellKnownTileServer.MapTiler)
        MapView(context)
    }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

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

    LaunchedEffect(mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        enableLiveLocation(context, map, onLocationUpdate)
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri("$MAP_STYLE_URL?key=${BuildConfig.MAPTILER_API_KEY}")) {
                    mapLibreMap = map
                }
            }
            mapView
        },
    )
}

private fun enableLiveLocation(context: Context, map: MapLibreMap, onLocationUpdate: (Location) -> Unit) {
    val style = map.style ?: return
    val locationComponent = map.locationComponent
    val request = LocationEngineRequest.Builder(1000L)
        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
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
    locationComponent.setCameraMode(CameraMode.TRACKING)
    locationComponent.zoomWhileTracking(16.0)

    val engine = locationComponent.locationEngine
    val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult) {
            result.lastLocation?.let(onLocationUpdate)
        }

        override fun onFailure(exception: Exception) = Unit
    }
    engine?.getLastLocation(callback)
    engine?.requestLocationUpdates(request, callback, Looper.getMainLooper())
}
