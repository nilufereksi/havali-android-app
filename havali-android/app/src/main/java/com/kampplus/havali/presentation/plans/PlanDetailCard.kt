package com.kampplus.havali.presentation.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.PlanScore
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.common.rememberNotificationPermissionRequest
import com.kampplus.havali.presentation.common.textRes
import com.kampplus.havali.presentation.components.HavaliCard
import com.kampplus.havali.presentation.components.Pill
import com.kampplus.havali.presentation.components.VerdictBadge
import com.kampplus.havali.presentation.theme.HavaliTheme

/** Seçili planın ayrıntısı: uygunluk puanı, akıllı çıkış saati, hatırlatıcı ve daha rahat saat önerisi. */
@Composable
fun PlanDetailCard(detail: PlanDetail, onEvent: (PlansEvent) -> Unit, modifier: Modifier = Modifier) {
    HavaliCard(modifier = modifier) {
        Text(
            text = stringResource(
                R.string.detail_when,
                Format.dayLong(detail.plan.start.toLocalDate()),
                Format.time(detail.plan.start.toLocalTime())
            ).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = HavaliTheme.colors.mutedOnSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = detail.plan.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = stringResource(R.string.detail_where, detail.plan.placeName, Format.km(detail.plan.distanceKm)),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = Format.degrees(detail.weather.temperatureC), style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = stringResource(R.string.detail_rain, detail.weather.precipitationProbability),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        ScoreRow(detail.score)
        DepartureBlock(detail)
        ReminderRow(detail, onEvent)
        detail.betterTime?.let { better ->
            Row(
                modifier = Modifier.fillMaxWidth().background(
                    HavaliTheme.colors.infoTint,
                    MaterialTheme.shapes.small
                ).padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.detail_better_time,
                        Format.time(better.start.toLocalTime()),
                        better.precipitationProbability,
                        better.travelMinutes
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.onInfoTint,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onEvent(PlansEvent.AcceptBetterTime) }) { Text(stringResource(R.string.detail_better_time_accept)) }
            }
        }
    }
}

@Composable
private fun ScoreRow(score: PlanScore) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = score.value.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.shapes.small
            ).padding(horizontal = 12.dp, vertical = 4.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = stringResource(scoreLabel(score.value)), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                score.factors.forEach { factor ->
                    VerdictBadge(suitability = factor.rating, text = stringResource(factor.factor.labelRes))
                }
            }
        }
    }
}

private fun scoreLabel(value: Int): Int = when {
    value >= GREAT_SCORE -> R.string.score_great
    value >= OK_SCORE -> R.string.score_ok
    else -> R.string.score_hard
}

@Composable
private fun DepartureBlock(detail: PlanDetail) {
    val advice = detail.departure
    Column(
        modifier = Modifier.fillMaxWidth().background(HavaliTheme.colors.accentSoft, MaterialTheme.shapes.small).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_departure_title),
            style = MaterialTheme.typography.labelSmall,
            color = HavaliTheme.colors.onAccentSoft
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = Format.time(advice.leaveAt),
                style = MaterialTheme.typography.headlineLarge,
                color = HavaliTheme.colors.onAccentSoft
            )
            Text(
                text = stringResource(R.string.detail_usually, Format.time(advice.usualLeaveAt)),
                style = MaterialTheme.typography.bodySmall,
                color = HavaliTheme.colors.onAccentSoft
            )
        }
        Text(
            text = stringResource(R.string.detail_travel, advice.travelMinutes, advice.usualMinutes),
            style = MaterialTheme.typography.bodyMedium,
            color = HavaliTheme.colors.onAccentSoft
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            advice.delays.forEach { delay ->
                Pill(
                    text = stringResource(delay.factor.textRes, delay.minutes),
                    container = MaterialTheme.colorScheme.surface,
                    content = HavaliTheme.colors.onAccentSoft
                )
            }
        }
    }
}

/**
 * Hatırlatıcı anahtarı. Açılırken önce bildirim izni istenir (Android 13+);
 * izin verilmezse anahtar kapalı kalır ve nedeni yazılır.
 */
@Composable
private fun ReminderRow(detail: PlanDetail, onEvent: (PlansEvent) -> Unit) {
    var denied by rememberSaveable { mutableStateOf(false) }
    val requestPermission = rememberNotificationPermissionRequest { granted ->
        denied = !granted
        onEvent(PlansEvent.SetReminder(granted))
    }
    val remindAt = detail.departure.leaveAt.minusMinutes(PlansViewModel.REMIND_BEFORE_MINUTES)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.detail_remind_at, Format.time(remindAt)), style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(reminderCaption(denied)),
                style = MaterialTheme.typography.bodySmall,
                color = HavaliTheme.colors.mutedOnSurface
            )
        }
        Switch(
            checked = detail.reminderOn,
            onCheckedChange = { enabled ->
                when (enabled) {
                    true -> requestPermission()
                    false -> onEvent(PlansEvent.SetReminder(false))
                }
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = HavaliTheme.colors.deep,
                checkedThumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

private fun reminderCaption(denied: Boolean): Int = when (denied) {
    true -> R.string.detail_remind_denied
    false -> R.string.detail_remind_caption
}

private const val GREAT_SCORE = 75
private const val OK_SCORE = 50
