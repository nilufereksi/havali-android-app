package com.kampplus.havali.domain.usecase.summary

import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.SummaryFact
import com.kampplus.havali.domain.model.WeatherSnapshot
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Önümüzdeki saatlerden kısa bir gün özeti çıkarır.
 *
 * Her cümle kendi koşuluyla seçilir (`takeIf`); yağmur cümlesi üç durumdan biridir
 * ve tek bir `when` ile belirlenir.
 */
class SummarizeDayUseCase {

    operator fun invoke(forecast: Forecast, profile: ComfortProfile): List<SummaryFact> {
        val window = forecast.nextHours(WINDOW_HOURS).ifEmpty { listOf(forecast.current) }
        val coldest = window.minBy { it.apparentTemperatureC }
        val personal = profile.feelsLikeFor(coldest.apparentTemperatureC)
        val maxGust = window.maxOf { it.gustKmh }
        val maxUv = window.maxOf { it.uvIndex }
        return listOfNotNull(
            rainFact(forecast.current, window),
            SummaryFact.StrongGusts(maxGust.roundToInt()).takeIf { maxGust >= STRONG_GUST_KMH },
            SummaryFact.FreshSnow(forecast.current.snowDepthCm.roundToInt()).takeIf { forecast.current.snowDepthCm >= SNOW_CM },
            SummaryFact.PersonalFeel(coldest.temperatureC.roundToInt(), personal.roundToInt())
                .takeIf { abs(profile.offsetC) >= PERSONAL_OFFSET_C },
            SummaryFact.HighUv(maxUv.roundToInt()).takeIf { maxUv >= HIGH_UV }
        )
    }

    private fun rainFact(current: WeatherSnapshot, window: List<WeatherSnapshot>): SummaryFact {
        val firstWet = window.firstOrNull { it.isRainy() }
        return when {
            current.isRainy() -> SummaryFact.RainNow(current.precipitationProbability)
            firstWet != null -> SummaryFact.RainArrives(firstWet.time.toLocalTime(), firstWet.precipitationProbability)
            else -> SummaryFact.StaysDry
        }
    }

    private fun WeatherSnapshot.isRainy() = kind.isWet || precipitationProbability >= RAIN_PERCENT

    private companion object {
        const val WINDOW_HOURS = 12
        const val RAIN_PERCENT = 50
        const val STRONG_GUST_KMH = 35.0
        const val SNOW_CM = 5.0
        const val PERSONAL_OFFSET_C = 1.0
        const val HIGH_UV = 5.0
    }
}
