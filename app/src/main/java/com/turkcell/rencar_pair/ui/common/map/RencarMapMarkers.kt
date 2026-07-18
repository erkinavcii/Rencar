package com.turkcell.rencar_pair.ui.common.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.turkcell.rencar_pair.R
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

// Aktif Yolculuk ekranindaki aracin canli konum marker'i icin: renkli bir daire
// icinde projede zaten var olan ic_car.xml (yandan gorunuslu araba) ikonu.
// Yandan gorunuslu oldugundan yon (bearing) rotasyonu uygulanmiyor - dondurulurse
// yan yatmis gibi gorunur; marker sabit yonde durup yalnizca konumu kayar
// (bkz. ActiveRentalScreen.ActiveRentalMapView, konum arasi yumusak animasyon).
fun createCarMarkerBitmap(context: Context, backgroundColor: Int, sizeDp: Int = 44): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (sizeDp * density).toInt()

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundColor }
    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius, backgroundPaint)

    val carDrawable = ContextCompat.getDrawable(context, R.drawable.ic_car) ?: return bitmap
    val iconSize = (size * 0.6f).toInt()
    val iconBitmap = carDrawable.toBitmap(width = iconSize, height = iconSize)
    val offset = (size - iconSize) / 2f
    canvas.drawBitmap(iconBitmap, offset, offset, null)

    return bitmap
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
