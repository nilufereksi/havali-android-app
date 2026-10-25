package com.kampplus.havali.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Hava durumunun uygulama içindeki sade sınıflandırması.
 *
 * Open-Meteo WMO 4677 kodu gönderir (0, 61, 95...). Ekran ve iş kuralları bu kodları
 * tek tek bilmez; [fromWmoCode] onları altı temel türe indirger.
 */
enum class WeatherKind {
    CLEAR,
    CLOUDY,
    FOG,
    RAIN,
    SNOW,
    STORM;

    /** Islak zemin üreten türler: yağmur ve fırtına. */
    val isWet: Boolean get() = this in WET_KINDS

    companion object {
        private val WET_KINDS = setOf(RAIN, STORM)

        fun fromWmoCode(code: Int): WeatherKind = when (code) {
            0, 1 -> CLEAR
            2, 3 -> CLOUDY
            45, 48 -> FOG
            in 51..67, in 80..82 -> RAIN
            in 71..77, 85, 86 -> SNOW
            95, 96, 99 -> STORM
            else -> CLOUDY
        }
    }
}

/** Belirli bir andaki hava. Hem "şimdi" hem de saatlik tahmin satırları bu modeli kullanır. */
data class WeatherSnapshot(
    val time: LocalDateTime,
    val temperatureC: Double,
    val apparentTemperatureC: Double,
    val kind: WeatherKind,
    val precipitationProbability: Int,
    val windKmh: Double,
    val gustKmh: Double,
    val uvIndex: Double,
    val humidityPercent: Int,
    val visibilityMeters: Double,
    val snowDepthCm: Double
)

/** Bir günün özeti: en düşük / en yüksek sıcaklık ve yağış olasılığı. */
data class DailyWeather(
    val date: LocalDate,
    val minTemperatureC: Double,
    val maxTemperatureC: Double,
    val kind: WeatherKind,
    val precipitationProbability: Int,
    val uvIndexMax: Double
) {
    val averageTemperatureC: Double get() = (minTemperatureC + maxTemperatureC) / 2
}

/** Bir noktanın anlık durumu, saatlik ve günlük tahmini. */
data class Forecast(
    val point: GeoPoint,
    val current: WeatherSnapshot,
    val hourly: List<WeatherSnapshot>,
    val daily: List<DailyWeather>
) {
    /**
     * Şimdiden [hoursAhead] saat sonraki hava. Harita altındaki zaman çubuğu bunu kullanır.
     * Tahmin o kadar ileriye uzanmıyorsa eldeki en yakın değer döner.
     */
    fun after(hoursAhead: Int): WeatherSnapshot = when (hoursAhead) {
        0 -> current
        else -> at(current.time.plusHours(hoursAhead.toLong()))
    }

    /** Belirli bir saatteki hava (plan saati gibi). Saatlik tahminin dışındaysa en yakın uç değer döner. */
    fun at(time: LocalDateTime): WeatherSnapshot {
        val target = time.truncatedTo(ChronoUnit.HOURS)
        return hourly.firstOrNull { !it.time.isBefore(target) } ?: hourly.lastOrNull() ?: current
    }

    /** Saatlik tahmin bu saati kapsıyor mu? (Open-Meteo saatlik veriyi sınırlı gün için verir.) */
    fun covers(time: LocalDateTime): Boolean = hourly.any { it.time == time.truncatedTo(ChronoUnit.HOURS) }

    /** Belirli bir günün özeti; tahmin o günü kapsamıyorsa null. */
    fun day(date: LocalDate): DailyWeather? = daily.firstOrNull { it.date == date }

    /** Şimdiden başlayarak [count] saatlik pencere. */
    fun nextHours(count: Int): List<WeatherSnapshot> {
        val from = current.time.truncatedTo(ChronoUnit.HOURS)
        return hourly.filter { !it.time.isBefore(from) }.take(count)
    }
}
