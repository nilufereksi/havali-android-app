package com.kampplus.havali.presentation.road

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.CorridorTraffic
import com.kampplus.havali.domain.model.HourRisk
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.RiskLevel
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.TrafficLevel
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherAlert
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.bodyRes
import com.kampplus.havali.presentation.common.icon
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.common.suitability
import com.kampplus.havali.presentation.common.titleRes
import com.kampplus.havali.presentation.common.valueRes
import com.kampplus.havali.presentation.components.ChoiceChip
import com.kampplus.havali.presentation.components.HavaliCard
import com.kampplus.havali.presentation.components.HavaliIcons
import com.kampplus.havali.presentation.components.ModeSelector
import com.kampplus.havali.presentation.components.Pill
import com.kampplus.havali.presentation.components.RaisedCard
import com.kampplus.havali.presentation.components.SectionHeader
import com.kampplus.havali.presentation.components.colors
import com.kampplus.havali.presentation.theme.HavaliTheme

/** Şehir ve ulaşım seçici + dikkat çeken risk kartı + trafik haritası + tehlike kutucukları + ekipman. */
@Composable
fun RoadRiskSection(road: RoadUi, onEvent: (RoadHealthEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            road.cities.forEach { city ->
                ChoiceChip(label = city.name, selected = city.id == road.city.id, onClick = {
                    onEvent(RoadHealthEvent.SelectCity(city.id))
                }, icon = HavaliIcons.Map)
            }
        }
        TrafficSection(road, onEvent)
        road.alerts.forEach { AlertStrip(it) }
        ModeSelector(modes = TravelMode.commuteModes, selected = road.mode, onSelect = { onEvent(RoadHealthEvent.SelectMode(it)) })
        RiskHero(road)
        HazardGrid(road)
        Text(
            text = stringResource(R.string.road_gear, road.gear.map { stringResource(it.labelRes) }.joinToString(", ")),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Riskin rengini taşıyan büyük kart: rahat günde turkuaz, dikkatte sarı, riskte kırmızı.
 * Köşedeki araç simgesi riske göre sakin süzülür ya da dur-kalk titrer.
 */
@Composable
private fun RiskHero(road: RoadUi) {
    val risk = road.risk
    val tone = risk.level.suitability.heroColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(tone.start, tone.end)), MaterialTheme.shapes.large)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.road_risk_title, stringResource(road.mode.labelRes)).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = tone.ink,
                modifier = Modifier.weight(1f)
            )
            MovingVehicle(road.mode, risk.level.suitability, tone.ink)
        }
        Text(
            text = stringResource(R.string.road_hero_level, stringResource(risk.level.labelRes)),
            style = MaterialTheme.typography.headlineLarge,
            color = tone.ink
        )
        Text(text = riskHeadline(road), style = MaterialTheme.typography.titleMedium, color = tone.ink)
        Text(
            text = stringResource(R.string.road_risk_window),
            style = MaterialTheme.typography.bodySmall,
            color = tone.ink.copy(alpha = 0.8f)
        )
        HourBars(risk.hours, tone.ink)
    }
}

private data class HeroColors(val start: Color, val end: Color, val ink: Color)

@Composable
private fun Suitability.heroColors(): HeroColors = when (this) {
    Suitability.GOOD -> HeroColors(LIGHT_COLOR, HavaliTheme.colors.raised, Color.White)
    Suitability.CAUTION -> HeroColors(MaterialTheme.colorScheme.primary, MODERATE_COLOR, HavaliTheme.colors.deep)
    Suitability.BAD -> HeroColors(HEAVY_COLOR, HavaliTheme.colors.deep, Color.White)
}

