package com.kampplus.havali.domain

import com.kampplus.havali.domain.model.ComfortFeedback
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.ComfortSensitivity
import com.kampplus.havali.domain.model.HazardType
import com.kampplus.havali.domain.model.RiskLevel
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.usecase.road.AssessRoadRiskUseCase
import com.kampplus.havali.testing.TestWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadAndComfortTest {

    private val assess = AssessRoadRiskUseCase()
    private val stormyEvening = listOf(
        TestWeather.snapshot(rain = 20, gustKmh = 20.0),
        TestWeather.snapshot(rain = 80, gustKmh = 45.0, kind = WeatherKind.RAIN)
    )

    @Test
    fun `ayni havada motor toplu tasimadan daha riskli`() {
        val motor = assess(TravelMode.MOTORBIKE, stormyEvening)
        val transit = assess(TravelMode.TRANSIT, stormyEvening)

        assertTrue(motor.level.ordinal > transit.level.ordinal)
        assertEquals(RiskLevel.VERY_HIGH, motor.level)
    }

    @Test
    fun `kaygan zemin ve ruzgar hamlesi tehlike olarak listelenir`() {
        val hazards = assess(TravelMode.CAR, stormyEvening).hazards.map { it.type }

        assertEquals(listOf(HazardType.SLIPPERY_ROAD, HazardType.STRONG_GUSTS, HazardType.HEAVY_TRAFFIC), hazards)
    }

    @Test
    fun `sik usuyen kullanicinin profili negatif ofset ogrenir`() {
        val profile = (1..4).fold(ComfortProfile()) { current, _ -> current.record(ComfortFeedback.COLD) }

        assertEquals(-3.0, profile.offsetC, 0.001)
        assertEquals(4, profile.samples)
        assertEquals(ComfortSensitivity.FEELS_COLD, profile.sensitivity)
        assertEquals(9.0, profile.feelsLikeFor(12.0), 0.001)
    }

    @Test
    fun `iyiydi cevabi ofseti yavasca sifira ceker`() {
        val profile = ComfortProfile(offsetC = -3.0, samples = 2).record(ComfortFeedback.OK)

        assertEquals(-2.0, profile.offsetC, 0.001)
    }
}
