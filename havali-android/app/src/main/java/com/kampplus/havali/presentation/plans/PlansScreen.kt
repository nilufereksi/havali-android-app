package com.kampplus.havali.presentation.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.PlanSuggestion
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.icon
import com.kampplus.havali.presentation.components.DropBanner
import com.kampplus.havali.presentation.components.EmptyView
import com.kampplus.havali.presentation.components.ErrorView
import com.kampplus.havali.presentation.components.HavaliCard
import com.kampplus.havali.presentation.components.LoadingView
import com.kampplus.havali.presentation.components.Pill
import com.kampplus.havali.presentation.components.ScrollbarColumn
import com.kampplus.havali.presentation.components.SectionHeader
import com.kampplus.havali.presentation.theme.HavaliTheme

@Composable
fun PlansRoute(twoPane: Boolean, contentPadding: PaddingValues, viewModel: PlansViewModel = viewModel(factory = PlansViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlansScreen(state, viewModel::onEvent, twoPane, contentPadding)
}

/**
 * Planlar: dar ekranda tek sütun; geniş ekranda solda oylama ve hafta, sağda seçili planın ayrıntısı.
 * Ekrana girerken "Beyza plan oylaması başlattı!" bildirimi yukarıdan düşer (oy verilmediyse).
 */
@Composable
fun PlansScreen(state: PlansUiState, onEvent: (PlansEvent) -> Unit, twoPane: Boolean, contentPadding: PaddingValues) {
    val padding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = contentPadding.calculateTopPadding() + 12.dp,
        bottom = contentPadding.calculateBottomPadding() + 24.dp
    )
    Box(modifier = Modifier.fillMaxSize()) {
        when (twoPane) {
            true -> Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlansColumn(padding, Modifier.weight(1f)) {
                    header(state)
                    status(state, onEvent)
                    voting(state, onEvent)
                    week(state, onEvent)
                }
                PlansColumn(padding, Modifier.weight(1f)) {
                    detail(state, onEvent)
                    suggestions(state, onEvent)
                }
            }
            false -> PlansColumn(padding, Modifier.fillMaxWidth()) {
                header(state)
                status(state, onEvent)
                voting(state, onEvent)
                week(state, onEvent)
                detail(state, onEvent)
                suggestions(state, onEvent)
            }
        }
        state.proposal?.takeIf { it.proposal.myVote == null }?.let { ui ->
            DropBanner(
                courierCat = true,
                badge = ui.proposal.proposer.initial,
                title = stringResource(R.string.banner_vote_title, ui.proposal.proposer.name),
                body = stringResource(R.string.banner_vote_body, ui.proposal.title, ui.proposal.votes.size),
                modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
            )
        }
    }
}

@Composable
private fun PlansColumn(padding: PaddingValues, modifier: Modifier, content: LazyListScope.() -> Unit) =
    ScrollbarColumn(contentPadding = padding, modifier = modifier.fillMaxHeight(), content = content)

