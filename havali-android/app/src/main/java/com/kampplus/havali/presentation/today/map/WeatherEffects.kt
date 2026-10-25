package com.kampplus.havali.presentation.today.map

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.presentation.theme.MapColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Harita üstündeki hava animasyonları. Her biri küçük, saf bir çizim fonksiyonudur:
 * merkez, yarıçap ve 0..1 arası animasyon fazı alır. Faz sonsuz bir geçişten gelir;
 * yalnızca çizim (draw) aşaması yenilenir, ekran yeniden oluşturulmaz (recomposition yok).
 */

private const val FULL_TURN = 2 * PI.toFloat()
private const val WINDY_KMH = 35.0

internal fun DrawScope.drawWeatherEffect(
    kind: WeatherKind,
    gustKmh: Double,
    center: Offset,
    radius: Float,
    phase: Float,
    colors: MapColors
) {
    when (kind) {
        WeatherKind.CLEAR -> drawSun(center, radius, phase, colors.sun)
        WeatherKind.CLOUDY -> drawCloud(center, radius, phase, colors.label)
        WeatherKind.FOG -> drawFog(center, radius, phase, colors.label)
        WeatherKind.RAIN -> drawRain(center, radius, phase, colors.rain, drops = 7)
        WeatherKind.STORM -> drawRain(center, radius, phase, colors.rain, drops = 11)
        WeatherKind.SNOW -> drawSnow(center, radius, phase, colors.snow)
    }
    if (gustKmh >= WINDY_KMH) drawWind(center, radius, phase, colors.wind)
}

/** Seçili yerin etrafında genişleyen halka. */
internal fun DrawScope.drawPing(center: Offset, radius: Float, phase: Float, color: Color) {
    drawCircle(
        color = color.copy(alpha = (1f - phase) * 0.8f),
        radius = radius * (0.25f + phase),
        center = center,
        style = Stroke(width = radius * 0.06f)
    )
}

private fun DrawScope.drawSun(center: Offset, radius: Float, phase: Float, color: Color) {
    val pulse = 1f + 0.08f * sin(phase * FULL_TURN)
    drawCircle(
        Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), center, radius * 1.4f * pulse),
        radius * 1.4f * pulse,
        center
    )
    drawCircle(color, radius * 0.24f, center)
    rotate(degrees = phase * 90f, pivot = center) {
        repeat(SUN_RAYS) { index ->
            val angle = index * FULL_TURN / SUN_RAYS
            val direction = Offset(cos(angle), sin(angle))
            drawLine(
                color = color,
                start = center + direction * (radius * 0.38f),
                end = center + direction * (radius * 0.56f),
                strokeWidth = radius * 0.07f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawCloud(center: Offset, radius: Float, phase: Float, color: Color) {
    val drift = sin(phase * FULL_TURN) * radius * 0.1f
    val puff = color.copy(alpha = 0.16f)
    drawCircle(puff, radius * 0.45f, center + Offset(drift - radius * 0.35f, 0f))
    drawCircle(puff, radius * 0.6f, center + Offset(drift, -radius * 0.15f))
    drawCircle(puff, radius * 0.42f, center + Offset(drift + radius * 0.4f, radius * 0.05f))
}

private fun DrawScope.drawFog(center: Offset, radius: Float, phase: Float, color: Color) {
    repeat(FOG_BANDS) { index ->
        val shift = sin((phase + index * 0.3f) * FULL_TURN) * radius * 0.2f
        drawRoundRect(
            color = color.copy(alpha = 0.16f),
            topLeft = Offset(center.x - radius + shift, center.y - radius * 0.4f + index * radius * 0.35f),
            size = Size(radius * 2f, radius * 0.18f),
            cornerRadius = CornerRadius(radius * 0.09f)
        )
    }
}

private fun DrawScope.drawRain(center: Offset, radius: Float, phase: Float, color: Color, drops: Int) {
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.5f), Color.Transparent), center, radius * 1.5f), radius * 1.5f, center)
    drawCloud(center + Offset(0f, -radius * 0.35f), radius * 0.8f, phase, Color.White)
    repeat(drops) { index ->
        val t = (phase * 2f + index * 0.37f) % 1f
        val x = center.x + (index / (drops - 1f) - 0.5f) * radius * 1.4f
        val y = center.y - radius * 0.15f + t * radius * 1.1f
        drawLine(
            color = color.copy(alpha = (1f - t) * 0.95f),
            start = Offset(x, y),
            end = Offset(x - radius * 0.08f, y + radius * 0.24f),
            strokeWidth = radius * 0.05f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSnow(center: Offset, radius: Float, phase: Float, color: Color) {
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.3f), Color.Transparent), center, radius * 1.3f), radius * 1.3f, center)
    repeat(SNOW_FLAKES) { index ->
        val t = (phase + index * 0.13f) % 1f
        val sway = sin((t + index) * FULL_TURN) * radius * 0.08f
        val x = center.x + (index / (SNOW_FLAKES - 1f) - 0.5f) * radius * 1.4f + sway
        val y = center.y - radius * 0.55f + t * radius * 1.1f
        drawCircle(color.copy(alpha = 1f - t * 0.7f), radius * 0.055f, Offset(x, y))
    }
}

private fun DrawScope.drawWind(center: Offset, radius: Float, phase: Float, color: Color) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(radius * 0.3f, radius * 0.2f), phase = -phase * radius * 2f)
    repeat(WIND_LINES) { index ->
        val y = center.y - radius * 0.3f + index * radius * 0.3f
        val line = Path().apply {
            moveTo(center.x - radius * 1.1f, y)
            quadraticTo(center.x, y - radius * 0.15f, center.x + radius * 1.1f, y)
        }
        drawPath(line, color.copy(alpha = 0.8f), style = Stroke(width = radius * 0.045f, cap = StrokeCap.Round, pathEffect = dash))
    }
}

private const val SUN_RAYS = 8
private const val FOG_BANDS = 3
private const val SNOW_FLAKES = 8
private const val WIND_LINES = 3
