package com.kampplus.havali.domain.usecase.road

import com.kampplus.havali.domain.model.Hazard
import com.kampplus.havali.domain.model.HazardType
import com.kampplus.havali.domain.model.HourRisk
import com.kampplus.havali.domain.model.RiskLevel
import com.kampplus.havali.domain.model.RoadRisk
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherSnapshot
import kotlin.math.roundToInt

/**
 * Seçilen ulaşım şekli için saat saat yol riski.
 *
 * Her saatin ham puanı yağış, rüzgâr hamlesi, buzlanma ve görüşten toplanır; sonra
 * modun hassasiyetiyle çarpılır (motor ıslak zemine yayadan çok daha hassastır).
 * Puan [RiskLevel.fromScore] ile seviyeye çevrilir.
 */
class AssessRoadRiskUseCase {

    operator fun invoke(mode: TravelMode, hours: List<WeatherSnapshot>): RoadRisk {
        val hourRisks = hours.map { HourRisk(it.time, RiskLevel.fromScore(score(it, mode))) }
        return RoadRisk(
            mode = mode,
            level = hourRisks.maxByOrNull { it.level.ordinal }?.level ?: RiskLevel.VERY_LOW,
            hours = hourRisks,
            hazards = hazards(mode, hours)
        )
    }

    private fun score(weather: WeatherSnapshot, mode: TravelMode): Double {
        val rain = weather.precipitationProbability / RAIN_DIVISOR
        val gusts = weather.gustKmh / GUST_DIVISOR
        val ice = when {
            weather.temperatureC <= ICE_C -> ICE_SCORE
            else -> 0.0
        }
        val fog = when {
            weather.visibilityMeters < FOG_METERS -> FOG_SCORE
            else -> 0.0
        }
        return ((rain + gusts + ice + fog) * mode.riskSensitivity).coerceAtMost(MAX_SCORE)
    }

    private fun hazards(mode: TravelMode, hours: List<WeatherSnapshot>): List<Hazard> {
        val maxRain = hours.maxOfOrNull { it.precipitationProbability } ?: 0
        val maxGust = hours.maxOfOrNull { it.gustKmh } ?: 0.0
        val minVisibility = hours.minOfOrNull { it.visibilityMeters } ?: Double.MAX_VALUE
        val minTemperature = hours.minOfOrNull { it.temperatureC } ?: 0.0
        return listOfNotNull(
            Hazard(HazardType.SLIPPERY_ROAD, maxRain).takeIf { maxRain >= SLIPPERY_RAIN },
            Hazard(HazardType.STRONG_GUSTS, maxGust.roundToInt()).takeIf { maxGust >= STRONG_GUST },
            Hazard(HazardType.LOW_VISIBILITY, (minVisibility / METERS_PER_KM).roundToInt()).takeIf { minVisibility < LOW_VISIBILITY },
            Hazard(HazardType.ICE, minTemperature.roundToInt()).takeIf { minTemperature <= ICE_C },
            Hazard(HazardType.HEAVY_TRAFFIC, TRAFFIC_PERCENT).takeIf { mode in TRAFFIC_MODES && maxRain >= SLIPPERY_RAIN }
        )
    }

    private companion object {
        const val RAIN_DIVISOR = 30.0
        const val GUST_DIVISOR = 25.0
        const val ICE_C = 1.0
        const val ICE_SCORE = 2.0
        const val FOG_METERS = 1000.0
        const val FOG_SCORE = 1.0
        const val MAX_SCORE = 5.0
        const val SLIPPERY_RAIN = 50
        const val STRONG_GUST = 35.0
        const val LOW_VISIBILITY = 2000.0
        const val METERS_PER_KM = 1000.0
        const val TRAFFIC_PERCENT = 40
        val TRAFFIC_MODES = setOf(TravelMode.CAR, TravelMode.TRANSIT)
    }
}
