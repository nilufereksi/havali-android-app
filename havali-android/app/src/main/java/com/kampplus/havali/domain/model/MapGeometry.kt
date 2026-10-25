package com.kampplus.havali.domain.model

/**
 * Haritanın çizim katmanları.
 *
 * Ulusal katmanlar (COUNTRY, PROVINCES) uzaktan bakışta, DETAIL katmanları şehre
 * yaklaşınca gösterilir. [closed] true ise halka kapalı bir alandır (kara), false ise
 * açık bir çizgidir (kıyı, ilçe sınırı).
 */
enum class MapLayer(val closed: Boolean) {
    NEIGHBORS(closed = true),
    COUNTRY(closed = true),
    PROVINCES(closed = true),
    DETAIL_LAND(closed = true),
    DETAIL_COAST(closed = false),
    DETAIL_DISTRICTS(closed = false)
}

/** Harita biriminde bir nokta. */
data class BasePoint(val x: Float, val y: Float)

/**
 * Harita geometrisi: her katman için halkaların listesi.
 * Her halka `x0, y0, x1, y1, ...` biçiminde düz bir sayı dizisidir (bellekte en ucuz biçim).
 */
class MapGeometry(val layers: Map<MapLayer, List<FloatArray>>)

/**
 * Enlem/boylamı harita birimine çeviren basit eşdikdörtgen projeksiyon.
 *
 * Türkiye enleminde boylam dereceleri kısaldığı için x ekseni cos(39°) ile
 * sıkıştırılmıştır. Harita varlık dosyası (assets/map) aynı formülle üretildi.
 */
object MapProjection {
    private const val LON_ORIGIN = 25.6
    private const val LAT_ORIGIN = 42.4
    private const val UNITS_PER_LON_DEGREE = 20.2
    private const val UNITS_PER_LAT_DEGREE = 26.0

    /** Türkiye'nin harita birimindeki sınırları: uzaktan bakışta ekrana sığdırmak için. */
    const val TURKEY_MIN_X = 0f
    const val TURKEY_MAX_X = 390f
    const val TURKEY_MIN_Y = 5f
    const val TURKEY_MAX_Y = 168f

    fun toBase(point: GeoPoint): BasePoint = BasePoint(
        x = ((point.longitude - LON_ORIGIN) * UNITS_PER_LON_DEGREE).toFloat(),
        y = ((LAT_ORIGIN - point.latitude) * UNITS_PER_LAT_DEGREE).toFloat()
    )
}

/** Avrupa hava kalitesi indeksine göre seviye. */
enum class AirQualityLevel {
    GOOD,
    FAIR,
    MODERATE,
    POOR,
    UNKNOWN;

    companion object {
        fun fromEuropeanAqi(aqi: Int?): AirQualityLevel = when {
            aqi == null -> UNKNOWN
            aqi <= 20 -> GOOD
            aqi <= 40 -> FAIR
            aqi <= 60 -> MODERATE
            else -> POOR
        }
    }
}

enum class PollenLevel {
    LOW,
    MODERATE,
    HIGH,
    UNKNOWN;

    companion object {
        fun fromGrainsPerCubicMeter(value: Double?): PollenLevel = when {
            value == null -> UNKNOWN
            value < 20 -> LOW
            value < 80 -> MODERATE
            else -> HIGH
        }
    }
}

data class AirQuality(val level: AirQualityLevel, val europeanAqi: Int?, val pollen: PollenLevel)

/** Havaya göre cilt / sağlık ipuçları. */
enum class CareTip { HUMID_SKIN, DRY_SKIN, WINDY_LIPS, HIGH_UV, COLD_HANDS }

data class CareAdvice(val tip: CareTip, val value: Int)

/** Hava verisinin nereden geldiği: canlı servis mi, demo senaryosu mu. */
enum class DataSource { LIVE, DEMO }
