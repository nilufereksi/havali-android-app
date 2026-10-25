package com.kampplus.havali.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

/** Yol süresini uzatan etkenler. */
enum class DelayFactor { RAIN, RUSH_HOUR, COMMUNITY_REPORTS, LOW_VISIBILITY }

data class Delay(val factor: DelayFactor, val minutes: Int)

/**
 * "Akıllı çıkış önerisi". Tamamen cihazda, kural tabanlı hesaplanır; ücretli bir
 * trafik servisine ihtiyaç duymaz.
 */
data class DepartureAdvice(
    val leaveAt: LocalTime,
    val usualLeaveAt: LocalTime,
    val travelMinutes: Int,
    val usualMinutes: Int,
    val delays: List<Delay>
)

/** Sürüş riski seviyesi. [minScore] eşiği, ham risk puanını seviyeye çevirir. */
enum class RiskLevel(val minScore: Double) {
    VERY_LOW(0.0),
    LOW(1.0),
    MEDIUM(2.0),
    HIGH(3.0),
    VERY_HIGH(4.0);

    companion object {
        fun fromScore(score: Double): RiskLevel = entries.lastOrNull { score >= it.minScore } ?: VERY_LOW
    }
}

enum class HazardType { SLIPPERY_ROAD, STRONG_GUSTS, LOW_VISIBILITY, ICE, HEAVY_TRAFFIC }

/** @param value Tehlikenin büyüklüğü: yağış %, rüzgâr km/sa, görüş km, sıcaklık °C... */
data class Hazard(val type: HazardType, val value: Int)

data class HourRisk(val time: LocalDateTime, val level: RiskLevel)

data class RoadRisk(
    val mode: TravelMode,
    val level: RiskLevel,
    val hours: List<HourRisk>,
    val hazards: List<Hazard>
)

/** Topluluğun haritaya işaretlediği yol durumu türleri. */
enum class ReportType { PUDDLE, SLIPPERY_ROAD, ICE, FOG, FALLEN_TREE, ACCIDENT, ROADWORK }

/**
 * Sürücülerin bildirdiği yerel bir durum (Waze benzeri).
 *
 * @param confirmations Kaç kişinin "hâlâ var" dediği.
 * @param mine Raporu bu kullanıcının kendisi mi eklediği.
 */
data class CommunityReport(
    val id: String,
    val type: ReportType,
    val placeName: String,
    val point: GeoPoint,
    val minutesAgo: Int,
    val confirmations: Int,
    val confirmedByMe: Boolean = false,
    val mine: Boolean = false
)

/** Bir noktanın çevresindeki rapor sayısı: çıkış tahmininde "yoldaki raporlar" olarak kullanılır. */
fun List<CommunityReport>.countNear(point: GeoPoint, radiusKm: Double = ROUTE_RADIUS_KM): Int =
    count { it.point.distanceKmTo(point) <= radiusKm }

private const val ROUTE_RADIUS_KM = 2.5
