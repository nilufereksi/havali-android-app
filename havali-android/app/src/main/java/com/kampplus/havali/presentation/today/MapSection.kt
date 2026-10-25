package com.kampplus.havali.presentation.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.dataOrNull
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.components.ErrorView
import com.kampplus.havali.presentation.components.HavaliIcons
import com.kampplus.havali.presentation.components.LoadingView
import com.kampplus.havali.presentation.theme.HavaliTheme
import com.kampplus.havali.presentation.today.map.WeatherMap
import kotlin.math.roundToInt

/**
 * Haritanın ve üstündeki katmanların (araç çubuğu, çıkış uyarısı, hava kartı, arama paneli) yerleşimi.
 * Bütün kararlar [TodayUiState]'ten gelir; bu dosya yalnızca yerleştirir.
 */
@Composable
fun MapSection(state: TodayUiState, onEvent: (TodayEvent) -> Unit, onScrollHint: (() -> Unit)?, modifier: Modifier = Modifier) {
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.background(HavaliTheme.colors.map.sea)) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            WeatherMap(
                geometry = state.geometry.dataOrNull(),
                markers = state.markers,
                reports = state.reportPoints,
                campsites = state.campsitePoints,
                camera = state.camera,
                onPlaceTap = { onEvent(TodayEvent.SelectPlace(it)) },
                onEmptyTap = { onEvent(TodayEvent.ShowTurkey) },
                contentDescription = stringResource(R.string.map_description),
                modifier = Modifier.fillMaxSize()
            )
            MapStatus(state, onRetry = { onEvent(TodayEvent.Retry) }, modifier = Modifier.align(Alignment.Center))
            Column(
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MapToolbar(state, onEvent, onSearch = { searchOpen = true })
                DepartureToast(state.nextDeparture)
            }
            FocusCard(state, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
            Legend(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp))
            onScrollHint?.let { ScrollHint(onClick = it, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) }
            this@Column.AnimatedVisibility(
                visible = searchOpen,
                enter = fadeIn() + slideInVertically { -it / 4 },
                exit = fadeOut() + slideOutVertically { -it / 4 },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(12.dp)
            ) {
                MapSearchPanel(
                    search = state.search,
                    selectedId = state.selected?.id,
                    onEvent = onEvent,
                    onClose = { searchOpen = false }
                )
            }
        }
        TimeSlider(state, onEvent)
    }
}

/** Harita verisi yüklenirken veya hata varsa haritanın ortasında görünür. */
@Composable
private fun MapStatus(state: TodayUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (val geometry = state.geometry) {
        LoadState.Loading -> LoadingView(modifier = modifier, label = stringResource(R.string.map_loading))
        is LoadState.Error -> ErrorView(error = geometry.error, onRetry = onRetry, modifier = modifier)
        is LoadState.Success, LoadState.Empty -> WeatherStatus(state.focus, onRetry, modifier)
    }
}

@Composable
private fun WeatherStatus(focus: LoadState<FocusWeather>, onRetry: () -> Unit, modifier: Modifier) {
    (focus as? LoadState.Error)?.let {
        Surface(
            color = HavaliTheme.colors.map.labelBackground.copy(alpha = 0.92f),
            shape = MaterialTheme.shapes.medium,
            modifier = modifier.padding(24.dp)
        ) {
            ErrorView(error = it.error, onRetry = onRetry)
        }
    }
}

@Composable
private fun MapToolbar(state: TodayUiState, onEvent: (TodayEvent) -> Unit, onSearch: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.padding(end = THEME_BUTTON_SPACE),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            DataSourceToggle(state.dataSource, onToggle = { onEvent(TodayEvent.ToggleDataSource) })
            FilledIconButton(
                onClick = onSearch,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_open),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        QuickPlaces(state, onEvent)
    }
}

/**
 * Hızlı geçiş çipleri: Türkiye, iki odak şehir (İzmir, Gemlik) ve öteki büyük şehirler.
 * Seçili olan sarıdır; kaydırılabilir satır dar ekranda da sığar.
 */
