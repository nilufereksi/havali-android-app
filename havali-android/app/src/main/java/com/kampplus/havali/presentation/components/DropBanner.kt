package com.kampplus.havali.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.havali.R
import com.kampplus.havali.presentation.theme.HavaliTheme
import kotlinx.coroutines.delay

/**
 * Ekrana girerken yukarıdan "düşen" uygulama içi bildirim.
 *
 * Görünürlük `remember` ile tutulur (rememberSaveable değil): ekrana her girişte
 * bildirim yeniden düşer. Yaylı (spring) animasyon düşüşe hafif bir sekme verir.
 * Birkaç saniye sonra kendiliğinden kalkar; dokununca hemen kapanır.
 */
/** Bildirimin tonu: bilgi (sarı rozet, harf) ya da uyarı (kırmızı rozet, ünlem). */
enum class BannerTone { INFO, ALERT }

@Composable
fun DropBanner(
    badge: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.INFO,
    courierCat: Boolean = false,
    delayMillis: Long = 350,
    visibleMillis: Long = 4200
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        visible = true
        delay(visibleMillis)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
        slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { -it * 2 } +
            fadeIn(),
        exit = slideOutVertically { -it * 2 } + fadeOut()
    ) {
        Box(modifier = Modifier.padding(top = catSpace(courierCat))) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                border = borderOf(tone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .clickable { visible = false }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BannerBadge(badge, tone)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.app_name).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = HavaliTheme.colors.mutedOnSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = stringResource(R.string.now),
                                style = MaterialTheme.typography.labelSmall,
                                color = HavaliTheme.colors.mutedOnSurface
                            )
                        }
                        Text(text = title, style = MaterialTheme.typography.titleSmall, color = titleColor(tone))
                        Text(text = body, style = MaterialTheme.typography.bodySmall, color = HavaliTheme.colors.mutedOnSurface)
                    }
                }
            }
            if (courierCat) CourierCat(modifier = Modifier.align(Alignment.TopStart).padding(start = 22.dp).offset(y = -CAT_SPACE))
        }
    }
}

@Composable
private fun BannerBadge(badge: String, tone: BannerTone) {
    val container = when (tone) {
        BannerTone.INFO -> MaterialTheme.colorScheme.primary
        BannerTone.ALERT -> MaterialTheme.colorScheme.error
    }
    Box(modifier = Modifier.size(38.dp).background(container, CircleShape), contentAlignment = Alignment.Center) {
        when (tone) {
            BannerTone.INFO -> Text(text = badge, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
            BannerTone.ALERT -> Icon(
                HavaliIcons.Exclamation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun titleColor(tone: BannerTone): Color = when (tone) {
    BannerTone.INFO -> MaterialTheme.colorScheme.onSurface
    BannerTone.ALERT -> HavaliTheme.colors.onDangerTint
}

@Composable
private fun borderOf(tone: BannerTone): BorderStroke? = when (tone) {
    BannerTone.INFO -> null
    BannerTone.ALERT -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
}

/**
 * Bildirimi getiren kedi (Logcat'ten esinlenme). Banner yukarıdan inerken kedi onu taşır,
 * kalkarken birlikte yukarı çıkar; beklerken kuyruğunu sallar gibi sağa sola sallanır.
 */
@Composable
private fun CourierCat(modifier: Modifier = Modifier) {
    val swing = rememberInfiniteTransition(label = "cat").animateFloat(
        initialValue = -CAT_SWING_DEGREES,
        targetValue = CAT_SWING_DEGREES,
        animationSpec = infiniteRepeatable(tween(durationMillis = CAT_SWING_MS), repeatMode = RepeatMode.Reverse),
        label = "swing"
    )
    Text(
        text = CAT,
        fontSize = 30.sp,
        modifier = modifier.graphicsLayer {
            rotationZ = swing.value
            transformOrigin = TransformOrigin(0.5f, 1f)
        }
    )
}

private fun catSpace(courierCat: Boolean) = when (courierCat) {
    true -> CAT_SPACE
    false -> 0.dp
}

private const val CAT = "🐈"
private val CAT_SPACE = 26.dp
private const val CAT_SWING_DEGREES = 10f
private const val CAT_SWING_MS = 420
