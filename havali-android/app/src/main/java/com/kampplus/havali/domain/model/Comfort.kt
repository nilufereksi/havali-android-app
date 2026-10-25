package com.kampplus.havali.domain.model

/** Kullanıcının dışarıdan dönünce verdiği tek dokunuşluk geri bildirim. */
enum class ComfortFeedback(val deltaC: Double) {
    COLD(-3.0),
    OK(0.0),
    HOT(3.0)
}

enum class ComfortSensitivity { FEELS_COLD, NEUTRAL, FEELS_WARM }

/**
 * "Sana göre hissedilen": kullanıcının sıcaklığa kişisel hassasiyeti.
 *
 * Her geri bildirim kayan bir ortalamaya eklenir. [offsetC] negatifse kullanıcı
 * ortalamadan daha çabuk üşüyordur; önerilen sıcaklık o kadar düşük hesaplanır.
 */
data class ComfortProfile(val offsetC: Double = 0.0, val samples: Int = 0) {

    val sensitivity: ComfortSensitivity
        get() = when {
            offsetC <= -SENSITIVITY_THRESHOLD -> ComfortSensitivity.FEELS_COLD
            offsetC >= SENSITIVITY_THRESHOLD -> ComfortSensitivity.FEELS_WARM
            else -> ComfortSensitivity.NEUTRAL
        }

    fun feelsLikeFor(apparentTemperatureC: Double): Double = apparentTemperatureC + offsetC

    fun record(feedback: ComfortFeedback): ComfortProfile {
        val count = samples + 1
        val average = (offsetC * samples + feedback.deltaC) / count
        return ComfortProfile(offsetC = average.coerceIn(-MAX_OFFSET, MAX_OFFSET), samples = count)
    }

    private companion object {
        const val SENSITIVITY_THRESHOLD = 1.0
        const val MAX_OFFSET = 5.0
    }
}
