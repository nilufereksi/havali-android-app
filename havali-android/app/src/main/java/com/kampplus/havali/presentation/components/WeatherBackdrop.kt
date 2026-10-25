package com.kampplus.havali.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.presentation.common.icon

/**
 * Sayfanın arkasına havaya göre sönük bir desen çizer: yağmurda damla bulutları,
 * güneşte güneşler, karda kar taneleri. Desen yavaşça aşağı kayar; zemin rengi değişmez.
 * Animasyon yalnızca çizim aşamasında okunur, ekran yeniden oluşturulmaz.
 */
@Composable
fun WeatherBackdrop(kind: WeatherKind, modifier: Modifier = Modifier, alpha: Float = BACKDROP_ALPHA, tint: Color = Color.White) {
    val painter = rememberVectorPainter(kind.icon)
    val drift = rememberInfiniteTransition(label = "backdrop").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = DRIFT_MS, easing = LinearEasing)),
        label = "drift"
    )
    Canvas(modifier = modifier) {
        val cell = CELL_DP.dp.toPx()
        val iconSize = ICON_DP.dp.toPx()
        val shift = drift.value * cell
        val columns = (size.width / cell).toInt() + 2
        val rows = (size.height / cell).toInt() + 2
        for (row in -1 until rows) {
            for (column in 0 until columns) {
                val stagger = (row % 2) * cell / 2
                translate(left = column * cell - stagger, top = row * cell + shift) {
                    rotate(degrees = TILT_DEGREES, pivot = Offset(iconSize / 2, iconSize / 2)) {
                        with(painter) { draw(Size(iconSize, iconSize), alpha = alpha, colorFilter = ColorFilter.tint(tint)) }
                    }
                }
            }
        }
    }
}

private const val BACKDROP_ALPHA = 0.07f
private const val CELL_DP = 72
private const val ICON_DP = 34
private const val DRIFT_MS = 9000
private const val TILT_DEGREES = -12f