@Composable
private fun QuickPlaces(state: TodayUiState, onEvent: (TodayEvent) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        GlassChip(
            text = stringResource(R.string.map_back_to_turkey),
            onClick = { onEvent(TodayEvent.ShowTurkey) },
            container = chipColor(state.selected == null)
        )
        state.quickPlaces.forEach { place ->
            GlassChip(
                text = place.name,
                onClick = { onEvent(TodayEvent.SelectPlace(place)) },
                container = chipColor(state.selected?.id == place.id)
            )
        }
    }
}

@Composable
private fun chipColor(selected: Boolean): Color = when (selected) {
    true -> MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
    false -> HavaliTheme.colors.map.labelBackground.copy(alpha = 0.85f)
}

/** Haritanın altında nabız gibi atan aşağı ok: ayrıntıların aşağıda olduğunu gösterir, dokununca kaydırır. */
@Composable
private fun ScrollHint(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bounce = rememberInfiniteTransition(label = "hint").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = HINT_MS), repeatMode = RepeatMode.Reverse),
        label = "bounce"
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = bounce.value * HINT_TRAVEL_DP * density
                alpha = 0.6f + 0.4f * bounce.value
            }
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            HavaliIcons.ChevronDown,
            contentDescription = stringResource(R.string.scroll_hint),
            tint = HavaliTheme.colors.map.labelBackground
        )
    }
}

private val THEME_BUTTON_SPACE = 48.dp
private const val HINT_MS = 700
private const val HINT_TRAVEL_DP = 6f

/** DEMO: hikâyeyi anlatan senaryo verisi. CANLI: Open-Meteo. Seçim cihazda saklanır. */
@Composable
private fun DataSourceToggle(source: DataSource, onToggle: () -> Unit) {
    val (label, container) = when (source) {
        DataSource.DEMO -> stringResource(R.string.source_demo) to HavaliTheme.colors.map.labelBackground.copy(alpha = 0.85f)
        DataSource.LIVE -> stringResource(R.string.source_live) to HavaliTheme.colors.raised
    }
    GlassChip(
        text = label,
        onClick = onToggle,
        container = container,
        leading = { Box(Modifier.size(8.dp).background(source.dotColor(), CircleShape)) }
    )
}

@Composable
private fun DataSource.dotColor(): Color = when (this) {
    DataSource.DEMO -> MaterialTheme.colorScheme.primary
    DataSource.LIVE -> HavaliTheme.colors.map.wind
}

@Composable
private fun GlassChip(
    text: String,
    onClick: () -> Unit,
    container: Color = HavaliTheme.colors.map.labelBackground.copy(alpha = 0.85f),
    leading: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        leading()
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = textOn(container))
    }
}

@Composable
private fun textOn(container: Color): Color = when (container.luminance() > LIGHT_LUMINANCE) {
    true -> MaterialTheme.colorScheme.onPrimary
    false -> Color.White
}

private const val LIGHT_LUMINANCE = 0.5f
private val Yellow = Color(0xFFFFC20E)
private val MAP_MUTED = Color(0xFFB9C7E0)

/** "Yola çık: 17:10" uyarısı: bugünün sıradaki açık hava planından gelir; dokununca kapanır. */
@Composable
private fun DepartureToast(item: TodayPlanItem?) {
    var dismissed by rememberSaveable { mutableStateOf(false) }
    AnimatedVisibility(visible = item?.departure != null && !dismissed, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
        item?.let { DepartureToastContent(it, onDismiss = { dismissed = true }) }
    }
}

