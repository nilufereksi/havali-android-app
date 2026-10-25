package com.kampplus.havali.presentation.prepare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.ComfortSensitivity
import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.TripWarmth
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.icon
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.components.ChecklistCard
import com.kampplus.havali.presentation.components.EmptyView
import com.kampplus.havali.presentation.components.ErrorView
import com.kampplus.havali.presentation.components.HavaliCard
import com.kampplus.havali.presentation.components.HavaliIcons
import com.kampplus.havali.presentation.components.LoadingView
import com.kampplus.havali.presentation.components.ModeSelector
import com.kampplus.havali.presentation.components.RaisedCard
import com.kampplus.havali.presentation.theme.HavaliTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/** Valizde seçilebilen yolculuk şekilleri, tasarımdaki sırayla. */
private val TRIP_MODES = listOf(TravelMode.PLANE, TravelMode.BUS, TravelMode.CAR, TravelMode.MOTORBIKE, TravelMode.TRAIN)

/** "Valiz": nereden / nereye, kaç gece, nasıl; sonra karşılaştırma ve valiz listesi. */
@Composable
fun TripSection(trip: TripUi, sensitivity: ComfortSensitivity, onEvent: (PrepareEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RouteCard(trip, onEvent)
        ModeSelector(modes = TRIP_MODES, selected = trip.mode, onSelect = { onEvent(PrepareEvent.SelectTripMode(it)) })
        when (val result = trip.result) {
            LoadState.Loading -> LoadingView()
            LoadState.Empty -> EmptyView(title = stringResource(R.string.trip_empty_title), body = stringResource(R.string.trip_empty_body))
            is LoadState.Error -> ErrorView(error = result.error, onRetry = { onEvent(PrepareEvent.Retry) })
            is LoadState.Success -> TripResult(trip, result.data, sensitivity, onEvent)
        }
    }
}

@Composable
private fun RouteCard(trip: TripUi, onEvent: (PrepareEvent) -> Unit) {
    HavaliCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlacePicker(
                label = stringResource(R.string.trip_from),
                selected = trip.origin,
                choices = trip.choices,
                allowNone = true,
                onSelect = { onEvent(PrepareEvent.SelectOrigin(it)) },
                modifier = Modifier.weight(1f)
            )
            PlacePicker(
                label = stringResource(R.string.trip_to),
                selected = trip.destination,
                choices = trip.choices,
                allowNone = false,
                onSelect = { place -> place?.let { onEvent(PrepareEvent.SelectDestination(it)) } },
                modifier = Modifier.weight(1f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.trip_dates,
                    Format.date(trip.start),
                    Format.date(trip.start.plusDays(trip.nights.toLong())),
                    trip.nights
                ),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            NightsButton(icon = HavaliIcons.Minus, label = stringResource(R.string.trip_fewer_nights)) {
                onEvent(PrepareEvent.ChangeNights(-1))
            }
            NightsButton(icon = Icons.Filled.Add, label = stringResource(R.string.trip_more_nights)) {
                onEvent(PrepareEvent.ChangeNights(1))
            }
        }
    }
}

@Composable
private fun NightsButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    FilledIconButton(onClick = onClick, colors = IconButtonDefaults.filledIconButtonColors(containerColor = HavaliTheme.colors.infoTint)) {
        Icon(icon, contentDescription = label, tint = HavaliTheme.colors.onInfoTint)
    }
}

/** Basit açılır seçici: alan dokununca şehir listesi açılır. */
@Composable
private fun PlacePicker(
    label: String,
    selected: Place?,
    choices: List<Place>,
    allowNone: Boolean,
    onSelect: (Place?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(HavaliTheme.colors.infoTint)
                .clickable(role = Role.DropdownList) { expanded = true }
                .padding(12.dp)
        ) {
            Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = HavaliTheme.colors.onInfoTint)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selected?.name ?: stringResource(R.string.trip_none),
                    style = MaterialTheme.typography.titleMedium,
                    color = HavaliTheme.colors.onInfoTint,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = HavaliTheme.colors.onInfoTint)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowNone) {
                DropdownMenuItem(text = { Text(stringResource(R.string.trip_none)) }, onClick = {
                    expanded = false
                    onSelect(null)
                })
            }
            choices.forEach { place ->
                DropdownMenuItem(text = { Text(place.name) }, onClick = {
                    expanded = false
                    onSelect(place)
                })
            }
        }
    }
}

