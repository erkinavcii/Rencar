package com.turkcell.rencar_pair.ui.activerental

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class ActiveRentalLatLng(val latitude: Double, val longitude: Double)

private const val EARTH_RADIUS_METERS = 6_371_000.0

// Home ekranindaki ile ayni gercek Haversine hesabi (uydurma degil); iki ayri
// ozellik paketi (ui.home / ui.activerental) birbirine bagimli olmasin diye
// kucuk bir yardimci burada da ayrica tutuluyor.
internal fun haversineMeters(from: ActiveRentalLatLng, to: ActiveRentalLatLng): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLng = Math.toRadians(to.longitude - from.longitude)
    val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(h))
}

data class ActiveRentalUiState(
    val rentalId: String = "",
    val vehicleId: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Double = 0.0,
    // Backend'in RentalResponseDto.plan alani (PER_MINUTE, HOURLY, DAILY); arac karti
    // altyazisinda ("34 HCH 305 · Dakikalık") gosterilir.
    val plan: String = "",
    // RentalResponseDto.startFee — anlik ucrete dahil edilen sabit baslangic ucreti
    // (uydurma degil, backend'den geliyor).
    val startFee: Double = 0.0,
    val startEpochMillis: Long? = null,
    val nowEpochMillis: Long = System.currentTimeMillis(),
    val distanceMeters: Double = 0.0,
    val isVehicleLocked: Boolean = false,
    // Aracin backend'den Socket.IO ile gelen canli konumu (kiracinin kendi telefon GPS'inden
    // ayri); aktif kiralama yoksa veya henuz ilk kare gelmediyse null kalir.
    val vehicleLocation: ActiveRentalLatLng? = null,
) {
    val elapsedSeconds: Long
        get() {
            val start = startEpochMillis ?: return 0L
            return ((nowEpochMillis - start) / 1000L).coerceAtLeast(0L)
        }

    val elapsedTimeLabel: String
        get() {
            val total = elapsedSeconds
            val hours = total / 3600
            val minutes = (total % 3600) / 60
            val seconds = total % 60
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

    // Backend yalnizca gunluk fiyat (pricePerDay) donduruyor; anlik ucret gecen
    // gercek sureye orantili turetiliyor (uydurma sabit degil, VehicleDetailBottomSheet
    // ve Rezervasyon ekranindaki ile ayni yaklasim). startFee, backend'in RentalResponseDto'da
    // dondurdugu gercek sabit baslangic ucreti; anlik ucrete dahil edilir.
    val liveCost: Double
        get() = (pricePerDay / 1440.0) * (elapsedSeconds / 60.0) + startFee

    val distanceKm: Double get() = distanceMeters / 1000.0

    val planLabel: String
        get() = when (plan) {
            "PER_MINUTE" -> "Dakikalık"
            "HOURLY" -> "Saatlik"
            "DAILY" -> "Günlük"
            else -> ""
        }

    val startedAtLabel: String
        get() {
            val start = startEpochMillis ?: return "—"
            return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")).format(Date(start))
        }
}

sealed interface ActiveRentalIntent {
    data object LockToggleClicked : ActiveRentalIntent
    data object EndRentalClicked : ActiveRentalIntent
    data object Tick : ActiveRentalIntent
    data class LocationUpdated(val location: ActiveRentalLatLng) : ActiveRentalIntent
}

sealed interface ActiveRentalEffect {
    data class NavigateToVehicleCondition(
        val rentalId: String,
        val vehicleId: String,
        val brand: String,
        val model: String,
        val plate: String,
        val durationSeconds: Long,
        val distanceMeters: Double,
    ) : ActiveRentalEffect
}
