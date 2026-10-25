package com.kampplus.havali.domain.usecase.plan

import com.kampplus.havali.domain.model.Delay
import com.kampplus.havali.domain.model.DelayFactor
import com.kampplus.havali.domain.model.DepartureAdvice
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.WeatherSnapshot
import kotlin.math.roundToInt

/**
 * "Kaçta çıkmalıyım?" sorusunu kural tabanlı cevaplar.
 *
 * Normal süre = mesafe / modun ortalama hızı + hazırlık payı.
 * Üzerine dört etkenin her biri ayrı bir küçük fonksiyonla eklenir:
 * yağış, akşam yoğunluğu, topluluk raporları ve düşük görüş.
 * Canlı trafik servisi gerektirmez, tamamen cihazda hesaplanır.
 */
class EstimateDepartureUseCase {

    operator fun invoke(plan: Plan, weatherAtArrival: WeatherSnapshot, reportsOnRoute: Int): DepartureAdvice {
        val usualMinutes = (plan.distanceKm / plan.mode.averageKmh * MINUTES_PER_HOUR).roundToInt() + OVERHEAD_MINUTES
        val delays = listOfNotNull(
            rainDelay(usualMinutes, weatherAtArrival),
            rushHourDelay(usualMinutes, plan.start.hour),
            reportDelay(reportsOnRoute),
            visibilityDelay(weatherAtArrival)
        )
        val travelMinutes = usualMinutes + delays.sumOf { it.minutes }
        return DepartureAdvice(
            leaveAt = plan.start.minusMinutes((travelMinutes + BUFFER_MINUTES).toLong()).toLocalTime(),
            usualLeaveAt = plan.start.minusMinutes((usualMinutes + BUFFER_MINUTES).toLong()).toLocalTime(),
            travelMinutes = travelMinutes,
            usualMinutes = usualMinutes,
            delays = delays
        )
    }

    private fun rainDelay(usualMinutes: Int, weather: WeatherSnapshot): Delay? {
        val ratio = when {
            weather.precipitationProbability >= HEAVY_RAIN_PERCENT -> HEAVY_RAIN_RATIO
            weather.precipitationProbability >= LIGHT_RAIN_PERCENT -> LIGHT_RAIN_RATIO
            else -> 0.0
        }
        return Delay(DelayFactor.RAIN, (usualMinutes * ratio).roundToInt()).takeIf { it.minutes > 0 }
    }

    private fun rushHourDelay(usualMinutes: Int, hour: Int): Delay? =
        Delay(DelayFactor.RUSH_HOUR, (usualMinutes * RUSH_HOUR_RATIO).roundToInt())
            .takeIf { hour in RUSH_HOURS }

    private fun reportDelay(reports: Int): Delay? =
        Delay(DelayFactor.COMMUNITY_REPORTS, reports * MINUTES_PER_REPORT).takeIf { reports > 0 }

    private fun visibilityDelay(weather: WeatherSnapshot): Delay? =
        Delay(DelayFactor.LOW_VISIBILITY, FOG_MINUTES).takeIf { weather.visibilityMeters < FOG_VISIBILITY_METERS }

    private companion object {
        const val MINUTES_PER_HOUR = 60
        const val OVERHEAD_MINUTES = 7
        const val BUFFER_MINUTES = 10
        const val HEAVY_RAIN_PERCENT = 60
        const val LIGHT_RAIN_PERCENT = 30
        const val HEAVY_RAIN_RATIO = 0.35
        const val LIGHT_RAIN_RATIO = 0.15
        const val RUSH_HOUR_RATIO = 0.25
        const val MINUTES_PER_REPORT = 3
        const val FOG_MINUTES = 5
        const val FOG_VISIBILITY_METERS = 1000.0
        val RUSH_HOURS = 17..19
    }
}
