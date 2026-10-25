package com.kampplus.havali.domain.usecase.health

import com.kampplus.havali.domain.model.CareAdvice
import com.kampplus.havali.domain.model.CareTip
import com.kampplus.havali.domain.model.ComfortFeedback
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.domain.repository.ComfortRepository
import kotlin.math.roundToInt

/**
 * "Dün nasıldı? Üşüdüm / İyiydi / Terledim" cevabını konfor profiline işler.
 * Profil kaydedildiği için uygulama kapanıp açılsa da öğrenilen bilgi kaybolmaz.
 */
class RecordComfortFeedbackUseCase(private val repository: ComfortRepository) {

    operator fun invoke(feedback: ComfortFeedback): ComfortProfile {
        val updated = repository.profile.value.record(feedback)
        repository.save(updated)
        return updated
    }
}

/** Havaya göre cilt ve sağlık ipuçları. Her ipucu kendi koşuluyla, if zinciri olmadan seçilir. */
class BuildCareTipsUseCase {

    operator fun invoke(weather: WeatherSnapshot, profile: ComfortProfile): List<CareAdvice> {
        val personal = profile.feelsLikeFor(weather.apparentTemperatureC)
        return listOfNotNull(
            CareAdvice(CareTip.HUMID_SKIN, weather.humidityPercent).takeIf { weather.humidityPercent >= HUMID_PERCENT },
            CareAdvice(CareTip.DRY_SKIN, weather.humidityPercent).takeIf { weather.humidityPercent <= DRY_PERCENT },
            CareAdvice(CareTip.WINDY_LIPS, weather.gustKmh.roundToInt()).takeIf { weather.gustKmh >= WINDY_KMH },
            CareAdvice(CareTip.HIGH_UV, weather.uvIndex.roundToInt()).takeIf { weather.uvIndex >= HIGH_UV },
            CareAdvice(CareTip.COLD_HANDS, personal.roundToInt()).takeIf { personal <= COLD_C }
        )
    }

    private companion object {
        const val HUMID_PERCENT = 75
        const val DRY_PERCENT = 35
        const val WINDY_KMH = 25.0
        const val HIGH_UV = 6.0
        const val COLD_C = 5.0
    }
}
