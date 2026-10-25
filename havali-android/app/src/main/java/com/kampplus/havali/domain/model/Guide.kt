package com.kampplus.havali.domain.model

/** Bir şehirde önerilen kamp alanı. Haritada çadır simgesiyle, yalnızca şehre yaklaşınca görünür. */
data class Campsite(val id: String, val name: String, val cityId: String, val point: GeoPoint)

/** Kamp alanı ve bugünkü havaya göre kamp kararı. */
data class CampingAdvice(val site: Campsite, val verdict: ActivityVerdict)

/** Önemli hava uyarıları. Sıra önemlidir: önce gelen daha ciddidir. */
enum class AlertType { STORM, WAVES, FOG, HEAVY_RAIN }

/** @param value Uyarının büyüklüğü: hamle km/sa, görüş metre veya yağış %. */
data class WeatherAlert(val type: AlertType, val placeName: String, val value: Int)

/**
 * Bir şehrin ana trafik aksı. [baseLoad] 0..1 arası olağan doluluğudur.
 * [alternativeId] doluyken önerilecek yan yolu, [isAlternative] bu aksın kendisinin bir yan yol olduğunu belirtir.
 */
data class TrafficCorridor(
    val id: String,
    val name: String,
    val cityId: String,
    val points: List<GeoPoint>,
    val baseLoad: Double,
    val alternativeId: String? = null,
    val isAlternative: Boolean = false
)

enum class TrafficLevel { LIGHT, MODERATE, HEAVY }

data class CorridorTraffic(val corridor: TrafficCorridor, val level: TrafficLevel, val delayMinutes: Int)

/** Yoğun bir aks için önerilen yan yol ve kazandırdığı süre. */
data class AlternativeRoute(val main: CorridorTraffic, val alternative: CorridorTraffic) {
    val savedMinutes: Int get() = main.delayMinutes - alternative.delayMinutes
}
