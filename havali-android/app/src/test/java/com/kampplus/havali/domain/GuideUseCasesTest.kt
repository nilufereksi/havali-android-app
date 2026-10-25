package com.kampplus.havali.domain

import com.kampplus.havali.data.catalog.GuideCatalog
import com.kampplus.havali.domain.model.AlertType
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PlaceTag
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.TrafficLevel
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.usecase.guide.DetectAlertsUseCase
import com.kampplus.havali.domain.usecase.guide.EvaluateCampingUseCase
import com.kampplus.havali.domain.usecase.guide.PredictTrafficUseCase
import com.kampplus.havali.testing.TestWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideUseCasesTest {

    private val alsancak = Place("alsancak", "Alsancak", "İzmir", GeoPoint(38.437, 27.143), MapZoom.STREET, setOf(PlaceTag.COASTAL))

    @Test
    fun `sert hamlede kiyida firtina ve dalga uyarisi, siste sis uyarisi verilir`() {
        val hours = listOf(TestWeather.snapshot(gustKmh = 55.0, visibilityMeters = 700.0, kind = WeatherKind.RAIN, rain = 60))

        val alerts = DetectAlertsUseCase()(alsancak, hours).map { it.type }

        assertEquals(listOf(AlertType.STORM, AlertType.WAVES, AlertType.FOG), alerts)
    }

    @Test
    fun `ic kesimde dalga uyarisi verilmez`() {
        val inland = alsancak.copy(tags = emptySet())

        val alerts = DetectAlertsUseCase()(inland, listOf(TestWeather.snapshot(gustKmh = 40.0))).map { it.type }

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `kuru ve sakin havada kamp uygundur, yagmurda degildir`() {
        val sites = GuideCatalog.campsites().filter { it.cityId == "izmir" }

        val calm = EvaluateCampingUseCase()(sites, TestWeather.snapshot(temperatureC = 20.0))
        val rainy = EvaluateCampingUseCase()(sites, TestWeather.snapshot(rain = 80, kind = WeatherKind.RAIN))

        assertTrue(calm.all { it.verdict.suitability == Suitability.GOOD })
        assertTrue(rainy.all { it.verdict.suitability == Suitability.BAD })
    }

    @Test
    fun `aksam yogunlugu ve yagmur Altinyolu tikar, gece akicidir`() {
        val altinyol = GuideCatalog.corridors("izmir").first { it.id == "altinyol" }
        val predict = PredictTrafficUseCase()

        val rushInRain = predict(altinyol, TestWeather.NOW.withHour(18), TestWeather.snapshot(rain = 80, kind = WeatherKind.RAIN), 0)
        val night = predict(altinyol, TestWeather.NOW.withHour(2), TestWeather.snapshot(), 0)

        assertEquals(TrafficLevel.HEAVY, rushInRain.level)
        assertEquals(TrafficLevel.LIGHT, night.level)
        assertTrue(rushInRain.delayMinutes > night.delayMinutes)
    }
}
