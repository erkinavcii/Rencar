package com.turkcell.rencar_pair.ui.common.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
import org.maplibre.android.maps.MapLibreMap

data class MapMarkerItem(
    val id: String,
    val position: GeoPoint,
    val label: String,
    val color: Color,
)

fun renderMapMarkers(
    context: Context,
    map: MapLibreMap,
    items: List<MapMarkerItem>,
): Map<Marker, String> {
    map.markers.toList().forEach { map.removeMarker(it) }
    val iconFactory = IconFactory.getInstance(context)
    val markerToItemId = mutableMapOf<Marker, String>()
    items.forEach { item ->
        val bitmap = createLabelMarkerBitmap(item.label, item.color.toArgb())
        val marker = map.addMarker(
            MarkerOptions()
                .position(MapLibreLatLng(item.position.latitude, item.position.longitude))
                .icon(iconFactory.fromBitmap(bitmap)),
        )
        markerToItemId[marker] = item.id
    }
    return markerToItemId
}

private fun createLabelMarkerBitmap(label: String, backgroundColor: Int): Bitmap {
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