@Composable
private fun DepartureToastContent(item: TodayPlanItem, onDismiss: () -> Unit) {
    item.departure?.let { advice ->
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.widthIn(max = 420.dp).clickable(onClick = onDismiss)
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(HavaliIcons.Umbrella, contentDescription = null, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        text = stringResource(R.string.toast_leave_at, Format.time(advice.leaveAt)),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(
                            R.string.toast_plan_weather,
                            item.plan.title,
                            Format.time(item.plan.start.toLocalTime()),
                            item.weather?.precipitationProbability ?: 0
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** Sol alttaki yarı saydam hava kartı: odaktaki yerin seçili saatteki havası ve "Sana göre" değeri. */
@Composable
private fun FocusCard(state: TodayUiState, modifier: Modifier = Modifier) {
    state.focus.dataOrNull()?.let { focus ->
        Surface(
            color = HavaliTheme.colors.map.labelBackground.copy(alpha = 0.82f),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.small,
            modifier = modifier.widthIn(max = 168.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = stringResource(R.string.focus_title, focusName(focus), stepLabel(state)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MAP_MUTED,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = Format.degrees(focus.snapshot.temperatureC), style = MaterialTheme.typography.headlineLarge)
                Text(text = stringResource(focus.snapshot.kind.labelRes), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(
                        R.string.focus_detail,
                        focus.snapshot.precipitationProbability,
                        focus.snapshot.windKmh.roundToInt()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MAP_MUTED
                )
                Text(
                    text = stringResource(R.string.focus_personal, Format.degrees(focus.personalC)),
                    style = MaterialTheme.typography.labelMedium,
                    color = HavaliTheme.colors.map.labelBackground,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Yellow, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                AnimatedVisibility(visible = state.selected == null) {
                    Text(
                        text = stringResource(R.string.map_tap_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MAP_MUTED
                    )
                }
            }
        }
    }
}

@Composable
private fun focusName(focus: FocusWeather): String = when (focus.isHome) {
    true -> stringResource(R.string.focus_home, focus.place.name)
    false -> focus.place.name
}

@Composable
private fun stepLabel(state: TodayUiState): String = when (state.step) {
    0 -> stringResource(R.string.now)
    else -> state.stepTimes.getOrNull(state.step)?.let(Format::time).orEmpty()
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.background(
            HavaliTheme.colors.map.labelBackground.copy(alpha = 0.8f),
            RoundedCornerShape(percent = 50)
        ).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        Text(text = stringResource(R.string.map_legend_reports), style = MaterialTheme.typography.labelSmall, color = Color.White)
        Box(Modifier.size(8.dp).background(HavaliTheme.colors.map.rain, CircleShape))
        Text(text = stringResource(R.string.map_legend_rain), style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

/** Zaman çubuğu: haritadaki animasyonları önümüzdeki 12 saate kaydırır. Oynat/duraklat ile otomatik ilerler. */
@Composable
private fun TimeSlider(state: TodayUiState, onEvent: (TodayEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HavaliTheme.colors.map.labelBackground).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledIconButton(
            onClick = { onEvent(TodayEvent.TogglePlay) },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = HavaliTheme.colors.raised),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = playIcon(state.playing),
                contentDescription = stringResource(playLabel(state.playing)),
                tint = Color.White
            )
        }
        Row(modifier = Modifier.weight(1f).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.stepTimes.forEachIndexed { index, time ->
                StepChip(
                    label = stepChipLabel(index, Format.time(time)),
                    selected = index == state.step,
                    onClick = { onEvent(TodayEvent.SelectStep(index)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun playIcon(playing: Boolean) = when (playing) {
    true -> HavaliIcons.Pause
    false -> Icons.Filled.PlayArrow
}

private fun playLabel(playing: Boolean) = when (playing) {
    true -> R.string.timeline_pause
    false -> R.string.timeline_play
}

@Composable
private fun stepChipLabel(index: Int, time: String): String = when (index) {
    0 -> stringResource(R.string.now)
    else -> time
}

@Composable
private fun StepChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (container, content) = when (selected) {
        true -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        false -> Color.Transparent to MAP_MUTED
    }
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(container)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = content, maxLines = 1)
    }
}
