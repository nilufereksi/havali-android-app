package com.kampplus.havali.domain.model

/** Önerilebilen aktiviteler. Sıra önemlidir: eşit uygunlukta önce gelen öne çıkar. */
enum class ActivityType {
    WINDSURF,
    KITESURF,
    SUP,
    KAYAK,
    SKIING,
    SNOWBOARD,
    SEA_SWIMMING,
    HIKING,
    RUNNING,
    CYCLING,
    INDOOR_POOL
}

/** Bir aktivitenin bugünkü havaya uygunluğu. [severity] büyüdükçe durum kötüleşir. */
enum class Suitability(val severity: Int) {
    GOOD(0),
    CAUTION(1),
    BAD(2)
}

/** Kararın gerekçesi. Ekran bunu Türkçe cümleye çevirir; domain metin bilmez. */
enum class ActivityReason {
    INDOOR_BEST,
    OUTDOOR_BETTER,
    MILD_AND_DRY,
    CALM_WATER,
    GOOD_WIND,
    WIND_TOO_WEAK,
    WIND_TOO_STRONG,
    RAIN_LIKELY,
    TOO_COLD,
    TOO_HOT,
    FRESH_SNOW,
    NO_SNOW,
    ICY_GROUND,
    LOW_VISIBILITY
}

/** @param value Gerekçeyi destekleyen sayı (km/sa, %, °C, cm...). */
data class ActivityVerdict(
    val suitability: Suitability,
    val reason: ActivityReason,
    val value: Int = 0
)

data class ActivityAssessment(val type: ActivityType, val verdict: ActivityVerdict)

/** "Bugün ne yapsam?" bölümünün içeriği: öne çıkan öneri ve diğerleri. */
data class ActivityPlan(val best: ActivityAssessment, val others: List<ActivityAssessment>)
