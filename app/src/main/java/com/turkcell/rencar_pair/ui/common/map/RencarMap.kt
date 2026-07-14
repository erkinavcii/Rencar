package com.turkcell.rencar_pair.ui.common.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.turkcell.rencar_pair.BuildConfig
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

const val MAP_STYLE_URL = "https://api.maptiler.com/maps/streets-v4/style.json"

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

/**
 * Ortak MapLibre kurulum bileseni. Hangi ekranda kullanildigini veya veri
 * kaynagini (tek seferlik REST listesi mi, canli akis mi) bilmez; yalnizca
 * haritayi kurar ve hazir oldugunda [onMapReady] ile geri verir.
 */
@Composable
fun RencarMap(
    modifier: Modifier = Modifier,
    initialCameraTarget: GeoPoint,
    initialCameraZoom: Double = 14.0,
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
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            MapLibreLatLng(initialCameraTarget.latitude, initialCameraTarget.longitude),
                            initialCameraZoom,
                        ),
                    )
                    onMapReady(map)
                }
            }
            mapView
        },
    )
}
