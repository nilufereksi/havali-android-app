package com.kampplus.havali.domain.usecase.activity

import com.kampplus.havali.domain.model.ActivityReason
import com.kampplus.havali.domain.model.ActivityType
import com.kampplus.havali.domain.model.ActivityVerdict
import com.kampplus.havali.domain.model.PlaceTag
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.WeatherSnapshot
import kotlin.math.roundToInt

/**
 * Her aktivitenin kuralları tek yerde.
 *
 * Yeni bir aktivite eklemek için: [ActivityType]'a sabit ekle, buradaki üç `when`'e birer dal yaz.
 * `when` enum üzerinde tam (exhaustive) olduğu için bir dalı unutursan derleme hatası alırsın.
 */
object ActivityRules {

    /** Aktivite yalnızca bu etiketlere sahip yerlerde önerilir (SUP için deniz, kayak için dağ). */
    fun requiredTags(type: ActivityType): Set<PlaceTag> = when (type) {
        ActivityType.WINDSURF,
        ActivityType.KITESURF,
        ActivityType.SUP,
        ActivityType.KAYAK,
        ActivityType.SEA_SWIMMING -> setOf(PlaceTag.COASTAL)
        ActivityType.SKIING, ActivityType.SNOWBOARD -> setOf(PlaceTag.MOUNTAIN)
        ActivityType.HIKING,
        ActivityType.RUNNING,
        ActivityType.CYCLING,
        ActivityType.INDOOR_POOL -> emptySet()
    }

    /** Sırayla uygulanan kontroller. En kötü sonuç aktivitenin kararını belirler. */
    fun checks(type: ActivityType): List<WeatherCheck> = when (type) {
        ActivityType.WINDSURF -> listOf(Checks.minWind(18.0), Checks.maxGust(65.0), Checks.maxRain(70, Suitability.CAUTION))
        ActivityType.KITESURF -> listOf(Checks.minWind(20.0), Checks.maxGust(55.0), Checks.maxRain(60, Suitability.CAUTION))
        ActivityType.SUP -> listOf(Checks.maxGust(25.0), Checks.maxRain(50), Checks.minFeelsLike(15.0), Checks.minVisibility(1000.0))
        ActivityType.KAYAK -> listOf(Checks.maxGust(30.0), Checks.maxRain(50), Checks.minFeelsLike(12.0), Checks.minVisibility(1000.0))
        ActivityType.SKIING,
        ActivityType.SNOWBOARD -> listOf(Checks.needsSnow(10.0), Checks.maxGust(60.0), Checks.minVisibility(500.0))
        ActivityType.SEA_SWIMMING -> listOf(Checks.minFeelsLike(22.0), Checks.maxGust(30.0, Suitability.CAUTION), Checks.maxRain(40))
        ActivityType.HIKING -> listOf(Checks.maxRain(60), Checks.maxGust(50.0, Suitability.CAUTION), Checks.minVisibility(1000.0))
        ActivityType.RUNNING -> listOf(Checks.maxRain(60, Suitability.CAUTION), Checks.maxFeelsLike(32.0), Checks.icyGround())
        ActivityType.CYCLING -> listOf(Checks.maxRain(40), Checks.maxGust(40.0, Suitability.CAUTION), Checks.icyGround())
        ActivityType.INDOOR_POOL -> listOf(Checks.outdoorIsNice())
    }

    /** Hiçbir kontrol takılmadıysa aktivitenin olumlu gerekçesi. */
    fun positiveVerdict(type: ActivityType, weather: WeatherSnapshot): ActivityVerdict = when (type) {
        ActivityType.WINDSURF, ActivityType.KITESURF -> good(ActivityReason.GOOD_WIND, weather.windKmh)
        ActivityType.SUP, ActivityType.KAYAK -> good(ActivityReason.CALM_WATER, weather.gustKmh)
        ActivityType.SKIING, ActivityType.SNOWBOARD -> good(ActivityReason.FRESH_SNOW, weather.snowDepthCm)
        ActivityType.SEA_SWIMMING,
        ActivityType.HIKING,
        ActivityType.RUNNING,
        ActivityType.CYCLING -> good(ActivityReason.MILD_AND_DRY, weather.apparentTemperatureC)
        ActivityType.INDOOR_POOL -> good(ActivityReason.INDOOR_BEST, weather.precipitationProbability.toDouble())
    }

    fun evaluate(type: ActivityType, weather: WeatherSnapshot): ActivityVerdict = checks(type)
        .mapNotNull { it.evaluate(weather) }
        .maxByOrNull { it.suitability.severity }
        ?: positiveVerdict(type, weather)

    private fun good(reason: ActivityReason, value: Double) = ActivityVerdict(Suitability.GOOD, reason, value.roundToInt())
}