/** Riskli günde araç dur-kalk yapar (kısa, sık sarsıntı); rahat günde sakin bir şekilde ileri geri süzülür. */
@Composable
private fun MovingVehicle(mode: TravelMode, suitability: Suitability, tint: Color) {
    val (distance, duration) = when (suitability) {
        Suitability.GOOD -> 18f to 1400
        Suitability.CAUTION -> 10f to 900
        Suitability.BAD -> 4f to 220
    }
    val shift = rememberInfiniteTransition(label = "vehicle").animateFloat(
        initialValue = -distance,
        targetValue = distance,
        animationSpec = infiniteRepeatable(tween(durationMillis = duration), repeatMode = RepeatMode.Reverse),
        label = "shift"
    )
    Icon(
        imageVector = mode.icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(34.dp).graphicsLayer { translationX = shift.value * density }
    )
}

/** Fırtına, dalga, sis uyarısı: kırmızı ünlemli şerit. */
@Composable
private fun AlertStrip(alert: WeatherAlert) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HavaliTheme.colors.dangerTint, MaterialTheme.shapes.small).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(30.dp).background(MaterialTheme.colorScheme.error, CircleShape), contentAlignment = Alignment.Center) {
            Icon(
                HavaliIcons.Exclamation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(alert.type.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = HavaliTheme.colors.onDangerTint
            )
            Text(
                text = stringResource(alert.type.bodyRes, alert.placeName, alert.value),
                style = MaterialTheme.typography.bodySmall,
                color = HavaliTheme.colors.onDangerTint
            )
        }
    }
}

/** Trafik: harita, saat seçici, seçili aksın ayrıntısı ve renk açıklaması. */
@Composable
private fun TrafficSection(road: RoadUi, onEvent: (RoadHealthEvent) -> Unit) {
    val traffic = road.traffic
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = stringResource(R.string.traffic_title, road.city.name))
        TrafficMap(
            traffic = traffic,
            contentDescription = stringResource(R.string.traffic_map_description, road.city.name),
            onSelect = { onEvent(RoadHealthEvent.SelectCorridor(it)) },
            modifier = Modifier.fillMaxWidth().height(TRAFFIC_MAP_HEIGHT).clip(MaterialTheme.shapes.large)
        )
        TrafficSteps(traffic, onEvent)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TrafficLegend(LIGHT_COLOR, R.string.traffic_light)
            TrafficLegend(MODERATE_COLOR, R.string.traffic_moderate)
            TrafficLegend(HEAVY_COLOR, R.string.traffic_heavy)
            TrafficLegend(ALTERNATIVE_COLOR, R.string.traffic_alternative)
        }
        traffic.selected?.let { CorridorCard(it, picked = traffic.userPicked) }
        RouteVerdict(traffic, onSelect = { onEvent(RoadHealthEvent.SelectCorridor(it)) })
    }
}

@Composable
private fun TrafficSteps(traffic: TrafficUi, onEvent: (RoadHealthEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilledIconButton(
            onClick = { onEvent(RoadHealthEvent.ToggleTrafficPlay) },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = HavaliTheme.colors.raised),
            modifier = Modifier.size(38.dp)
        ) {
            Icon(playIcon(traffic.playing), contentDescription = stringResource(playLabel(traffic.playing)), tint = Color.White)
        }
        Row(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            traffic.stepTimes.forEachIndexed { index, time ->
                ChoiceChip(
                    label = stepLabel(index, Format.time(time)),
                    selected = index == traffic.step,
                    onClick = { onEvent(RoadHealthEvent.SelectTrafficStep(index)) }
                )
            }
        }
    }
}