private fun LazyListScope.header(state: PlansUiState) = item(key = "header") {
    Column {
        Text(
            text = stringResource(R.string.plans_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        state.days.takeIf { it.isNotEmpty() }?.let { days ->
            Text(
                text = stringResource(R.string.plans_range, Format.date(days.first().date), Format.date(days.last().date)),
                style = MaterialTheme.typography.bodyMedium,
                color = HavaliTheme.colors.mutedOnBackground
            )
        }
    }
}

/** Tahmin yüklenirken veya hata varsa (uçak modu) en üstte gösterilir; planlar yine de listelenir. */
private fun LazyListScope.status(state: PlansUiState, onEvent: (PlansEvent) -> Unit) {
    state.error?.let { error -> item(key = "error") { ErrorView(error = error, onRetry = { onEvent(PlansEvent.Retry) }) } }
    if (state.loading) item(key = "loading") { LoadingView() }
}

private fun LazyListScope.voting(state: PlansUiState, onEvent: (PlansEvent) -> Unit) {
    state.proposal?.let { proposal ->
        item(key = "vote-header") {
            SectionHeader(title = stringResource(R.string.vote_section_title)) {
                Text(
                    text = stringResource(R.string.vote_group, proposal.proposal.groupName, proposal.proposal.members.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnBackground
                )
            }
        }
        item(key = "vote") { VoteSection(proposal, onEvent) }
    }
}

private fun LazyListScope.week(state: PlansUiState, onEvent: (PlansEvent) -> Unit) {
    item(key = "week-header") { SectionHeader(title = stringResource(R.string.week_title)) }
    item(key = "week") { WeekStrip(state, onSelect = { onEvent(PlansEvent.SelectDate(it.date)) }) }
    when (state.dayPlans.isEmpty()) {
        true -> item(key = "day-empty") {
            EmptyView(title = stringResource(R.string.day_empty_title), body = stringResource(R.string.day_empty_body))
        }
        false -> items(state.dayPlans, key = { "plan-${it.id}" }) { plan ->
            PlanRow(plan, selected = plan.id == state.selectedPlanId, onClick = { onEvent(PlansEvent.SelectPlan(plan.id)) })
        }
    }
}

private fun LazyListScope.detail(state: PlansUiState, onEvent: (PlansEvent) -> Unit) {
    state.detail?.let { detail -> item(key = "detail-${detail.plan.id}") { PlanDetailCard(detail, onEvent) } }
}

private fun LazyListScope.suggestions(state: PlansUiState, onEvent: (PlansEvent) -> Unit) {
    items(state.suggestions, key = { "suggestion-${it.plan.id}" }) { suggestion -> SuggestionCard(suggestion, onEvent) }
}

/** Hafta şeridi: her gün havası ve plan sayısıyla. Seçili gün sarıdır. */
@Composable
private fun WeekStrip(state: PlansUiState, onSelect: (DayCell) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.days, key = { it.date.toString() }) { day ->
            DayChip(day, selected = day.date == state.selectedDate, onClick = { onSelect(day) })
        }
    }
}

@Composable
private fun DayChip(day: DayCell, selected: Boolean, onClick: () -> Unit) {
    val (container, content) = when (selected) {
        true -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        false -> HavaliTheme.colors.raised to MaterialTheme.colorScheme.onBackground
    }
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = Format.dayShort(day.date), style = MaterialTheme.typography.labelMedium, color = content)
        Text(text = day.date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, color = content)
        day.weather?.let { weather ->
            Icon(weather.kind.icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Text(text = Format.degrees(weather.maxTemperatureC), style = MaterialTheme.typography.labelMedium, color = content)
        }
        Box(modifier = Modifier.size(6.dp).background(content.copy(alpha = dotAlpha(day.planCount)), CircleShape))
    }
}

private fun dotAlpha(planCount: Int): Float = when (planCount) {
    0 -> 0f
    else -> 1f
}

@Composable
private fun PlanRow(plan: Plan, selected: Boolean, onClick: () -> Unit) {
    val border = when (selected) {
        true -> MaterialTheme.colorScheme.primary
        false -> Color.Transparent
    }
    HavaliCard(
        modifier = Modifier
            .border(2.dp, border, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = Format.time(plan.start.toLocalTime()),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.width(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = plan.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listOfNotNull(plan.placeName, plan.groupName).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnSurface
                )
            }
            Pill(
                text = stringResource(placeTypeLabel(plan)),
                container = HavaliTheme.colors.infoTint,
                content = HavaliTheme.colors.onInfoTint
            )
        }
    }
}

private fun placeTypeLabel(plan: Plan): Int = when {
    plan.done -> R.string.plan_done
    plan.outdoor -> R.string.plan_outdoor
    else -> R.string.plan_indoor
}

/** Havaya göre öneri kartı: esnek planı kuru güne taşı ya da kapalı mekân düşün. */
@Composable
private fun SuggestionCard(suggestion: PlanSuggestion, onEvent: (PlansEvent) -> Unit) {
    val plan = suggestion.plan
    HavaliCard(container = HavaliTheme.colors.accentSoft) {
        Text(text = stringResource(R.string.suggestion_title, plan.title), style = MaterialTheme.typography.titleMedium)
        Text(text = suggestionBody(suggestion), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestionAction(suggestion)?.let { label ->
                Button(onClick = { onEvent(PlansEvent.ApplySuggestion(suggestion)) }) { Text(label) }
            }
            TextButton(onClick = { onEvent(PlansEvent.DismissSuggestion(plan.id)) }) { Text(stringResource(R.string.suggestion_keep)) }
        }
    }
}

@Composable
private fun suggestionBody(suggestion: PlanSuggestion): String {
    val plan = suggestion.plan
    val day = Format.dayLong(plan.start.toLocalDate())
    val time = Format.time(plan.start.toLocalTime())
    return when (suggestion) {
        is PlanSuggestion.MoveToDay -> stringResource(
            R.string.suggestion_move_body,
            day,
            time,
            plan.placeName,
            Format.dayLong(suggestion.date),
            suggestion.maxTemperatureC
        )
        is PlanSuggestion.PreferIndoor -> stringResource(R.string.suggestion_indoor_body, day, time, suggestion.precipitationProbability)
    }
}

@Composable
private fun suggestionAction(suggestion: PlanSuggestion): String? = when (suggestion) {
    is PlanSuggestion.MoveToDay -> stringResource(R.string.suggestion_move_action, Format.dayLong(suggestion.date))
    is PlanSuggestion.PreferIndoor -> null
}
