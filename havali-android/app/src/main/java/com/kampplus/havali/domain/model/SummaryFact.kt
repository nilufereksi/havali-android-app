package com.kampplus.havali.domain.model

import java.time.LocalTime

/**
 * "Günün özeti" kartının cümleleri. Domain yalnızca olguyu ve sayıyı üretir;
 * Türkçe cümleye çevirmek sunum katmanının işidir (strings.xml).
 */
sealed interface SummaryFact {
    data class RainNow(val probability: Int) : SummaryFact

    data class RainArrives(val at: LocalTime, val probability: Int) : SummaryFact

    data object StaysDry : SummaryFact

    data class StrongGusts(val gustKmh: Int) : SummaryFact

    data class FreshSnow(val depthCm: Int) : SummaryFact

    data class PersonalFeel(val actualC: Int, val personalC: Int) : SummaryFact

    data class HighUv(val index: Int) : SummaryFact
}
