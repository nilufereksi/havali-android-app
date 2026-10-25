package com.kampplus.havali.presentation.today.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.MapLayer
import com.kampplus.havali.domain.model.MapProjection
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PlaceTag
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.theme.HavaliTheme
import com.kampplus.havali.presentation.theme.MapColors
import kotlin.math.exp
import kotlin.math.ln

/** Haritadaki bir yer ve o andaki havası. */
@Immutable
data class MapMarker(val place: Place, val weather: WeatherSnapshot?, val selected: Boolean)

/**
 * Canlı hava haritası: gerçek Türkiye sınırları + her yerin üstünde kendi hava animasyonu.
 *
 * Üç katman üst üste çizilir ve her biri yalnızca kendi ihtiyacı olan durumu okur:
 * 1. Zemin (ülke, il, ilçe sınırları) yalnızca kamera değişince yeniden çizilir.
 * 2. Hava animasyonları sonsuz faz ile her karede çizilir.
 * 3. Etiketler kamera değişince çizilir.
 * Kamera hedefi değişince merkez ve yakınlık 1,4 sn'de yumuşakça geçer
 * (yakınlık logaritmik ölçekte animasyonlanır; böylece Türkiye'den semte iniş doğal görünür).
 */
@Composable
fun WeatherMap(
    geometry: MapGeometry?,
    markers: List<MapMarker>,
    reports: List<GeoPoint>,
    campsites: List<GeoPoint>,
    camera: CameraTarget,
    onPlaceTap: (Place) -> Unit,
    onEmptyTap: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val colors = HavaliTheme.colors.map
    val highlight = MaterialTheme.colorScheme.primary
    val paths = remember(geometry) { geometry?.let(::MapPaths) }
    val cameraState = rememberAnimatedCamera(camera)
    val phase = rememberWeatherPhase()
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium
    val currentMarkers = rememberUpdatedState(markers)
    val currentOnTap = rememberUpdatedState(onPlaceTap)
    val currentOnEmptyTap = rememberUpdatedState(onEmptyTap)

    Box(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val viewport = cameraState.viewport(size.width.toFloat(), size.height.toFloat())
                    // Bir yere dokunulduysa oraya yaklaşılır; boş bir yere dokununca bir üst görünüme dönülür.
                    nearestMarker(currentMarkers.value, viewport, tap, TAP_RADIUS_DP.dp.toPx())
                        ?.let { currentOnTap.value(it.place) }
                        ?: currentOnEmptyTap.value()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val viewport = cameraState.viewport(size.width, size.height)
            drawRect(colors.sea)
            paths?.let { drawGeography(it, viewport, colors) }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val viewport = cameraState.viewport(size.width, size.height)
            val radius = EFFECT_RADIUS_DP.dp.toPx() * viewport.overlayScale
            markers.visibleOn(viewport, radius).forEach { (marker, center) ->
                marker.weather?.let { drawWeatherEffect(it.kind, it.gustKmh, center, radius, phase.value, colors) }
                if (marker.selected) drawPing(center, radius, phase.value, highlight)
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val viewport = cameraState.viewport(size.width, size.height)
            drawReports(reports, viewport, highlight)
            drawCampsites(campsites, viewport, colors)
            markers.visibleOn(viewport, margin = 0f).forEach { (marker, center) ->
                drawLabel(textMeasurer, labelStyle, marker, center, colors, highlight)
            }
        }
    }
}

/** Kameranın animasyonlu durumu. Değerler State olarak okunur; okuma çizim aşamasında yapılır. */
private class AnimatedCamera(private val x: State<Float>, private val y: State<Float>, private val logZoom: State<Float>) {
    fun viewport(width: Float, height: Float) = Viewport(x.value, y.value, exp(logZoom.value), Size(width, height))
}

@Composable
private fun rememberAnimatedCamera(target: CameraTarget): AnimatedCamera {
    val spec = tween<Float>(durationMillis = CAMERA_MS, easing = FastOutSlowInEasing)
    val x = animateFloatAsState(target.center.x, spec, label = "cameraX")
    val y = animateFloatAsState(target.center.y, spec, label = "cameraY")
    val logZoom = animateFloatAsState(ln(target.zoom), spec, label = "cameraZoom")
    return remember { AnimatedCamera(x, y, logZoom) }
}

@Composable
private fun rememberWeatherPhase(): State<Float> = rememberInfiniteTransition(label = "weather").animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(durationMillis = PHASE_MS, easing = LinearEasing)),
    label = "phase"
)

