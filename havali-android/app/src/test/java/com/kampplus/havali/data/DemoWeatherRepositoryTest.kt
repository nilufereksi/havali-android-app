package com.kampplus.havali.data

import com.kampplus.havali.data.catalog.PlaceCatalog
import com.kampplus.havali.data.repository.DemoWeatherRepository
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.testing.TestWeather
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Demo senaryosu tasarımdaki hikâyeyi anlatıyor mu? */
class DemoWeatherRepositoryTest {

    private val repository = DemoWeatherRepository(clock = { TestWeather.NOW }, latencyMillis = 0)

    private fun forecastOf(id: String) = runBlocking { repository.getForecast(PlaceCatalog.places.first { it.id == id }.point) }

    @Test
    fun `yagmur cephesi Urladan Bornovaya ilerler`() {
        val urla = forecastOf("urla")
        val bornova = forecastOf("bornova")

        assertEquals(WeatherKind.RAIN, urla.after(0).kind)
        assertEquals(WeatherKind.CLOUDY, bornova.after(0).kind)
        assertEquals(WeatherKind.RAIN, bornova.after(6).kind)
    }

    @Test
    fun `Alacatida poyraz, Bozdagda kar, Antalyada gunes var`() {
        assertTrue(forecastOf("alacati").current.gustKmh >= 50)
        assertEquals(18.0, forecastOf("bozdag").current.snowDepthCm, 0.001)
        assertEquals(WeatherKind.CLEAR, forecastOf("antalya").current.kind)
    }

    @Test
    fun `tahmin 48 saat ve 14 gun kapsar`() {
        val forecast = forecastOf("istanbul")

        assertEquals(48, forecast.hourly.size)
        assertEquals(14, forecast.daily.size)
        assertEquals(TestWeather.NOW.toLocalDate(), forecast.daily.first().date)
    }
}
