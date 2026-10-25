package com.kampplus.havali.presentation.today.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.kampplus.havali.domain.model.BasePoint
import com.kampplus.havali.domain.model.MapProjection
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place

/** Kameranın gitmek istediği yer: merkez (harita birimi) ve yakınlık. */
data class CameraTarget(val center: BasePoint, val zoom: Float) {
    companion object {
        val Turkey = CameraTarget(
            center = BasePoint(
                x = (MapProjection.TURKEY_MIN_X + MapProjection.TURKEY_MAX_X) / 2,
                y = (MapProjection.TURKEY_MIN_Y + MapProjection.TURKEY_MAX_Y) / 2
            ),
            zoom = MapZoom.COUNTRY.level
        )

        fun of(place: Place) = CameraTarget(MapProjection.toBase(place.point), place.zoom.level)
    }
}

/**
 * Bir karedeki (frame) izdüşüm: harita birimi -> ekran pikseli.
 *
 * Responsive: ölçek, haritanın gerçek boyutundan hesaplanır. Dar ve uzun telefonda
 * genişlik, geniş ve alçak ekranda (yatay telefon, tablet) yükseklik belirleyicidir;
 * böylece Türkiye görünümü her ekrana sığar.
 */
class Viewport(private val centerX: Float, private val centerY: Float, val zoom: Float, private val size: Size) {

    val pixelsPerUnit: Float = zoom * minOf(size.width, size.height * FIT_ASPECT) / DESIGN_WIDTH_UNITS

    /** Hava animasyonları ve etiketler yakınlaştıkça büyür ama hiçbir zaman devleşmez. */
    val overlayScale: Float = (OVERLAY_BASE + OVERLAY_PER_ZOOM * zoom).coerceIn(OVERLAY_MIN, OVERLAY_MAX)

    /** Yakın plan (il / ilçe detayı) katmanlarının görünürlüğü: 5x'te başlar, 8x'te tamdır. */
    val detailAlpha: Float = ((zoom - DETAIL_FADE_START) / DETAIL_FADE_RANGE).coerceIn(0f, 1f)

    fun toScreen(point: BasePoint) = Offset(
        x = (point.x - centerX) * pixelsPerUnit + size.width / 2,
        y = (point.y - centerY) * pixelsPerUnit + size.height / 2
    )

    fun isOnScreen(offset: Offset, margin: Float): Boolean =
        offset.x in -margin..(size.width + margin) && offset.y in -margin..(size.height + margin)

    /** Haritanın kendisini çizmek için gereken dönüşüm değerleri. */
    val translateX: Float get() = -centerX
    val translateY: Float get() = -centerY

    private companion object {
        const val DESIGN_WIDTH_UNITS = 390f
        const val FIT_ASPECT = 2.2f
        const val OVERLAY_BASE = 0.25f
        const val OVERLAY_PER_ZOOM = 0.075f
        const val OVERLAY_MIN = 0.32f
        const val OVERLAY_MAX = 1.25f
        const val DETAIL_FADE_START = 5f
        const val DETAIL_FADE_RANGE = 3f
    }
}

/**
 * Hangi yer etiketinin hangi yakınlıkta görüneceği.
 * Türkiye bakışında şehirler, şehre inince ilçeler, mahalleye inince semtler görünür.
 */
fun MapZoom.isVisibleAt(zoom: Float): Boolean = when (this) {
    MapZoom.COUNTRY, MapZoom.CITY, MapZoom.REGION -> zoom < DISTRICT_LABELS_FROM
    MapZoom.DISTRICT -> zoom >= DISTRICT_LABELS_FROM
    MapZoom.STREET -> zoom >= STREET_LABELS_FROM
}

private const val DISTRICT_LABELS_FROM = 7f
private const val STREET_LABELS_FROM = 30f