@Composable
private fun stepLabel(index: Int, time: String): String = when (index) {
    0 -> stringResource(R.string.now)
    else -> time
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
private fun TrafficLegend(color: Color, label: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(width = 18.dp, height = 6.dp).background(color, CircleShape))
        Text(text = stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun CorridorCard(item: CorridorTraffic, picked: Boolean) {
    HavaliCard {
        Text(
            text = stringResource(corridorKicker(picked)),
            style = MaterialTheme.typography.labelSmall,
            color = HavaliTheme.colors.mutedOnSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = item.corridor.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Pill(text = stringResource(item.level.labelRes), container = item.level.color(), content = Color.White)
        }
        Text(text = stringResource(R.string.traffic_delay, item.delayMinutes), style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Seçilen yol trafikteyse kırmızı kutu, altında yeşil "alternatif" kutusu ve seçme düğmesi.
 * Yan yol seçildiyse yeşil onay kutusu gösterilir.
 */
@Composable
private fun RouteVerdict(traffic: TrafficUi, onSelect: (String) -> Unit) {
    val selected = traffic.selected ?: return
    val alternative = traffic.focusAlternative
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        selected.takeIf { it.level != TrafficLevel.LIGHT && !it.corridor.isAlternative }?.let {
            VerdictBox(
                container = HavaliTheme.colors.dangerTint,
                content = HavaliTheme.colors.onDangerTint,
                text = stringResource(R.string.route_busy, it.corridor.name, stringResource(it.level.labelRes), it.delayMinutes)
            )
        }
        alternative?.let { route ->
            VerdictBox(
                container = GREEN_TINT,
                content = GREEN_INK,
                text = stringResource(
                    R.string.route_alternative,
                    route.alternative.corridor.name,
                    route.alternative.delayMinutes,
                    route.savedMinutes
                ),
                action = stringResource(R.string.route_choose),
                onAction = { onSelect(route.alternative.corridor.id) }
            )
        }
        selected.takeIf { it.corridor.isAlternative }?.let {
            VerdictBox(
                container = GREEN_TINT,
                content = GREEN_INK,
                text = stringResource(R.string.route_chosen, it.corridor.name, it.delayMinutes)
            )
        }
    }
}

@Composable
private fun VerdictBox(container: Color, content: Color, text: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().background(
            container,
            MaterialTheme.shapes.small
        ).padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = content, modifier = Modifier.weight(1f))
        action?.let {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = ALTERNATIVE_COLOR, contentColor = Color.White)
            ) {
                Text(it)
            }
        }
    }
}

private val GREEN_TINT = Color(0xFFDDF5EA)
private val GREEN_INK = Color(0xFF0E5A3A)

private fun corridorKicker(picked: Boolean): Int = when (picked) {
    true -> R.string.traffic_selected
    false -> R.string.traffic_busiest
}

private fun TrafficLevel.color(): Color = when (this) {
    TrafficLevel.LIGHT -> LIGHT_COLOR
    TrafficLevel.MODERATE -> MODERATE_COLOR
    TrafficLevel.HEAVY -> HEAVY_COLOR
}

/** Başlık cümlesi en önemli tehlikeden kurulur; tehlike yoksa "Yollar rahat". */
@Composable
private fun riskHeadline(road: RoadUi): String = road.risk.hazards.firstOrNull()
    ?.let { stringResource(R.string.road_headline_hazard, stringResource(it.type.titleRes), stringResource(it.type.valueRes, it.value)) }
    ?: stringResource(R.string.road_headline_clear)

