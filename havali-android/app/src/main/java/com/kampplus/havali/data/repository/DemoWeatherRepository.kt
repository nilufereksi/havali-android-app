package com.kampplus.havali.data.repository

import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.domain.repository.WeatherRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Demo senaryosu: tasarımdaki hikâyeyi üreten sahte hava kaynağı.
 *
 * Batıdan gelen bir yağmur cephesi saat ilerledikçe Urla'dan Bornova'ya geçer; Alaçatı'da
 * poyraz, Çeşme'de güneş, Bozdağ ve Erzurum'da kar vardır. [WeatherRepository] arayüzünü
 * uyguladığı için ekranlar canlı veriyle demo arasındaki farkı bilmez (Strategy deseni).
 */
class DemoWeatherRepository(
    private val clock: () -> LocalDateTime = { LocalDateTime.now() },
    private val latencyMillis: Long = DEFAULT_LATENCY_MS
) : WeatherRepository {

    override suspend fun getForecast(point: GeoPoint): Forecast {
        delay(latencyMillis)
        return build(point)
    }

    override suspend fun getForecasts(points: List<GeoPoint>): List<Forecast> {
        delay(latencyMillis)
        return points.map(::build)
    }

    private fun build(point: GeoPoint): Forecast {
        val now = clock().truncatedTo(ChronoUnit.MINUTES)
        val firstHour = now.truncatedTo(ChronoUnit.HOURS)
        val region = DemoRegions.of(point)
        val hourly = (0 until HOURS).map { offset ->
            region.snapshot(point, firstHour.plusHours(offset.toLong()), step = (offset / HOURS_PER_STEP).coerceAtMost(LAST_STEP))
        }
        return Forecast(
            point = point,
            current = hourly.first().copy(time = now),
            hourly = hourly,
            daily = (0 until DAYS).map { region.day(now.toLocalDate().plusDays(it.toLong()), it) }
        )
    }

    private companion object {
        const val DEFAULT_LATENCY_MS = 450L
        const val HOURS = 48
        const val DAYS = 14
        const val HOURS_PER_STEP = 3
        const val LAST_STEP = 4
    }
}

/** Zaman çubuğunun bir adımındaki hava. [visibilityMeters] verilmezse bölgenin görüşü kullanılır. */
private data class Step(
    val kind: WeatherKind,
    val temperatureC: Double,
    val rain: Int,
    val windKmh: Double,
    val gustKmh: Double,
    val visibilityMeters: Double? = null
)

private data class DayPattern(val kind: WeatherKind, val minC: Double, val maxC: Double, val rain: Int, val uvMax: Double)

/**
 * Bir bölgenin demo profili. [matches] koşulunu sağlayan ilk bölge kullanılır;
 * böylece "hangi bölgedeyiz" kararı if zinciri yerine bir listeyle verilir.
 */
private class DemoRegion(
    val matches: (GeoPoint) -> Boolean,
    val steps: (GeoPoint) -> List<Step>,
    val uvIndex: Double,
    val humidity: Int,
    val snowCm: Double,
    val visibilityMeters: Double,
    val days: List<DayPattern>
) {
    fun snapshot(point: GeoPoint, time: LocalDateTime, step: Int): WeatherSnapshot {
        val current = steps(point)[step]
        return WeatherSnapshot(
            time = time,
            temperatureC = current.temperatureC,
            apparentTemperatureC = current.temperatureC - current.windKmh / WIND_CHILL_DIVISOR,
            kind = current.kind,
            precipitationProbability = current.rain,
            windKmh = current.windKmh,
            gustKmh = current.gustKmh,
            uvIndex = uvIndex * daylight(time.hour),
            humidityPercent = humidity,
            visibilityMeters = current.visibilityMeters ?: visibilityMeters,
            snowDepthCm = snowCm
        )
    }

    /** UV gün ortasında en yüksektir, gece sıfırdır: 06:00-20:00 arası yarım sinüs. */
    private fun daylight(hour: Int): Double = sin(PI * (hour - SUNRISE_HOUR) / DAYLIGHT_HOURS).coerceAtLeast(0.0)

    fun day(date: LocalDate, index: Int): DailyWeather {
        val pattern = days[index % days.size]
        return DailyWeather(date, pattern.minC, pattern.maxC, pattern.kind, pattern.rain, pattern.uvMax)
    }

    private companion object {
        const val WIND_CHILL_DIVISOR = 12.0
        const val SUNRISE_HOUR = 6
        const val DAYLIGHT_HOURS = 14.0
    }
}

private object DemoRegions {

    /** Yağmur hücresinin her adımdaki boylamı: Urla -> Konak -> Bornova -> Kemalpaşa. */
    private val CELL_LONGITUDES = listOf(26.62, 26.9, 27.15, 27.45, 27.8)
    private const val CELL_HALF_WIDTH = 0.3

