package com.kampplus.havali.presentation.road

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.havali.R
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.dataOrNull
import com.kampplus.havali.presentation.components.ErrorView
import com.kampplus.havali.presentation.components.LoadingView
import com.kampplus.havali.presentation.components.ScrollbarColumn
import com.kampplus.havali.presentation.components.SectionHeader
import com.kampplus.havali.presentation.components.SegmentedTabs
import com.kampplus.havali.presentation.components.WeatherBackdrop
import com.kampplus.havali.presentation.theme.HavaliTheme

@Composable
fun RoadHealthRoute(
    twoPane: Boolean,
    contentPadding: PaddingValues,
    viewModel: RoadHealthViewModel = viewModel(factory = RoadHealthViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RoadHealthScreen(state, viewModel::onEvent, twoPane, contentPadding)
}

/**
 * Yol & Sağlık. Dar ekranda iki sekme; geniş ekranda iki sütun yan yana (sekmeye gerek kalmaz).
 */
@Composable
fun RoadHealthScreen(state: RoadHealthUiState, onEvent: (RoadHealthEvent) -> Unit, twoPane: Boolean, contentPadding: PaddingValues) {
    val padding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = contentPadding.calculateTopPadding() + 12.dp,
        bottom = contentPadding.calculateBottomPadding() + 24.dp
    )
    Box(modifier = Modifier.fillMaxSize()) {
        state.road.dataOrNull()?.let { WeatherBackdrop(it.kind, modifier = Modifier.fillMaxSize()) }
        RoadHealthContent(state, onEvent, twoPane, padding)
    }
}

@Composable
private fun RoadHealthContent(state: RoadHealthUiState, onEvent: (RoadHealthEvent) -> Unit, twoPane: Boolean, padding: PaddingValues) {
    when (twoPane) {
        true -> Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScreenColumn(padding, Modifier.weight(1f)) {
                header(state)
                roadContent(state, onEvent)
            }
            ScreenColumn(padding, Modifier.weight(1f)) {
                item { SectionHeader(title = stringResource(R.string.tab_health)) }
                healthContent(state, onEvent)
            }
        }
        false -> ScreenColumn(padding, Modifier.fillMaxSize()) {
            header(state)
            item(key = "tabs") {
                SegmentedTabs(
                    options = listOf(stringResource(R.string.tab_road), stringResource(R.string.tab_health)),
                    selectedIndex = state.tab.ordinal,
                    onSelect = { onEvent(RoadHealthEvent.SelectTab(RoadHealthTab.entries[it])) }
                )
            }
            when (state.tab) {
                RoadHealthTab.ROAD -> roadContent(state, onEvent)
                RoadHealthTab.HEALTH -> healthContent(state, onEvent)
            }
        }
    }
}

@Composable
private fun ScreenColumn(padding: PaddingValues, modifier: Modifier, content: LazyListScope.() -> Unit) =
    ScrollbarColumn(contentPadding = padding, modifier = modifier.fillMaxHeight(), content = content)

private fun LazyListScope.header(state: RoadHealthUiState) = item(key = "header") {
    Column {
        Text(
            text = stringResource(R.string.road_health_kicker, state.placeName),
            style = MaterialTheme.typography.bodyMedium,
            color = HavaliTheme.colors.mutedOnBackground
        )
        Text(
            text = stringResource(R.string.nav_road_health),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun LazyListScope.roadContent(state: RoadHealthUiState, onEvent: (RoadHealthEvent) -> Unit) {
    item(key = "road") {
        when (val road = state.road) {
            LoadState.Loading -> LoadingView()
            LoadState.Empty -> Unit
            is LoadState.Error -> ErrorView(error = road.error, onRetry = { onEvent(RoadHealthEvent.Retry) })
            is LoadState.Success -> RoadRiskSection(road.data, onEvent)
        }
    }
    item(key = "reports") { ReportsSection(state.reports, state.placeName, state.reportFormOpen, state.reportSent, onEvent) }
}

private fun LazyListScope.healthContent(state: RoadHealthUiState, onEvent: (RoadHealthEvent) -> Unit) {
    when (val health = state.health) {
        LoadState.Loading -> item(key = "health-loading") { LoadingView() }
        LoadState.Empty -> Unit
        is LoadState.Error -> item(key = "health-error") { ErrorView(error = health.error, onRetry = { onEvent(RoadHealthEvent.Retry) }) }
        is LoadState.Success -> {
            item(key = "comfort") { ComfortProfileCard(health.data.profile) }
            item(key = "uv") { UvCard(health.data, onApply = { onEvent(RoadHealthEvent.ApplySunscreen) }) }
            item(key = "care") { CareTipsSection(health.data) }
        }
    }
    item(key = "air") { AirQualitySection(state.airQuality, onRetry = { onEvent(RoadHealthEvent.Retry) }) }
}
