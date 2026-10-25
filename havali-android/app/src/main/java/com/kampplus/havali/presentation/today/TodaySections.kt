package com.kampplus.havali.presentation.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.ActivityAssessment
import com.kampplus.havali.domain.model.ActivityPlan
import com.kampplus.havali.domain.model.ActivityReason
import com.kampplus.havali.domain.model.ActivityType
import com.kampplus.havali.domain.model.ActivityVerdict
import com.kampplus.havali.domain.model.ComfortFeedback
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.SummaryFact
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.icon
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.common.textRes
import com.kampplus.havali.presentation.components.HavaliCard
import com.kampplus.havali.presentation.components.Pill
import com.kampplus.havali.presentation.components.RaisedCard
import com.kampplus.havali.presentation.components.SectionHeader
import com.kampplus.havali.presentation.components.VerdictBadge
import com.kampplus.havali.presentation.theme.HavaliTheme

/** "Bugün ne yapsam?": seçili yerin havasına göre spor ve aktivite önerileri. */
@Composable
fun ActivitiesSection(placeName: String, plan: ActivityPlan, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = stringResource(R.string.activities_title)) {
            Pill(text = placeName, container = HavaliTheme.colors.raised, content = MaterialTheme.colorScheme.onBackground)
        }
        BestActivityCard(plan.best)
        plan.others.take(EXTRA_ACTIVITIES).forEach { ActivityRow(it) }
    }
}

@Composable
private fun BestActivityCard(best: ActivityAssessment) {
    HavaliCard(container = MaterialTheme.colorScheme.primary) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.onPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(best.type.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.activities_best), style = MaterialTheme.typography.labelSmall)
                Text(text = stringResource(best.type.labelRes), style = MaterialTheme.typography.headlineSmall)
                Text(text = reasonText(best.verdict), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ActivityRow(assessment: ActivityAssessment) {
    RaisedCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                assessment.type.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(assessment.type.labelRes), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = reasonText(assessment.verdict),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnBackground
                )
            }
            VerdictBadge(assessment.verdict.suitability)
        }
    }
}

@Composable
private fun reasonText(verdict: ActivityVerdict) = stringResource(verdict.reason.textRes, verdict.value)

/** Günün özeti ve "Dün nasıldı?" sorusu: cevap kişisel hissedilen sıcaklığı öğretir. */
@Composable
fun SummarySection(
    placeName: String,
    facts: List<SummaryFact>,
    departure: TodayPlanItem?,
    feedback: ComfortFeedback?,
    onFeedback: (ComfortFeedback) -> Unit,
    modifier: Modifier = Modifier
) {
    HavaliCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.summary_title, placeName).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = HavaliTheme.colors.mutedOnSurface
        )
        Text(text = summaryText(facts, departure), style = MaterialTheme.typography.bodyLarge)
        AnimatedContent(targetState = feedback, label = "feedback") { answer ->
            when (answer) {
                null -> FeedbackQuestion(onFeedback)
                else -> Text(
                    text = stringResource(R.string.feedback_thanks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HavaliTheme.colors.onInfoTint
                )
            }
        }
    }
}

@Composable
private fun FeedbackQuestion(onFeedback: (ComfortFeedback) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.feedback_question), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ComfortFeedback.entries.forEach { option ->
                OutlinedButton(onClick = { onFeedback(option) }, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(option.labelRes), maxLines = 1)
                }
            }
        }
    }
}

/** Olguları cümleye çevirip birleştirir. Çıkış önerisi varsa en sona eklenir. */
@Composable
private fun summaryText(facts: List<SummaryFact>, departure: TodayPlanItem?): String {
    val sentences = facts.map { factText(it) }
    val leave = departure?.let { item ->
        item.departure?.let { stringResource(R.string.summary_leave, item.plan.title, Format.time(it.leaveAt)) }
    }
    return (sentences + listOfNotNull(leave)).joinToString(" ")
}

@Composable
private fun factText(fact: SummaryFact): String = when (fact) {
    is SummaryFact.RainNow -> stringResource(R.string.summary_rain_now, fact.probability)
    is SummaryFact.RainArrives -> stringResource(R.string.summary_rain_arrives, Format.time(fact.at), fact.probability)
    SummaryFact.StaysDry -> stringResource(R.string.summary_dry)
    is SummaryFact.StrongGusts -> stringResource(R.string.summary_gusts, fact.gustKmh)
    is SummaryFact.FreshSnow -> stringResource(R.string.summary_snow, fact.depthCm)
    is SummaryFact.PersonalFeel -> stringResource(R.string.summary_personal, fact.actualC, fact.personalC)
    is SummaryFact.HighUv -> stringResource(R.string.summary_uv, fact.index)
}

/** "Bugün ne yapsam?" kısa kalsın: bir öneri + bir alternatif. */
private const val EXTRA_ACTIVITIES = 1

@Preview(showBackground = true, backgroundColor = 0xFF1F3B6E, widthDp = 390)
@Composable
private fun ActivitiesPreview() = HavaliTheme {
    ActivitiesSection(
        placeName = "Alaçatı",
        plan = ActivityPlan(
            best = ActivityAssessment(ActivityType.WINDSURF, ActivityVerdict(Suitability.GOOD, ActivityReason.GOOD_WIND, 42)),
            others = listOf(ActivityAssessment(ActivityType.SUP, ActivityVerdict(Suitability.BAD, ActivityReason.WIND_TOO_STRONG, 55)))
        ),
        modifier = Modifier.padding(16.dp)
    )
}
