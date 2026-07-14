package com.turkcell.rencar_pair.ui.common.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap

/**
 * Home'daki konum-dinleme bloguyla ActiveRental'daki enableLiveLocation'in
 * birlesimi. cameraMode cagiran ekrana gore degisir (Home: NONE, ActiveRental: TRACKING).
 * Ilk konum FusedLocationProviderClient.getCurrentLocation ile taze alinir (bkz.
 * docs/decisions.md, "getLastLocation Yerine getCurrentLocation"), ardindan LocationEngine
 * canli guncellemeleri dinler. Cagiran taraf donen fonksiyonu DisposableEffect.onDispose
 * icinde cagirarak guncellemeleri durdurmali.
 */
@SuppressLint("MissingPermission")
fun enableLiveLocation(
    context: Context,
    map: MapLibreMap,
    priority: Int = LocationEngineRequest.PRIORITY_HIGH_ACCURACY,
    cameraMode: Int = CameraMode.NONE,
    onLocationUpdate: (Location) -> Unit,
): () -> Unit {
    val style = map.style ?: return {}
    val locationComponent = map.locationComponent

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
    locationComponent.setCameraMode(cameraMode)
    if (cameraMode == CameraMode.TRACKING) {
        locationComponent.zoomWhileTracking(16.0)
    }

    val engine = locationComponent.locationEngine
    val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult) {
            result.lastLocation?.let(onLocationUpdate)
        }

        override fun onFailure(exception: Exception) = Unit
    }

    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
        .addOnSuccessListener { location -> location?.let(onLocationUpdate) }
    engine?.requestLocationUpdates(request, callback, Looper.getMainLooper())

    return { engine?.removeLocationUpdates(callback) }
}