    private fun steps(kind: WeatherKind, temps: List<Int>, rain: Int, wind: Double, gust: Double): List<Step> =
        temps.map { Step(kind, it.toDouble(), rain, wind, gust) }

    private fun day(kind: WeatherKind, min: Int, max: Int, rain: Int, uv: Double = 3.0) =
        DayPattern(kind, min.toDouble(), max.toDouble(), rain, uv)

    /** İzmir'in haftası: tasarımdaki plan çakışmaları (Çarşamba halı saha, Perşembe piknik) buna göre oluşur. */
    private val IZMIR_WEEK = listOf(
        day(WeatherKind.RAIN, 11, 15, 70),
        day(WeatherKind.CLOUDY, 12, 18, 20),
        day(WeatherKind.CLEAR, 13, 21, 5, 4.0),
        day(WeatherKind.RAIN, 12, 16, 80),
        day(WeatherKind.RAIN, 11, 15, 80),
        day(WeatherKind.CLEAR, 15, 24, 5, 5.0),
        day(WeatherKind.CLEAR, 14, 23, 5, 5.0)
    )

    /** İstanbul: valiz senaryosunda gidiş günlerinde (7-10. günler) yağmur ve serin akşamlar. */
    private val MARMARA_DAYS = listOf(
        day(WeatherKind.RAIN, 10, 14, 70),
        day(WeatherKind.CLOUDY, 11, 16, 30),
        day(WeatherKind.CLEAR, 12, 18, 10),
        day(WeatherKind.CLOUDY, 11, 16, 30),
        day(WeatherKind.RAIN, 10, 14, 70),
        day(WeatherKind.CLOUDY, 10, 15, 40),
        day(WeatherKind.CLEAR, 11, 17, 10),
        day(WeatherKind.RAIN, 8, 13, 75),
        day(WeatherKind.RAIN, 8, 12, 80),
        day(WeatherKind.CLOUDY, 9, 14, 40),
        day(WeatherKind.CLEAR, 9, 15, 10)
    )

    private val SUNNY_DAYS = listOf(day(WeatherKind.CLEAR, 18, 26, 5, 6.0), day(WeatherKind.CLEAR, 17, 25, 5, 6.0))
    private val SNOWY_DAYS = listOf(day(WeatherKind.SNOW, -8, -1, 70, 1.0), day(WeatherKind.CLOUDY, -9, -2, 30, 1.0))
    private val GREY_DAYS = listOf(day(WeatherKind.CLOUDY, 8, 15, 25), day(WeatherKind.CLEAR, 7, 16, 10))
    private val WET_DAYS = listOf(day(WeatherKind.RAIN, 9, 13, 75), day(WeatherKind.CLOUDY, 9, 14, 40))

    /** İzmir cephesi: hücrenin altında sağanak, sert hamle (fırtına uyarısı) ve düşük görüş. */
    private fun izmirFront(point: GeoPoint): List<Step> = CELL_LONGITUDES.mapIndexed { index, cellLon ->
        val raining = abs(point.longitude - cellLon) < CELL_HALF_WIDTH
        when (raining) {
            true -> Step(WeatherKind.RAIN, (14 - index).toDouble(), 75, 30.0, 52.0, visibilityMeters = 1500.0)
            false -> Step(WeatherKind.CLOUDY, (15 - index).toDouble(), 20, 22.0, 38.0)
        }
    }

    /** Gemlik körfezi: sabah sisi, öğleden sonra lodos ve fırtına. */
    private val GEMLIK_STEPS = listOf(
        Step(WeatherKind.FOG, 13.0, 20, 8.0, 15.0, visibilityMeters = 600.0),
        Step(WeatherKind.FOG, 14.0, 30, 12.0, 22.0, visibilityMeters = 900.0),
        Step(WeatherKind.RAIN, 14.0, 70, 32.0, 48.0),
        Step(WeatherKind.STORM, 13.0, 85, 40.0, 62.0, visibilityMeters = 2500.0),
        Step(WeatherKind.RAIN, 12.0, 60, 28.0, 40.0)
    )

