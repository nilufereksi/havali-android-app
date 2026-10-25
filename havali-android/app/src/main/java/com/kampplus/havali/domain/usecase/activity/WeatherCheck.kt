package com.kampplus.havali.domain.usecase.activity

import com.kampplus.havali.domain.model.ActivityReason
import com.kampplus.havali.domain.model.ActivityVerdict
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.WeatherSnapshot
import kotlin.math.roundToInt

/**
 * Havaya uygulanan tek bir kontrol.
 *
 * Kontrol bir sorun (veya dikkat çekici bir durum) bulursa bir [ActivityVerdict] döner,
 * bulmazsa null. Aktivite kuralları bu küçük kontrollerin listesidir; böylece
 * "rüzgâr şundan fazlaysa, yağış şundan fazlaysa..." gibi uzun if zincirleri yazılmaz.
 */
fun interface WeatherCheck {
    fun evaluate(weather: WeatherSnapshot): ActivityVerdict?
}

/** Yeniden kullanılabilir kontrol fabrikası. `takeIf` koşul sağlanınca sonucu, sağlanmazsa null döner. */
object Checks {

    fun maxGust(limitKmh: Double, level: Suitability = Suitability.BAD) = WeatherCheck { weather ->
        ActivityVerdict(level, ActivityReason.WIND_TOO_STRONG, weather.gustKmh.roundToInt())
            .takeIf { weather.gustKmh > limitKmh }
    }

    fun minWind(minKmh: Double) = WeatherCheck { weather ->
        ActivityVerdict(Suitability.CAUTION, ActivityReason.WIND_TOO_WEAK, weather.windKmh.roundToInt())
            .takeIf { weather.windKmh < minKmh }
    }

    fun maxRain(limitPercent: Int, level: Suitability = Suitability.BAD) = WeatherCheck { weather ->
        ActivityVerdict(level, ActivityReason.RAIN_LIKELY, weather.precipitationProbability)
            .takeIf { weather.precipitationProbability > limitPercent || weather.kind.isWet }
    }

    fun minFeelsLike(limitC: Double, level: Suitability = Suitability.CAUTION) = WeatherCheck { weather ->
        ActivityVerdict(level, ActivityReason.TOO_COLD, weather.apparentTemperatureC.roundToInt())
            .takeIf { weather.apparentTemperatureC < limitC }
    }

    fun maxFeelsLike(limitC: Double) = WeatherCheck { weather ->
        ActivityVerdict(Suitability.CAUTION, ActivityReason.TOO_HOT, weather.apparentTemperatureC.roundToInt())
            .takeIf { weather.apparentTemperatureC > limitC }
    }

    fun needsSnow(minCm: Double) = WeatherCheck { weather ->
        ActivityVerdict(Suitability.BAD, ActivityReason.NO_SNOW, weather.snowDepthCm.roundToInt())
            .takeIf { weather.snowDepthCm < minCm }
    }

    fun icyGround() = WeatherCheck { weather ->
        ActivityVerdict(Suitability.BAD, ActivityReason.ICY_GROUND, weather.temperatureC.roundToInt())
            .takeIf { weather.temperatureC <= ICE_THRESHOLD_C }
    }

    fun minVisibility(meters: Double) = WeatherCheck { weather ->
        ActivityVerdict(Suitability.CAUTION, ActivityReason.LOW_VISIBILITY, (weather.visibilityMeters / 1000).roundToInt())
            .takeIf { weather.visibilityMeters < meters }
    }

    /** Kapalı mekân önerisi yalnızca dışarısı kötüyken öne çıkar. */
    fun outdoorIsNice() = WeatherCheck { weather ->
        ActivityVerdict(Suitability.CAUTION, ActivityReason.OUTDOOR_BETTER, weather.temperatureC.roundToInt())
            .takeIf { !weather.kind.isWet && weather.precipitationProbability < NICE_RAIN_LIMIT }
    }

    private const val ICE_THRESHOLD_C = 1.0
    private const val NICE_RAIN_LIMIT = 40
}
