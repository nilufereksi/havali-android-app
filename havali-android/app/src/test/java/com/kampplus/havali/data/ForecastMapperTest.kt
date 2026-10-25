package com.kampplus.havali.data

import com.google.gson.Gson
import com.kampplus.havali.data.remote.dto.ForecastDto
import com.kampplus.havali.data.remote.dto.GeocodingResultDto
import com.kampplus.havali.data.remote.mapper.toDomain
import com.kampplus.havali.data.remote.mapper.toPlace
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.WeatherKind
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Gerçek Open-Meteo yanıtından alınmış JSON ile DTO -> domain dönüşümü. */
class ForecastMapperTest {

    private val json = checkNotNull(javaClass.classLoader).getResource("forecast_single.json").readText()
    private val dto = Gson().fromJson(json, ForecastDto::class.java)

    @Test
    fun `anlik hava ve gunluk tahmin dogru cevrilir`() {
        val forecast = dto.toDomain(GeoPoint(39.93, 32.85))

        assertEquals(LocalDateTime.of(2026, 9, 24, 11, 30), forecast.current.time)
        assertEquals(14.5, forecast.current.temperatureC, 0.001)
        assertEquals(13.1, forecast.current.apparentTemperatureC, 0.001)
        assertEquals(WeatherKind.CLOUDY, forecast.current.kind)
        assertEquals(LocalDate.of(2026, 9, 25), forecast.daily[1].date)
        assertEquals(6.2, forecast.daily[1].minTemperatureC, 0.001)
    }

    @Test
    fun `sicakligi bos olan saat satiri atlanir`() {
        val forecast = dto.toDomain(GeoPoint(39.93, 32.85))

        assertEquals(2, forecast.hourly.size)
        assertEquals(WeatherKind.RAIN, forecast.hourly[1].kind)
        assertEquals(35, forecast.hourly[1].precipitationProbability)
    }

    @Test(expected = IllegalStateException::class)
    fun `current blogu olmayan yanit hata verir`() {
        ForecastDto(latitude = null, longitude = null, current = null, hourly = null, daily = null).toDomain(GeoPoint(0.0, 0.0))
    }

    @Test
    fun `koordinati eksik arama sonucu atlanir`() {
        val missing =
            GeocodingResultDto(
                id = 1,
                name = "Hayalet",
                latitude = null,
                longitude = 27.0,
                countryCode = "TR",
                country = "Türkiye",
                admin1 = null
            )
        val izmir =
            GeocodingResultDto(
                id = 311046,
                name = "İzmir",
                latitude = 38.41,
                longitude = 27.14,
                countryCode = "TR",
                country = "Türkiye",
                admin1 = "İzmir"
            )

        assertNull(missing.toPlace())
        assertEquals("İzmir, Türkiye", izmir.toPlace()?.region)
    }
}