/** Saat saat risk çubukları: yükseklik ve renk riski gösterir. */
@Composable
private fun HourBars(hours: List<HourRisk>, ink: Color) {
    val description = stringResource(R.string.road_bars_description)
    Row(
        modifier = Modifier.fillMaxWidth().height(BAR_AREA).semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        hours.forEachIndexed { index, hour ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RiskBar(hour.level, ink)
                Text(
                    text = hourLabel(index, hour),
                    style = MaterialTheme.typography.labelSmall,
                    color = HavaliTheme.colors.mutedOnSurface,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RiskBar(level: RiskLevel, ink: Color) {
    val fraction by animateFloatAsState((level.ordinal + 1f) / RiskLevel.entries.size, label = "bar")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BAR_MAX * fraction)
            .background(
                ink.copy(alpha = BAR_BASE_ALPHA + BAR_STEP_ALPHA * level.ordinal),
                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
            )
    )
}

/** Her üç saatte bir etiket: kalabalığı önler. */
private fun hourLabel(index: Int, hour: HourRisk): String = when (index % LABEL_EVERY) {
    0 -> Format.hour(hour.time.toLocalTime())
    else -> ""
}

/** Küçük kare kutucuklar: kaygan zemin, rüzgâr, görüş... ve kişisel hissedilen. */
@Composable
private fun HazardGrid(road: RoadUi) {
    val tiles = road.risk.hazards.map { hazard ->
        HazardTileModel(hazard.type.icon, stringResource(hazard.type.titleRes), stringResource(hazard.type.valueRes, hazard.value))
    } + HazardTileModel(HavaliIcons.Thermometer, stringResource(R.string.road_personal_tile), Format.degrees(road.personalC))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(TILES_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { HazardTile(it, Modifier.weight(1f)) }
                repeat(TILES_PER_ROW - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

private data class HazardTileModel(val icon: ImageVector, val title: String, val value: String)

@Composable
private fun HazardTile(tile: HazardTileModel, modifier: Modifier) {
    RaisedCard(modifier = modifier) {
        Icon(tile.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(text = tile.title, style = MaterialTheme.typography.titleSmall)
        Text(text = tile.value, style = MaterialTheme.typography.headlineSmall)
    }
}

/** Hava muhbiri: topluluk raporları, rapor ekleme ve "hâlâ var / kalktı" onayları. */
@Composable
fun ReportsSection(
    reports: List<CommunityReport>,
    placeName: String,
    formOpen: Boolean,
    sent: Boolean,
    onEvent: (RoadHealthEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = stringResource(R.string.reports_title)) {
            Button(onClick = { onEvent(RoadHealthEvent.OpenReportForm) }) { Text(stringResource(R.string.reports_add)) }
        }
        Text(
            text = stringResource(R.string.reports_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = HavaliTheme.colors.mutedOnBackground
        )
        AnimatedVisibility(visible = formOpen) { ReportForm(placeName, onSend = { onEvent(RoadHealthEvent.SendReport(it)) }) }
        AnimatedVisibility(visible = sent) {
            Text(
                text = stringResource(R.string.reports_thanks),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        reports.forEach { ReportRow(it, onEvent) }
    }
}

@Composable
private fun ReportForm(placeName: String, onSend: (ReportType) -> Unit) {
    RaisedCard {
        Text(text = stringResource(R.string.reports_question, placeName), style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportType.entries.forEach { type ->
                ChoiceChip(label = stringResource(type.labelRes), selected = false, onClick = { onSend(type) })
            }
        }
    }
}

@Composable
private fun ReportRow(report: CommunityReport, onEvent: (RoadHealthEvent) -> Unit) {
    HavaliCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    HavaliIcons.Alert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(report.type.labelRes), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(R.string.reports_where_when, report.placeName, minutesAgo(report.minutesAgo)),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnSurface
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onEvent(RoadHealthEvent.ToggleConfirm(report.id)) }) {
                Text(stringResource(confirmLabel(report.confirmedByMe), report.confirmations))
            }
            TextButton(onClick = { onEvent(RoadHealthEvent.Dismiss(report.id)) }) { Text(stringResource(R.string.reports_gone)) }
        }
    }
}

@Composable
private fun minutesAgo(minutes: Int): String = when (minutes) {
    0 -> stringResource(R.string.just_now)
    else -> stringResource(R.string.minutes_ago, minutes)
}

private fun confirmLabel(confirmedByMe: Boolean): Int = when (confirmedByMe) {
    true -> R.string.reports_confirmed
    false -> R.string.reports_still_there
}

private val BAR_AREA = 92.dp
private val TRAFFIC_MAP_HEIGHT = 300.dp
private const val BAR_BASE_ALPHA = 0.3f
private const val BAR_STEP_ALPHA = 0.16f
private val BAR_MAX = 72.dp
private const val LABEL_EVERY = 3
private const val TILES_PER_ROW = 2