@Composable
private fun TripResult(trip: TripUi, result: TripResultUi, sensitivity: ComfortSensitivity, onEvent: (PrepareEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ComparisonCard(trip, result)
        DaysRow(result.destinationDays)
        TripNotes(result, sensitivity)
        ChecklistCard(
            title = stringResource(R.string.trip_packing_title),
            sections = result.packing.sections,
            checked = result.checked,
            done = result.done,
            total = result.total,
            onToggle = { onEvent(PrepareEvent.ToggleTrip(it)) }
        )
    }
}

/** Çıkış ve varışın ortalama sıcaklığı yan yana; altında valizin karakteri (yazlık / mevsimlik / kışlık). */
@Composable
private fun ComparisonCard(trip: TripUi, result: TripResultUi) {
    HavaliCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            trip.origin?.let { CityTemperature(it.name, result.originDays, Modifier.weight(1f)) }
            CityTemperature(trip.destination.name, result.destinationDays, Modifier.weight(1f))
        }
        trip.origin?.let {
            Text(text = gapText(result.packing.temperatureGapC), style = MaterialTheme.typography.titleSmall)
        }
        WarmthIndicator(result.packing.warmth)
    }
}

/** Valizin karakteri: üç seçenekten hangisi olduğu vurgulanır (salt gösterim, seçilemez). */
@Composable
private fun WarmthIndicator(warmth: TripWarmth) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HavaliTheme.colors.infoTint, MaterialTheme.shapes.small).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TripWarmth.entries.forEach { option ->
            val (container, content) = when (option == warmth) {
                true -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                false -> HavaliTheme.colors.infoTint to HavaliTheme.colors.onInfoTint
            }
            Text(
                text = stringResource(option.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = content,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).background(container, MaterialTheme.shapes.small).padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CityTemperature(name: String, days: List<DailyWeather>, modifier: Modifier) {
    val average = days.map { it.averageTemperatureC }.average()
    val mainKind = days.groupingBy { it.kind }.eachCount().maxByOrNull { it.value }?.key
    RaisedCard(modifier = modifier) {
        Text(text = name, style = MaterialTheme.typography.titleSmall)
        Text(text = Format.degrees(average), style = MaterialTheme.typography.headlineLarge)
        mainKind?.let {
            Text(
                text = stringResource(
                    R.string.trip_city_nights,
                    stringResource(it.labelRes),
                    days.minOf { day ->
                        day.minTemperatureC
                    }.roundToInt()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = HavaliTheme.colors.mutedOnBackground
            )
        }
    }
}

@Composable
private fun gapText(gapC: Int): String = when {
    gapC >= SIMILAR_GAP_C -> stringResource(R.string.trip_gap_colder, gapC)
    gapC <= -SIMILAR_GAP_C -> stringResource(R.string.trip_gap_warmer, abs(gapC))
    else -> stringResource(R.string.trip_gap_similar)
}

@Composable
private fun DaysRow(days: List<DailyWeather>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(days, key = { it.date.toString() }) { day ->
            Column(
                modifier = Modifier.background(
                    HavaliTheme.colors.raised,
                    MaterialTheme.shapes.medium
                ).padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = Format.dayShort(day.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    day.kind.icon,
                    contentDescription = stringResource(day.kind.labelRes),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = Format.degrees(day.maxTemperatureC),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = Format.degrees(day.minTemperatureC),
                    style = MaterialTheme.typography.labelMedium,
                    color = HavaliTheme.colors.mutedOnBackground
                )
            }
        }
    }
}

@Composable
private fun TripNotes(result: TripResultUi, sensitivity: ComfortSensitivity) {
    val notes = listOfNotNull(
        stringResource(R.string.trip_note_rain, result.packing.rainyDays).takeIf { result.packing.rainyDays > 0 },
        stringResource(R.string.trip_note_coldest, result.packing.coldestNightC),
        stringResource(R.string.trip_note_extra_sweater).takeIf { sensitivity == ComfortSensitivity.FEELS_COLD }
    )
    HavaliCard(container = HavaliTheme.colors.accentSoft) {
        notes.forEach { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
    }
}

private const val SIMILAR_GAP_C = 3
