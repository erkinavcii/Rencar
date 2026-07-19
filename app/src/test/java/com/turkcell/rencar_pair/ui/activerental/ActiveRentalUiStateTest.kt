package com.turkcell.rencar_pair.ui.activerental

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

// currentCost/distanceKm test edilmiyor: hesaplanan degil, backend'den gelen ham alanlar.
class ActiveRentalUiStateTest {

    // --- planLabel ---

    @Test
    fun `planLabel returns Dakikalik for PER_MINUTE plan`() {
        val state = ActiveRentalUiState(plan = "PER_MINUTE")

        assertEquals("Dakikalık", state.planLabel)
    }

    @Test
    fun `planLabel returns Saatlik for HOURLY plan`() {
        val state = ActiveRentalUiState(plan = "HOURLY")

        assertEquals("Saatlik", state.planLabel)
    }

    @Test
    fun `planLabel returns Gunluk for DAILY plan`() {
        val state = ActiveRentalUiState(plan = "DAILY")

        assertEquals("Günlük", state.planLabel)
    }

    @Test
    fun `planLabel returns empty string for an unknown plan value`() {
        val state = ActiveRentalUiState(plan = "UNKNOWN_VALUE")

        assertEquals("", state.planLabel)
    }

    // --- elapsedSeconds / elapsedTimeLabel ---

    @Test
    fun `elapsedSeconds and elapsedTimeLabel default to zero when startEpochMillis is null`() {
        val state = ActiveRentalUiState(startEpochMillis = null)

        assertEquals(0L, state.elapsedSeconds)
        assertEquals("00:00:00", state.elapsedTimeLabel)
    }

    @Test
    fun `elapsedTimeLabel formats a duration under an hour`() {
        val startMillis = 0L
        val nowMillis = 125_000L // 125 saniye = 2 dakika 5 saniye

        val state = ActiveRentalUiState(startEpochMillis = startMillis, nowEpochMillis = nowMillis)

        assertEquals(125L, state.elapsedSeconds)
        assertEquals("00:02:05", state.elapsedTimeLabel)
    }

    @Test
    fun `elapsedTimeLabel formats a duration past one hour`() {
        val startMillis = 0L
        val nowMillis = 3_661_000L // 3661 saniye = 1 saat 1 dakika 1 saniye

        val state = ActiveRentalUiState(startEpochMillis = startMillis, nowEpochMillis = nowMillis)

        assertEquals(3661L, state.elapsedSeconds)
        assertEquals("01:01:01", state.elapsedTimeLabel)
    }

    // --- startedAtLabel ---

    @Test
    fun `startedAtLabel returns an em dash when startEpochMillis is null`() {
        val state = ActiveRentalUiState(startEpochMillis = null)

        assertEquals("—", state.startedAtLabel)
    }

    @Test
    fun `startedAtLabel formats startEpochMillis using dd MM yyyy HH mm pattern`() {
        val startMillis = 1752741296000L
        val state = ActiveRentalUiState(startEpochMillis = startMillis)
        // Beklenen deger, calisan JVM'in varsayilan saat dilimiyle uretiliyor - production
        // kodundaki formatlayiciyla birebir ayni cagri (ActiveRentalContract.kt satir 62).
        // Boylece test, gelistirme makinesi ile CI farkli saat dilimlerinde olsa bile kararli
        // kalir.
        val expected = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")).format(Date(startMillis))

        assertEquals(expected, state.startedAtLabel)
    }
}
