package com.kampplus.havali.presentation.road

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.kampplus.havali.domain.model.BasePoint
import com.kampplus.havali.domain.model.CorridorTraffic
import com.kampplus.havali.domain.model.MapProjection
import com.kampplus.havali.domain.model.TrafficLevel
import com.kampplus.havali.presentation.theme.HavaliTheme
import com.kampplus.havali.presentation.today.map.MapPaths
import com.kampplus.havali.presentation.today.map.Viewport
import com.kampplus.havali.presentation.today.map.drawGeography
import kotlin.math.sin

/**
 * Şehrin gerçek kıyı ve ilçe sınırları üstünde ana akslar: renk yoğunluğu, üzerinde akan
 * küçük araçlar hızı gösterir. Akıcı yolda araçlar seyrek ve hızlıdır; tıkalı yolda sık,
 * yavaş ve kırmızı nabız atar. Bir aksa dokununca ayrıntısı seçilir.
 */
@Composable
fun TrafficMap(traffic: TrafficUi, contentDescription: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = HavaliTheme.colors.map
    val paths = remember(traffic.geometry) { traffic.geometry?.let(::MapPaths) }
    val camera = remember(traffic.corridors.map { it.corridor.id }) { fitCamera(traffic.corridors) }
    val flow = rememberInfiniteTransition(label = "traffic").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = FLOW_MS, easing = LinearEasing)),
        label = "flow"
    )
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall
    val current = rememberUpdatedState(traffic)
    val currentOnSelect = rememberUpdatedState(onSelect)
    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(camera) {
                detectTapGestures { tap ->
                    val viewport = camera.viewport(size.width.toFloat(), size.height.toFloat())
                    nearestCorridor(current.value.corridors, viewport, tap, TAP_DP.dp.toPx())?.let { currentOnSelect.value(it) }
                }
            }
    ) {
        val viewport = camera.viewport(size.width, size.height)
        drawRect(colors.sea)
        paths?.let { drawGeography(it, viewport, colors) }
        drawPlaceLabels(measurer, labelStyle, traffic, viewport, colors.label)
        // Önce yan yollar, üstüne ana akslar çizilir; önerilen yan yol saks mavisi ve kesikli görünür.
        traffic.corridors.sortedBy { !it.corridor.isAlternative }.forEach { item ->
            drawCorridor(
                item,
                viewport,
                flow.value,
                selected = item.corridor.id == traffic.selected?.corridor?.id,
                suggested = item.corridor.id in traffic.suggestedIds
            )
        }
    }
}

/** Haritanın sabit kamerası: bütün akslar ekrana sığacak şekilde merkez ve yakınlık. */
private data class TrafficCamera(val center: BasePoint, val zoom: Float) {
    fun viewport(width: Float, height: Float) = Viewport(center.x, center.y, zoom, Size(width, height))
}

private fun fitCamera(corridors: List<CorridorTraffic>): TrafficCamera {
    val points = corridors.flatMap { it.corridor.points }.map(MapProjection::toBase).ifEmpty { listOf(BasePoint(0f, 0f)) }
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    val span = maxOf(maxX - minX, (maxY - minY) * ASPECT, MIN_SPAN)
    return TrafficCamera(BasePoint((minX + maxX) / 2, (minY + maxY) / 2), DESIGN_UNITS / (span * PADDING_FACTOR))
}

private fun DrawScope.drawCorridor(item: CorridorTraffic, viewport: Viewport, flow: Float, selected: Boolean, suggested: Boolean) {
    val path = smoothPath(item.corridor.points.map { viewport.toScreen(MapProjection.toBase(it)) })
    val style = item.style(suggested)
    val width = ROAD_DP.dp.toPx() * selectedScale(selected)
    drawPath(path, Color.Black.copy(alpha = 0.55f), style = Stroke(width = width * 1.8f, cap = StrokeCap.Round))
    drawPath(path, style.color, style = Stroke(width = width, cap = StrokeCap.Round, pathEffect = style.dash(width)))
    val flowDash = PathEffect.dashPathEffect(floatArrayOf(width * 1.2f, width * 2.4f), phase = -flow * width * 36f * style.speed)
    drawPath(path, Color.White.copy(alpha = 0.45f), style = Stroke(width = width * 0.35f, cap = StrokeCap.Round, pathEffect = flowDash))
    drawCars(path, style, flow, width)
}

/**
 * Noktaları kırık çizgiyle değil, Catmull-Rom eğrisiyle birleştirir: yol kıyı ve yamaç
 * kıvrımlarını yumuşakça izler, köşelerde sivri kırılma olmaz.
 */