internal fun DrawScope.drawGeography(paths: MapPaths, viewport: Viewport, colors: MapColors) {
    val hairline = 1.dp.toPx() / viewport.pixelsPerUnit
    val detail = viewport.detailAlpha
    withTransform({
        translate(size.width / 2, size.height / 2)
        scale(viewport.pixelsPerUnit, viewport.pixelsPerUnit, pivot = Offset.Zero)
        translate(viewport.translateX, viewport.translateY)
    }) {
        paths[MapLayer.NEIGHBORS]?.let { drawPath(it, colors.neighbor) }
        paths[MapLayer.COUNTRY]?.let { drawPath(it, colors.land) }
        paths[MapLayer.PROVINCES]?.let { drawPath(it, colors.border, alpha = 0.7f * (1f - detail), style = Stroke(hairline)) }
        paths[MapLayer.DETAIL_LAND]?.let { drawPath(it, colors.landDetail, alpha = detail) }
        paths[MapLayer.DETAIL_DISTRICTS]?.let { drawPath(it, colors.border, alpha = 0.6f * detail, style = Stroke(hairline * 0.8f)) }
        paths[MapLayer.DETAIL_COAST]?.let { drawPath(it, colors.label, alpha = 0.35f * detail, style = Stroke(hairline * 1.2f)) }
        paths[MapLayer.COUNTRY]?.let { drawPath(it, colors.label, alpha = 0.3f * (1f - detail), style = Stroke(hairline * 1.2f)) }
    }
}

private fun DrawScope.drawReports(reports: List<GeoPoint>, viewport: Viewport, color: Color) {
    val dot = REPORT_DP.dp.toPx()
    reports
        .map { viewport.toScreen(MapProjection.toBase(it)) }
        .filter { viewport.zoom >= REPORTS_FROM_ZOOM && viewport.isOnScreen(it, dot) }
        .forEach { center ->
            drawCircle(color, dot / 2, center)
            drawCircle(Color.Black.copy(alpha = 0.6f), dot / 2, center, style = Stroke(width = dot * 0.15f))
        }
}

/** Kamp alanları: yalnızca şehre yaklaşınca (Türkiye görünümünde değil) küçük çadır olarak çizilir. */
private fun DrawScope.drawCampsites(sites: List<GeoPoint>, viewport: Viewport, colors: MapColors) {
    val half = TENT_DP.dp.toPx() / 2
    sites
        .map { viewport.toScreen(MapProjection.toBase(it)) }
        .filter { viewport.zoom >= REPORTS_FROM_ZOOM && viewport.isOnScreen(it, half) }
        .forEach { center ->
            val tent = Path().apply {
                moveTo(center.x, center.y - half)
                lineTo(center.x + half, center.y + half * 0.8f)
                lineTo(center.x - half, center.y + half * 0.8f)
                close()
            }
            drawCircle(colors.labelBackground.copy(alpha = 0.85f), half * 1.35f, center)
            drawPath(tent, colors.wind)
            drawLine(colors.labelBackground, center, Offset(center.x, center.y + half * 0.8f), strokeWidth = half * 0.25f)
        }
}

private fun DrawScope.drawLabel(
    measurer: TextMeasurer,
    style: TextStyle,
    marker: MapMarker,
    anchor: Offset,
    colors: MapColors,
    highlight: Color
) {
    val text = listOfNotNull(marker.place.name, marker.weather?.let { Format.degrees(it.temperatureC) }).joinToString("  ")
    val (background, ink) = when (marker.selected) {
        true -> highlight to colors.labelBackground
        false -> colors.labelBackground.copy(alpha = 0.88f) to colors.label
    }
    val layout = measurer.measure(text, style.copy(color = ink))
    val padH = 8.dp.toPx()
    val padV = 4.dp.toPx()
    val pill = Size(layout.size.width + padH * 2, layout.size.height + padV * 2)
    val topLeft = Offset(anchor.x - pill.width / 2, anchor.y - pill.height - LABEL_GAP_DP.dp.toPx())
    drawCircle(background, 3.dp.toPx(), anchor)
    drawRoundRect(background, topLeft, pill, CornerRadius(pill.height / 2))
    drawText(layout, topLeft = topLeft + Offset(padH, padV))
}

/** Ekranda görünen işaretler ve ekran konumları. */
private fun List<MapMarker>.visibleOn(viewport: Viewport, margin: Float): List<Pair<MapMarker, Offset>> =
    filter { it.selected || PlaceTag.FEATURED in it.place.tags || it.place.zoom.isVisibleAt(viewport.zoom) }
        .map { it to viewport.toScreen(MapProjection.toBase(it.place.point)) }
        .filter { (_, offset) -> viewport.isOnScreen(offset, margin) }

/** Dokunulan noktaya en yakın görünür yer; etiket hemen noktanın üstünde olduğu için hedef biraz yukarı kaydırılır. */
private fun nearestMarker(markers: List<MapMarker>, viewport: Viewport, tap: Offset, radiusPx: Float): MapMarker? =
    markers.visibleOn(viewport, margin = 0f)
        .map { (marker, offset) -> marker to (offset - Offset(0f, radiusPx * 0.5f) - tap).getDistance() }
        .filter { (_, distance) -> distance <= radiusPx }
        .minByOrNull { (_, distance) -> distance }
        ?.first

private const val CAMERA_MS = 1400
private const val PHASE_MS = 2400
private const val EFFECT_RADIUS_DP = 34
private const val TAP_RADIUS_DP = 30
private const val REPORT_DP = 9
private const val TENT_DP = 14
private const val LABEL_GAP_DP = 6
private const val REPORTS_FROM_ZOOM = 7f
