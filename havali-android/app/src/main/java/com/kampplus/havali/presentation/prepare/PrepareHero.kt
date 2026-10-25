package com.kampplus.havali.presentation.prepare

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.havali.R
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.theme.HavaliTheme
import com.kampplus.havali.presentation.today.map.drawWeatherEffect

/**
 * Hazırlık ekranının vitrini: sağda bugünün havası büyük ve canlı (haritadaki animasyonun
 * aynısı), solda "hazır mısın?" halkası listeyi işaretledikçe dolar; köşede kedi zıplar.
 */
@Composable
fun PrepareHero(daily: DailyPrepUi, modifier: Modifier = Modifier) {
    val colors = HavaliTheme.colors.map
    val progress by animateFloatAsState(daily.done.toFloat() / daily.total.coerceAtLeast(1), label = "ready")
    val phase = rememberInfiniteTransition(label = "hero").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = PHASE_MS, easing = LinearEasing)),
        label = "phase"
    )
    val hop = rememberInfiniteTransition(label = "cat").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = HOP_MS), repeatMode = RepeatMode.Reverse),
        label = "hop"
    )
    val ring = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT)
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(colors.labelBackground, colors.sea)))
    ) {
        Canvas(modifier = Modifier.fillMaxHeight().fillMaxWidth()) {
            val center = Offset(size.width * 0.74f, size.height * 0.5f)
            drawWeatherEffect(
                daily.kind,
                gustKmh = 0.0,
                center = center,
                radius = size.height * 0.34f,
                phase = phase.value,
                colors = colors
            )
        }
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(RING_SIZE)) {
                Canvas(modifier = Modifier.size(RING_SIZE)) {
                    val stroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(Color.White.copy(alpha = 0.15f), 0f, FULL_CIRCLE, useCenter = false, style = stroke)
                    drawArc(ring, START_ANGLE, FULL_CIRCLE * progress, useCenter = false, style = stroke)
                }
                Text(
                    text = stringResource(R.string.prepare_hero_progress, daily.done, daily.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.prepare_hero_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = stringResource(
                        R.string.prepare_hero_body,
                        stringResource(daily.kind.labelRes),
                        stringResource(daily.outfit.base.labelRes)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
        Text(
            text = CAT,
            fontSize = 26.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .graphicsLayer { translationY = -hop.value * HOP_DP * density }
        )
    }
}

private const val CAT = "🐈"
private val HERO_HEIGHT = 150.dp
private val RING_SIZE = 92.dp
private const val FULL_CIRCLE = 360f
private const val START_ANGLE = -90f
private const val PHASE_MS = 2400
private const val HOP_MS = 520
private const val HOP_DP = 8f
