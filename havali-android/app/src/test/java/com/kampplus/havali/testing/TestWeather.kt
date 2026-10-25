package com.kampplus.havali.testing

import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PlaceTag
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.model.WeatherSnapshot
import java.time.LocalDate
import java.time.LocalDateTime

/** Testlerde okunur hava nesneleri kurmak için küçük yardımcılar. Varsayılanlar "sakin, ılık, kuru". */
object TestWeather {

    val NOW: LocalDateTime = LocalDateTime.of(2026, 10, 25, 10, 0)

    fun snapshot(
        time: LocalDateTime = NOW,
        temperatureC: Double = 20.0,
        apparentC: Double = temperatureC,
        kind: WeatherKind = WeatherKind.CLEAR,
        rain: Int = 0,
        windKmh: Double = 8.0,
        gustKmh: Double = 12.0,
        uv: Double = 3.0,
        humidity: Int = 55,
        visibilityMeters: Double = 20_000.0,
        snowCm: Double = 0.0
    ) = WeatherSnapshot(time, temperatureC, apparentC, kind, rain, windKmh, gustKmh, uv, humidity, visibilityMeters, snowCm)

    fun day(date: LocalDate, min: Double, max: Double, rain: Int = 0, kind: WeatherKind = WeatherKind.CLEAR, uv: Double = 3.0) =
        DailyWeather(date, min, max, kind, rain, uv)

    fun forecast(hourly: List<WeatherSnapshot>, daily: List<DailyWeather> = emptyList()) =
        Forecast(GeoPoint(38.4, 27.1), hourly.first(), hourly, daily)

    fun place(vararg tags: PlaceTag) = Place("test", "Test", "Test", GeoPoint(38.4, 27.1), MapZoom.CITY, tags.toSet())
}
