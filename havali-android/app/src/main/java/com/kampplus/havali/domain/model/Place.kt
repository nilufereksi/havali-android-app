package com.kampplus.havali.domain.model

import kotlin.math.cos
import kotlin.math.sqrt

/** Dünya üzerindeki bir nokta. Enlem (latitude) kuzey-güney, boylam (longitude) doğu-batı. */
data class GeoPoint(val latitude: Double, val longitude: Double) {

    /**
     * İki nokta arası yaklaşık mesafe (km). Şehir ölçeğinde eşdikdörtgen yaklaşım
     * yeterince doğrudur ve haversine'den çok daha basittir.
     */
    fun distanceKmTo(other: GeoPoint): Double {
        val meanLatitude = Math.toRadians((latitude + other.latitude) / 2)
        val dx = Math.toRadians(other.longitude - longitude) * cos(meanLatitude)
        val dy = Math.toRadians(other.latitude - latitude)
        return sqrt(dx * dx + dy * dy) * EARTH_RADIUS_KM
    }

    private companion object {
        const val EARTH_RADIUS_KM = 6371.0
    }
}

/** Bir yerin hangi tür aktiviteye uygun olduğunu belirleyen etiketler. */
enum class PlaceTag {
    COASTAL,
    MOUNTAIN,
    URBAN,

    /** Odak yer (ör. Gemlik): ilçe olsa da Türkiye görünümünde de etiketlenir. */
    FEATURED
}

/**
 * Haritanın bir yere ne kadar yaklaşacağı.
 *
 * [level], 390 dp genişliğindeki tasarım ekranında harita birimi başına düşen dp'dir;
 * gerçek ekranda genişliğe göre ölçeklenir (responsive).
 */
enum class MapZoom(val level: Float) {
    COUNTRY(0.95f),
    CITY(6f),
    REGION(10f),
    DISTRICT(22f),
    STREET(60f)
}

/**
 * Haritada gösterilebilen, aranabilen ve hava durumu sorulabilen bir yer.
 *
 * @param region Üst bölge adı ("İzmir" gibi); arama sonucunda alt satırda gösterilir.
 * @param isHome Kullanıcının yaşadığı yer; açılışta harita buraya yaklaşır.
 */
data class Place(
    val id: String,
    val name: String,
    val region: String,
    val point: GeoPoint,
    val zoom: MapZoom,
    val tags: Set<PlaceTag> = emptySet(),
    val isHome: Boolean = false
)
