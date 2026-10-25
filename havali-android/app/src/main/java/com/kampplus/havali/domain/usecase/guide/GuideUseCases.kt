package com.kampplus.havali.domain.usecase.guide

import com.kampplus.havali.domain.model.ActivityReason
import com.kampplus.havali.domain.model.ActivityVerdict
import com.kampplus.havali.domain.model.AlertType
import com.kampplus.havali.domain.model.AlternativeRoute
import com.kampplus.havali.domain.model.CampingAdvice
import com.kampplus.havali.domain.model.Campsite
import com.kampplus.havali.domain.model.CorridorTraffic
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PlaceTag
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.TrafficCorridor
import com.kampplus.havali.domain.model.TrafficLevel
import com.kampplus.havali.domain.model.WeatherAlert
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.domain.usecase.activity.Checks
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * "Kamp için hava uygun mu?" Diğer aktivitelerle aynı küçük kontrolleri kullanır:
 * yağış, rüzgâr hamlesi ve gece serinliği. En kötü sonuç kararı belirler.
 */
class EvaluateCampingUseCase {

    private val checks = listOf(Checks.maxRain(RAIN_LIMIT), Checks.maxGust(GUST_LIMIT_KMH), Checks.minFeelsLike(COLD_LIMIT_C))

    operator fun invoke(sites: List<Campsite>, weather: WeatherSnapshot): List<CampingAdvice> = sites.map { site ->
        val verdict = checks.mapNotNull { it.evaluate(weather) }.maxByOrNull { it.suitability.severity }
            ?: ActivityVerdict(Suitability.GOOD, ActivityReason.MILD_AND_DRY, weather.apparentTemperatureC.roundToInt())
        CampingAdvice(site, verdict)
    }

    private companion object {
        const val RAIN_LIMIT = 30
        const val GUST_LIMIT_KMH = 40.0
        const val COLD_LIMIT_C = 6.0
    }
}

/**
 * Önümüzdeki saatlerde fırtına, dalga, sis ve kuvvetli yağış uyarıları.
 * Dalga uyarısı yalnızca kıyı yerleri için verilir ("Alsancak'ta Kordon'a çıkacaksan dikkat").
 */
class DetectAlertsUseCase {

    operator fun invoke(place: Place, hours: List<WeatherSnapshot>): List<WeatherAlert> {
        val maxGust = hours.maxOfOrNull { it.gustKmh } ?: 0.0
        val minVisibility = hours.minOfOrNull { it.visibilityMeters } ?: Double.MAX_VALUE
        val maxRain = hours.maxOfOrNull { it.precipitationProbability } ?: 0
        val stormy = maxGust >= STORM_GUST_KMH || hours.any { it.kind == WeatherKind.STORM }
        return listOfNotNull(
            WeatherAlert(AlertType.STORM, place.name, maxGust.roundToInt()).takeIf { stormy },
            WeatherAlert(AlertType.WAVES, place.name, maxGust.roundToInt())
                .takeIf { PlaceTag.COASTAL in place.tags && maxGust >= WAVE_GUST_KMH },
            WeatherAlert(AlertType.FOG, place.name, minVisibility.roundToInt()).takeIf { minVisibility < FOG_METERS },
            WeatherAlert(AlertType.HEAVY_RAIN, place.name, maxRain).takeIf { maxRain >= HEAVY_RAIN_PERCENT }
        )
    }

    private companion object {
        const val STORM_GUST_KMH = 50.0
        const val WAVE_GUST_KMH = 35.0
        const val FOG_METERS = 1000.0
        const val HEAVY_RAIN_PERCENT = 80
    }
}

/**
 * Trafik tahmini (kural tabanlı): aksın olağan doluluğuna iş çıkış saatleri,
 * yağış, sis ve aks üzerindeki topluluk raporları eklenir.
 */
class PredictTrafficUseCase {

    operator fun invoke(corridor: TrafficCorridor, time: LocalDateTime, weather: WeatherSnapshot, reportsOnCorridor: Int): CorridorTraffic {
        val load = corridor.baseLoad +
            RUSH_LOAD.takeIf { time.hour in MORNING_RUSH || time.hour in EVENING_RUSH }.orZero() +
            RAIN_LOAD.takeIf { weather.precipitationProbability >= RAIN_PERCENT || weather.kind.isWet }.orZero() +
            FOG_LOAD.takeIf { weather.visibilityMeters < FOG_METERS }.orZero() +
            reportsOnCorridor * REPORT_LOAD
        val level = when {
            load >= HEAVY_FROM -> TrafficLevel.HEAVY
            load >= MODERATE_FROM -> TrafficLevel.MODERATE
            else -> TrafficLevel.LIGHT
        }
        return CorridorTraffic(corridor, level, (load * MINUTES_PER_LOAD).roundToInt())
    }

    private fun Double?.orZero(): Double = this ?: 0.0

    private companion object {
        val MORNING_RUSH = 7..9
        val EVENING_RUSH = 17..19
        const val RUSH_LOAD = 0.4
        const val RAIN_LOAD = 0.25
        const val FOG_LOAD = 0.15
        const val REPORT_LOAD = 0.1
        const val RAIN_PERCENT = 50
        const val FOG_METERS = 2000.0
        const val MODERATE_FROM = 0.55
        const val HEAVY_FROM = 0.85
        const val MINUTES_PER_LOAD = 14
    }
}

/** Yoğun ya da tıkalı bir aksın yan yolu daha az gecikiyorsa alternatif rota olarak önerilir. */
class SuggestAlternativesUseCase {

    operator fun invoke(traffic: List<CorridorTraffic>): List<AlternativeRoute> = traffic
        .filter { it.level != TrafficLevel.LIGHT }
        .mapNotNull { main ->
            traffic.firstOrNull { it.corridor.id == main.corridor.alternativeId }
                ?.takeIf { it.delayMinutes < main.delayMinutes }
                ?.let { AlternativeRoute(main, it) }
        }
}