    private val REGIONS = listOf(
        DemoRegion(
            matches = { it.longitude > 39.5 && it.latitude in 39.0..40.8 },
            steps = { steps(WeatherKind.SNOW, listOf(-2, -1, -3, -6, -8), 70, 20.0, 30.0) },
            uvIndex = 1.0,
            humidity = 85,
            snowCm = 22.0,
            visibilityMeters = 1000.0,
            days = SNOWY_DAYS
        ),
        DemoRegion(
            matches = { it.latitude in 38.2..38.5 && it.longitude in 27.8..28.4 },
            steps = { steps(WeatherKind.SNOW, listOf(-3, -2, -4, -6, -7), 60, 15.0, 25.0) },
            uvIndex = 2.0,
            humidity = 80,
            snowCm = 18.0,
            visibilityMeters = 800.0,
            days = SNOWY_DAYS
        ),
        DemoRegion(
            matches = { it.latitude in 38.2..38.32 && it.longitude in 26.33..26.45 },
            steps = {
                listOf(38.0, 42.0, 45.0, 36.0, 30.0).zip(listOf(23, 23, 22, 20, 18)) { w, t ->
                    Step(WeatherKind.CLEAR, t.toDouble(), 5, w, 55.0)
                }
            },
            uvIndex = 5.0,
            humidity = 55,
            snowCm = 0.0,
            visibilityMeters = 20_000.0,
            days = SUNNY_DAYS
        ),
        DemoRegion(
            matches = { it.longitude < 26.6 && it.latitude in 37.9..38.6 },
            steps = { steps(WeatherKind.CLEAR, listOf(24, 24, 23, 21, 19), 5, 9.0, 14.0) },
            uvIndex = 5.0,
            humidity = 60,
            snowCm = 0.0,
            visibilityMeters = 20_000.0,
            days = SUNNY_DAYS
        ),
        DemoRegion(
            matches = { it.latitude in 38.05..38.56 && it.longitude in 26.6..27.9 },
            steps = ::izmirFront,
            uvIndex = 3.0,
            humidity = 86,
            snowCm = 0.0,
            visibilityMeters = 6_000.0,
            days = IZMIR_WEEK
        ),
        DemoRegion(
            matches = { it.latitude in 38.55..39.3 && it.longitude in 26.4..27.6 },
            steps = { steps(WeatherKind.CLEAR, listOf(17, 17, 16, 14, 12), 10, 12.0, 20.0) },
            uvIndex = 4.0,
            humidity = 65,
            snowCm = 0.0,
            visibilityMeters = 20_000.0,
            days = SUNNY_DAYS
        ),
        DemoRegion(
            matches = { it.latitude in 40.3..40.6 && it.longitude in 28.7..29.8 },
            steps = { GEMLIK_STEPS },
            uvIndex = 2.0,
            humidity = 90,
            snowCm = 0.0,
            visibilityMeters = 6_000.0,
            days = MARMARA_DAYS
        ),
        DemoRegion(
            matches = { it.latitude in 39.5..40.6 && it.longitude in 31.8..33.3 },
            steps = { steps(WeatherKind.CLEAR, listOf(9, 10, 8, 5, 3), 5, 10.0, 18.0) },
            uvIndex = 3.0,
            humidity = 45,
            snowCm = 0.0,
            visibilityMeters = 20_000.0,
            days = GREY_DAYS
        ),
        DemoRegion(
            matches = { it.latitude > 40.3 && it.longitude < 31.0 },
            steps = {
                listOf(
                    Step(WeatherKind.CLOUDY, 14.0, 20, 18.0, 30.0),
                    Step(WeatherKind.CLOUDY, 14.0, 40, 20.0, 34.0),
                    Step(WeatherKind.RAIN, 13.0, 70, 24.0, 42.0),
                    Step(WeatherKind.RAIN, 12.0, 80, 24.0, 42.0),
                    Step(WeatherKind.RAIN, 11.0, 80, 20.0, 36.0)
                )
            },
            uvIndex = 2.0,
            humidity = 82,
            snowCm = 0.0,
            visibilityMeters = 8_000.0,
            days = MARMARA_DAYS
        ),
        DemoRegion(
            matches = { it.latitude > 40.7 },
            steps = { steps(WeatherKind.RAIN, listOf(12, 12, 11, 11, 10), 75, 16.0, 28.0) },
            uvIndex = 1.0,
            humidity = 88,
            snowCm = 0.0,
            visibilityMeters = 7_000.0,
            days = WET_DAYS
        ),
        DemoRegion(
            matches = { it.latitude < 37.5 },
            steps = { steps(WeatherKind.CLEAR, listOf(26, 26, 24, 22, 20), 5, 8.0, 12.0) },
            uvIndex = 6.0,
            humidity = 58,
            snowCm = 0.0,
            visibilityMeters = 20_000.0,
            days = SUNNY_DAYS
        )
    )

    private val DEFAULT_REGION = DemoRegion(
        matches = { true },
        steps = { steps(WeatherKind.CLOUDY, listOf(14, 14, 13, 11, 9), 15, 12.0, 20.0) },
        uvIndex = 3.0,
        humidity = 60,
        snowCm = 0.0,
        visibilityMeters = 15_000.0,
        days = GREY_DAYS
    )

    fun of(point: GeoPoint): DemoRegion = REGIONS.firstOrNull { it.matches(point) } ?: DEFAULT_REGION
}
