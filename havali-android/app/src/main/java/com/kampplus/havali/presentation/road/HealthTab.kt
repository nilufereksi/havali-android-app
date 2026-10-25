package com.kampplus.havali.presentation.road

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.ComfortSensitivity
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.bodyRes
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.common.noteRes
import com.kampplus.havali.presentation.common.rememberNow
import com.kampplus.havali.presentation.common.titleRes
import com.kampplus.havali.presentation.components.ErrorView
import com.kampplus.havali.presentation.components.HavaliCard
import com.kampplus.havali.presentation.components.LoadingView
import com.kampplus.havali.presentation.components.RaisedCard
import com.kampplus.havali.presentation.theme.HavaliTheme
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

/** "Sana göre hissedilen": öğrenilen kişisel sıcaklık hassasiyeti ve üç bölmeli ölçek. */
@Composable
fun ComfortProfileCard(profile: ComfortProfile) {
    HavaliCard {
        Text(
            text = stringResource(R.string.comfort_title),
            style = MaterialTheme.typography.labelSmall,
            color = HavaliTheme.colors.mutedOnSurface
        )
        Text(
            text = stringResource(sensitivityHeadline(profile.sensitivity), abs(profile.offsetC).roundToInt()),
            style = MaterialTheme.typography.titleLarge
        )
        ComfortScale(profile.offsetC)
        Text(
            text = stringResource(R.string.comfort_learned, profile.samples),
            style = MaterialTheme.typography.bodySmall,
            color = HavaliTheme.colors.mutedOnSurface
        )
    }
}

private fun sensitivityHeadline(sensitivity: ComfortSensitivity): Int = when (sensitivity) {
    ComfortSensitivity.FEELS_COLD -> R.string.comfort_headline_cold
    ComfortSensitivity.NEUTRAL -> R.string.comfort_headline_neutral
    ComfortSensitivity.FEELS_WARM -> R.string.comfort_headline_warm
}

/** -5 °C ... +5 °C arası çubuk; işaret kullanıcının konumunu gösterir. */
@Composable
private fun ComfortScale(offsetC: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(18.dp)) {
            val fraction = ((offsetC + MAX_OFFSET) / (MAX_OFFSET * 2)).toFloat().coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(HavaliTheme.colors.infoTint, RoundedCornerShape(percent = 50))
            )
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth - MARKER) * fraction)
                    .size(MARKER)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        Row {
            ComfortSensitivity.entries.forEach { level ->
                Text(
                    text = stringResource(level.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = HavaliTheme.colors.mutedOnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** UV ve güneş kremi zamanlayıcısı: krem sürülünce iki saatlik geri sayım başlar. */
@Composable
fun UvCard(health: HealthUi, onApply: () -> Unit) {
    val now = rememberNow()
    HavaliCard(container = HavaliTheme.colors.accentSoft) {
        Text(
            text = stringResource(R.string.uv_title, health.uvMax, stringResource(uvLevel(health.uvMax))).uppercase(),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = stringResource(R.string.uv_peak, health.uvPeakAt?.let(Format::time).orEmpty()),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(text = stringResource(R.string.sunscreen_title), style = MaterialTheme.typography.titleMedium)
        SunscreenStatus(health.sunscreenAppliedAt, now)
        Button(onClick = onApply) { Text(stringResource(sunscreenButton(health.sunscreenAppliedAt))) }
    }
}

@Composable
private fun SunscreenStatus(appliedAt: LocalDateTime?, now: LocalDateTime) {
    when (appliedAt) {
        null -> Text(text = stringResource(R.string.sunscreen_not_applied), style = MaterialTheme.typography.bodySmall)
        else -> {
            val remaining = Duration.between(now, appliedAt.plus(SUNSCREEN_DURATION)).coerceAtLeast(Duration.ZERO)
            val progress = remaining.toMinutes().toFloat() / SUNSCREEN_DURATION.toMinutes()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = HavaliTheme.colors.deep,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Text(
                text = stringResource(R.string.sunscreen_remaining, remaining.toHours(), remaining.toMinutes() % MINUTES_PER_HOUR),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun sunscreenButton(appliedAt: LocalDateTime?): Int = when (appliedAt) {
    null -> R.string.sunscreen_apply
    else -> R.string.sunscreen_reapply
}

private fun uvLevel(uv: Int): Int = when {
    uv < UV_MODERATE -> R.string.uv_low
    uv < UV_HIGH -> R.string.uv_moderate
    uv < UV_VERY_HIGH -> R.string.uv_high
    else -> R.string.uv_very_high
}

/** Cilt ve sağlık ipuçları: nem, rüzgâr, UV, soğuk. */
@Composable
fun CareTipsSection(health: HealthUi) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        health.careTips.forEach { advice ->
            RaisedCard {
                Text(text = stringResource(advice.tip.titleRes, advice.value), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(advice.tip.bodyRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnBackground
                )
            }
        }
    }
}

/** Hava kalitesi ve polen: ayrı bir servisten geldiği için kendi yükleniyor / hata hâlleri vardır. */
@Composable
fun AirQualitySection(state: LoadState<AirQuality>, onRetry: () -> Unit) {
    when (state) {
        LoadState.Loading -> LoadingView()
        LoadState.Empty -> Unit
        is LoadState.Error -> ErrorView(error = state.error, onRetry = onRetry)
        is LoadState.Success -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AirTile(
                stringResource(R.string.air_title),
                stringResource(state.data.level.labelRes),
                stringResource(state.data.level.noteRes),
                Modifier.weight(1f)
            )
            AirTile(
                stringResource(R.string.pollen_title),
                stringResource(state.data.pollen.labelRes),
                stringResource(R.string.pollen_note),
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AirTile(title: String, value: String, note: String, modifier: Modifier) {
    HavaliCard(modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = HavaliTheme.colors.mutedOnSurface)
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Text(text = note, style = MaterialTheme.typography.bodySmall, color = HavaliTheme.colors.mutedOnSurface)
    }
}

private const val MAX_OFFSET = 5.0
private const val MINUTES_PER_HOUR = 60
private val MARKER = 18.dp
private val SUNSCREEN_DURATION: Duration = Duration.ofHours(2)
private const val UV_MODERATE = 3
private const val UV_HIGH = 6
private const val UV_VERY_HIGH = 8
