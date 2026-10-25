package com.kampplus.havali.domain

import com.kampplus.havali.domain.model.ActivityReason
import com.kampplus.havali.domain.model.ActivityType
import com.kampplus.havali.domain.model.PlaceTag
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.usecase.activity.EvaluateActivitiesUseCase
import com.kampplus.havali.testing.TestWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EvaluateActivitiesUseCaseTest {

    private val evaluate = EvaluateActivitiesUseCase()

    @Test
    fun `poyrazli Alacatida ruzgar sorfu one cikar, SUP uygun degildir`() {
        val windy = TestWeather.snapshot(temperatureC = 23.0, windKmh = 42.0, gustKmh = 55.0)

        val plan = evaluate(TestWeather.place(PlaceTag.COASTAL), windy)

        assertEquals(ActivityType.WINDSURF, plan.best.type)
        assertEquals(Suitability.GOOD, plan.best.verdict.suitability)
        val sup = plan.others.first { it.type == ActivityType.SUP }
        assertEquals(Suitability.BAD, sup.verdict.suitability)
        assertEquals(ActivityReason.WIND_TOO_STRONG, sup.verdict.reason)
        assertEquals(55, sup.verdict.value)
    }

    @Test
    fun `taze karli dagda kayak uygundur`() {
        val snowy = TestWeather.snapshot(temperatureC = -3.0, kind = WeatherKind.SNOW, rain = 60, gustKmh = 25.0, snowCm = 18.0)

        val plan = evaluate(TestWeather.place(PlaceTag.MOUNTAIN), snowy)

        assertEquals(ActivityType.SKIING, plan.best.type)
        assertEquals(ActivityReason.FRESH_SNOW, plan.best.verdict.reason)
        assertEquals(18, plan.best.verdict.value)
    }

    @Test
    fun `yagmurlu sehirde kapali havuz onerilir`() {
        val rainy = TestWeather.snapshot(temperatureC = 13.0, kind = WeatherKind.RAIN, rain = 70)

        val plan = evaluate(TestWeather.place(PlaceTag.URBAN), rainy)

        assertEquals(ActivityType.INDOOR_POOL, plan.best.type)
        assertEquals(Suitability.GOOD, plan.best.verdict.suitability)
    }

    @Test
    fun `denizi olmayan yerde su sporu onerilmez`() {
        val plan = evaluate(TestWeather.place(), TestWeather.snapshot())

        val types = (listOf(plan.best) + plan.others).map { it.type }
        assertFalse(ActivityType.SUP in types)
        assertFalse(ActivityType.SKIING in types)
    }
}
