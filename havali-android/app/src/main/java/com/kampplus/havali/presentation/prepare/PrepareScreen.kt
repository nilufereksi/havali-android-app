package com.kampplus.havali.presentation.prepare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.kampplus.havali.domain.model.SummaryFact
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.dataOrNull
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.common.shortRes
import com.kampplus.havali.presentation.common.titleRes
import com.kampplus.havali.presentation.components.BannerTone
import com.kampplus.havali.presentation.components.DropBanner
import com.kampplus.havali.presentation.components.ErrorView
import com.kampplus.havali.presentation.components.LoadingView
import com.kampplus.havali.presentation.components.Pill
import com.kampplus.havali.presentation.components.ScrollbarColumn
import com.kampplus.havali.presentation.components.SegmentedTabs
import com.kampplus.havali.presentation.components.WeatherBackdrop
import com.kampplus.havali.presentation.theme.HavaliTheme

@Composable
fun PrepareRoute(
    twoPane: Boolean,
    contentPadding: PaddingValues,
    viewModel: PrepareViewModel = viewModel(factory = PrepareViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PrepareScreen(state, viewModel::onEvent, twoPane, contentPadding)
}

/**
 * Hazırlık: dar ekranda "Bugün çıkarken | Valiz" sekmeleri, geniş ekranda ikisi yan yana.
 * Yağmur varsa ekrana girerken "Yağmur başladı!" bildirimi düşer.
 */
@Composable
fun PrepareScreen(state: PrepareUiState, onEvent: (PrepareEvent) -> Unit, twoPane: Boolean, contentPadding: PaddingValues) {
    val padding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = contentPadding.calculateTopPadding() + 12.dp,
        bottom = contentPadding.calculateBottomPadding() + 24.dp
    )
    Box(modifier = Modifier.fillMaxSize()) {
        state.daily.dataOrNull()?.let { WeatherBackdrop(it.kind, modifier = Modifier.fillMaxSize()) }
        when (twoPane) {
            true -> Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrepareColumn(padding, Modifier.weight(1f)) {
                    header(state)
                    hero(state)
                    daily(state, onEvent)
                }
                PrepareColumn(padding, Modifier.weight(1f)) { trip(state, onEvent) }
            }
            false -> PrepareColumn(padding, Modifier.fillMaxSize()) {
                header(state)
                hero(state)
                item(key = "tabs") {
                    SegmentedTabs(
                        options = listOf(stringResource(R.string.prepare_tab_today), stringResource(R.string.prepare_tab_trip)),
                        selectedIndex = state.tab.ordinal,
                        onSelect = { onEvent(PrepareEvent.SelectTab(PrepareTab.entries[it])) }
                    )
                }
                when (state.tab) {
                    PrepareTab.TODAY -> daily(state, onEvent)
                    PrepareTab.TRIP -> trip(state, onEvent)
                }
            }
        }
        state.daily.dataOrNull()?.let { daily -> PrepareBanner(daily, Modifier.padding(top = contentPadding.calculateTopPadding())) }
    }
}

/**
 * Ekrana girerken düşen bildirim. Fırtına / dalga / sis uyarısı varsa kırmızı ünlemle uyarı,
 * yoksa yağmur haberi. İkisi de birkaç saniye görünüp aynı şekilde kapanır.
 */
@Composable
private fun PrepareBanner(daily: DailyPrepUi, modifier: Modifier) {
    val alerts = daily.alerts.take(MAX_BANNER_ALERTS)
    when {
        alerts.isNotEmpty() -> DropBanner(
            courierCat = true,
            badge = "!",
            title = stringResource(alerts.first().type.titleRes),
            body = alerts.map { stringResource(it.type.shortRes, it.placeName, it.value) }.joinToString(" · "),
            tone = BannerTone.ALERT,
            modifier = modifier
        )
        daily.rain != null -> DropBanner(
            courierCat = true,
            badge = "☂",
            title = rainTitle(daily.rain),
            body = stringResource(R.string.banner_rain_body),
            modifier = modifier
        )
    }
}

private const val MAX_BANNER_ALERTS = 2

@Composable
private fun rainTitle(rain: SummaryFact): String = when (rain) {
    is SummaryFact.RainArrives -> stringResource(R.string.banner_rain_soon, Format.time(rain.at))
    else -> stringResource(R.string.banner_rain_now)
}

@Composable
private fun PrepareColumn(padding: PaddingValues, modifier: Modifier, content: LazyListScope.() -> Unit) =
    ScrollbarColumn(contentPadding = padding, modifier = modifier.fillMaxHeight(), content = content)

private fun LazyListScope.hero(state: PrepareUiState) {
    state.daily.dataOrNull()?.let { daily -> item(key = "hero") { PrepareHero(daily) } }
}

private fun LazyListScope.header(state: PrepareUiState) = item(key = "header") {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.prepare_kicker),
            style = MaterialTheme.typography.bodyMedium,
            color = HavaliTheme.colors.mutedOnBackground
        )
        Text(
            text = stringResource(R.string.nav_prepare),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Pill(
            text = stringResource(R.string.prepare_profile_chip, stringResource(state.profile.sensitivity.labelRes)),
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary
        )
    }
}

private fun LazyListScope.daily(state: PrepareUiState, onEvent: (PrepareEvent) -> Unit) = item(key = "daily") {
    when (val daily = state.daily) {
        LoadState.Loading -> LoadingView()
        LoadState.Empty -> Unit
        is LoadState.Error -> ErrorView(error = daily.error, onRetry = { onEvent(PrepareEvent.Retry) })
        is LoadState.Success -> DailyPrepSection(daily.data, onEvent)
    }
}

private fun LazyListScope.trip(state: PrepareUiState, onEvent: (PrepareEvent) -> Unit) {
    state.trip?.let { trip -> item(key = "trip") { TripSection(trip, state.profile.sensitivity, onEvent) } }
}
