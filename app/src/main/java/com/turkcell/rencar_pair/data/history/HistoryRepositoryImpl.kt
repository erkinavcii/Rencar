package com.turkcell.rencar_pair.data.history

import androidx.compose.ui.geometry.Offset
import com.turkcell.rencar_pair.data.model.RentalResponseDto
import com.turkcell.rencar_pair.data.repository.RentalRepository
import com.turkcell.rencar_pair.data.repository.VehicleRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Gerçek backend verisiyle çalışır: `GET /rentals` ile kullanıcının kiralamaları,
 * her kiralama için `GET /vehicles/{id}` ile araç marka/modeli çekilir.
 *
 * Not: `/vehicles/{id}` yalnızca AVAILABLE araçları döndürür (customer uç noktası).
 * Geçmişteki bir araç şu an başka biri tarafından kiralanmışsa (RENTED ise) bu
 * çağrı 404 döner; böyle durumlarda araç adı "Araç" olarak gösterilir.
 *
 * Not 2: Backend `RentalResponseDto`'da mesafe (km) alanı bulunmadığından
 * bu bilgi ekranda gösterilmiyor.
 */
@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val vehicleRepository: VehicleRepository,
) : HistoryRepository {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val displayFormat = SimpleDateFormat("d MMM yyyy · HH:mm", Locale("tr", "TR"))
    private val fullDisplayFormat = SimpleDateFormat("d MMMM yyyy, EEEE · HH:mm", Locale("tr", "TR"))

    private data class VehicleInfo(val name: String, val plate: String)

    override suspend fun getHistory(): Result<HistorySummary> = runCatching {
        val rentals = rentalRepository.getMyRentals().getOrThrow()
        val completed = rentals.filter { it.status == "COMPLETED" }

        // Aynı aracı birden fazla kez sorgulamamak için basit bir önbellek.
        val vehicleInfoCache = HashMap<String, VehicleInfo>()

        val trips = coroutineScope {
            completed.map { rental ->
                async {
                    val vehicleInfo = vehicleInfoCache.getOrPut(rental.vehicleId) {
                        val vehicle = vehicleRepository.getVehicleDetails(rental.vehicleId).getOrNull()
                        if (vehicle != null) {
                            VehicleInfo(name = "${vehicle.brand} ${vehicle.model}".trim(), plate = vehicle.plate)
                        } else {
                            VehicleInfo(name = "Araç", plate = "-")
                        }
                    }
                    rental.toHistoryTrip(vehicleInfo)
                }
            }.map { it.await() }
        }

        val now = Calendar.getInstance()
        val thisMonthCompleted = completed.filter { isSameMonth(it.startDate, now) }

        HistorySummary(
            monthlyTripCount = thisMonthCompleted.size,
            monthlySpending = thisMonthCompleted.sumOf { it.totalPrice ?: 0.0 },
            totalTripCount = completed.size,
            totalSpending = completed.sumOf { it.totalPrice ?: 0.0 },
            trips = trips,
        )
    }

    private fun RentalResponseDto.toHistoryTrip(vehicleInfo: VehicleInfo): HistoryTrip {
        val start = runCatching { isoFormat.parse(startDate) }.getOrNull()

        val dateLabel = start?.let { displayFormat.format(it) } ?: startDate
        val fullDateLabel = start?.let { fullDisplayFormat.format(it) } ?: startDate
        // endDate yalniz DAILY planda dolu; PER_MINUTE/HOURLY'de sure icin backend'in
        // zaten hesapladigi durationMinutes alani kullaniliyor (plan-bagimsiz, dogru kaynak).
        val durationMinutesValue = durationMinutes.toLong()
        val durationLabel = if (durationMinutesValue > 0) {
            if (durationMinutesValue < 60) "$durationMinutesValue dk" else "${durationMinutesValue / 60} sa ${durationMinutesValue % 60} dk"
        } else {
            "-"
        }

        // Rota önizlemesi yalnızca görsel bir süsleme: backend'de GPS rotası
        // saklanmadığından kiralama id'sinden türetilen sabit noktalar kullanılır.
        val seed = id.hashCode()
        val routeStart = Offset(
            x = 0.22f + (abs(seed) % 40) / 100f,
            y = 0.18f + (abs(seed / 7) % 20) / 100f,
        )
        val routeEnd = Offset(
            x = 0.62f + (abs(seed / 3) % 20) / 100f,
            y = 0.60f + (abs(seed / 11) % 24) / 100f,
        )

        return HistoryTrip(
            id = id,
            vehicleName = vehicleInfo.name.ifBlank { "Araç" },
            plate = vehicleInfo.plate,
            dateLabel = dateLabel,
            fullDateLabel = fullDateLabel,
            startDateMillis = start?.time ?: 0L,
            durationLabel = durationLabel,
            durationMinutes = durationMinutesValue,
            price = totalPrice ?: 0.0,
            paymentMethod = paymentMethod,
            routeStart = routeStart,
            routeEnd = routeEnd,
        )
    }

    private fun isSameMonth(isoDate: String, reference: Calendar): Boolean {
        val date = runCatching { isoFormat.parse(isoDate) }.getOrNull() ?: return false
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.YEAR) == reference.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == reference.get(Calendar.MONTH)
    }
}