private fun smoothPath(points: List<Offset>): Path = Path().apply {
    points.firstOrNull()?.let { moveTo(it.x, it.y) }
    for (i in 0 until points.size - 1) {
        val previous = points[(i - 1).coerceAtLeast(0)]
        val start = points[i]
        val end = points[i + 1]
        val next = points[(i + 2).coerceAtMost(points.size - 1)]
        val control1 = start + (end - previous) / CURVE_TENSION
        val control2 = end - (next - start) / CURVE_TENSION
        cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y)
    }
}

/** Aks boyunca araç noktaları: yoğunlukta sık ve yavaş, akıcıyken seyrek ve hızlı. */
private fun DrawScope.drawCars(path: Path, style: TrafficStyle, flow: Float, width: Float) {
    val measure = PathMeasure().apply { setPath(path, false) }
    val length = measure.length
    repeat(style.cars) { index ->
        val t = (flow * style.speed + index.toFloat() / style.cars) % 1f
        val position = measure.getPosition(t * length)
        val pulse = style.pulse * (0.5f + 0.5f * sin(flow * FULL_TURN * 3 + index))
        drawCircle(style.color.copy(alpha = 0.35f * pulse), width * 1.6f, position)
        drawCircle(Color.White, width * 0.55f, position)
    }
}

private fun DrawScope.drawPlaceLabels(measurer: TextMeasurer, style: TextStyle, traffic: TrafficUi, viewport: Viewport, color: Color) {
    traffic.labels.forEach { place ->
        val anchor = viewport.toScreen(MapProjection.toBase(place.point))
        val layout = measurer.measure(place.name, style.copy(color = color.copy(alpha = 0.75f)))
        val topLeft = anchor - Offset(layout.size.width / 2f, layout.size.height + LABEL_GAP_DP.dp.toPx())
        drawText(layout, topLeft = topLeft)
    }
}

private fun nearestCorridor(corridors: List<CorridorTraffic>, viewport: Viewport, tap: Offset, radius: Float): String? = corridors
    .map { item -> item.corridor.id to item.corridor.points.minOf { (viewport.toScreen(MapProjection.toBase(it)) - tap).getDistance() } }
    .filter { (_, distance) -> distance <= radius }
    .minByOrNull { (_, distance) -> distance }
    ?.first

/** Yoğunluğun görünümü: renk, araç sayısı, akış hızı, kırmızı nabız ve (yan yolsa) kesikli çizgi. */
private data class TrafficStyle(val color: Color, val cars: Int, val speed: Float, val pulse: Float, val dashed: Boolean = false) {
    fun dash(width: Float): PathEffect? = PathEffect.dashPathEffect(floatArrayOf(width * 2.5f, width * 1.2f)).takeIf { dashed }
}

/** Yan yollar önerildiğinde saks mavisi, önerilmediğinde sönük; ana akslar yoğunluk rengiyle çizilir. */
private fun CorridorTraffic.style(suggested: Boolean): TrafficStyle = when {
    suggested -> TrafficStyle(ALTERNATIVE_COLOR, cars = 3, speed = 1.2f, pulse = 0f, dashed = true)
    corridor.isAlternative -> TrafficStyle(ALTERNATIVE_COLOR.copy(alpha = 0.35f), cars = 1, speed = 1f, pulse = 0f, dashed = true)
    else -> level.style()
}

private fun TrafficLevel.style(): TrafficStyle = when (this) {
    TrafficLevel.LIGHT -> TrafficStyle(LIGHT_COLOR, cars = 3, speed = 1.4f, pulse = 0f)
    TrafficLevel.MODERATE -> TrafficStyle(MODERATE_COLOR, cars = 5, speed = 0.6f, pulse = 0.4f)
    TrafficLevel.HEAVY -> TrafficStyle(HEAVY_COLOR, cars = 9, speed = 0.2f, pulse = 1f)
}

private fun selectedScale(selected: Boolean): Float = when (selected) {
    true -> 1.6f
    false -> 1f
}

internal val LIGHT_COLOR = Color(0xFF4FC7B8)
internal val MODERATE_COLOR = Color(0xFFFF9F1C)
internal val HEAVY_COLOR = Color(0xFFE5533D)

/** Saks mavisi: önerilen alternatif rota. */
internal val ALTERNATIVE_COLOR = Color(0xFF0F52BA)
private const val FULL_TURN = 6.2831855f
private const val FLOW_MS = 6000
private const val ROAD_DP = 5
private const val TAP_DP = 26
private const val LABEL_GAP_DP = 4
private const val DESIGN_UNITS = 390f
private const val ASPECT = 1.6f
private const val MIN_SPAN = 2f
private const val PADDING_FACTOR = 1.35f
private const val CURVE_TENSION = 6f
