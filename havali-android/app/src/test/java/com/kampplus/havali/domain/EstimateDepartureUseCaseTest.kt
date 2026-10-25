package com.kampplus.havali.domain

import com.kampplus.havali.domain.model.DelayFactor
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.usecase.plan.EstimateDepartureUseCase
import com.kampplus.havali.testing.TestWeather
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstimateDepartureUseCaseTest {

    private val estimate = EstimateDepartureUseCase()
    private val cem = Plan(
        id = "cem",
        title = "Cem'le buluşma",
        placeName = "Alsancak",
        point = GeoPoint(38.437, 27.141),
        start = LocalDateTime.of(2026, 10, 25, 18, 0),
        outdoor = true,
        flexible = false,
        mode = TravelMode.CAR,
        distanceKm = 6.1
    )

    @Test
    fun `yagmur, aksam yogunlugu ve rapor yolu uzatir`() {
        val advice = estimate(cem, TestWeather.snapshot(rain = 70), reportsOnRoute = 1)

        // 6,1 km / 24 km/sa = 15 dk + 7 dk hazırlık = 22 dk
        assertEquals(22, advice.usualMinutes)
        assertEquals(listOf(DelayFactor.RAIN, DelayFactor.RUSH_HOUR, DelayFactor.COMMUNITY_REPORTS), advice.delays.map { it.factor })
        assertEquals(22 + 8 + 6 + 3, advice.travelMinutes)
        // 18:00 - (39 dk yol + 10 dk pay)
        assertEquals(LocalTime.of(17, 11), advice.leaveAt)
        assertEquals(LocalTime.of(17, 28), advice.usualLeaveAt)
    }

    @Test
    fun `kuru havada ve yogunluk disinda ek gecikme yoktur`() {
        val advice = estimate(cem.copy(start = cem.start.withHour(11)), TestWeather.snapshot(), reportsOnRoute = 0)

        assertTrue(advice.delays.isEmpty())
        assertEquals(advice.usualMinutes, advice.travelMinutes)
    }

    @Test
    fun `sisli havada gorus gecikmesi eklenir`() {
        val advice = estimate(cem.copy(start = cem.start.withHour(9)), TestWeather.snapshot(visibilityMeters = 600.0), reportsOnRoute = 0)

        assertEquals(listOf(DelayFactor.LOW_VISIBILITY), advice.delays.map { it.factor })
    }
}
