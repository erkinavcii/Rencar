package com.turkcell.rencar_pair.ui.common.map

import android.content.Context
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap

private const val CAMERA_FIT_PADDING_DP = 56

fun fitCameraToPoints(context: Context, map: MapLibreMap, points: List<GeoPoint>) {
    when {
        points.isEmpty() -> Unit
        points.size == 1 -> {
            val point = points.first()
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(MapLibreLatLng(point.latitude, point.longitude), 14.0),
            )
        }
        else -> {
            val bounds = LatLngBounds.Builder().apply {
                points.forEach { include(MapLibreLatLng(it.latitude, it.longitude)) }
            }.build()
            val paddingPx = (CAMERA_FIT_PADDING_DP * context.resources.displayMetrics.density).toInt()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
        }
    }
}
