package com.kampplus.havali.presentation.prepare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.OutfitPiece
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.components.ChecklistCard
import com.kampplus.havali.presentation.components.HavaliIcons
import com.kampplus.havali.presentation.components.ModeSelector
import com.kampplus.havali.presentation.components.SectionHeader
import com.kampplus.havali.presentation.components.WeatherBackdrop
import com.kampplus.havali.presentation.theme.HavaliTheme

/** "Bugün çıkarken": ulaşım şekli, ne giysem, çıkmadan önce listesi. */
@Composable
fun DailyPrepSection(daily: DailyPrepUi, onEvent: (PrepareEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(title = stringResource(R.string.prepare_how_today))
        ModeSelector(modes = TravelMode.commuteModes, selected = daily.mode, onSelect = { onEvent(PrepareEvent.SelectDailyMode(it)) })
        OutfitCard(daily)
        ChecklistCard(
            title = stringResource(R.string.prepare_before_leaving),
            sections = daily.sections,
            checked = daily.checked,
            done = daily.done,
            total = daily.total,
            onToggle = { onEvent(PrepareEvent.ToggleDaily(it)) }
        )
    }
}

/** "Ne giysem?" kartı: arkasında havanın sönük deseni (yağmurda damlalar, güneşte güneşler). */
@Composable
private fun OutfitCard(daily: DailyPrepUi) {
    Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surface)) {
        WeatherBackdrop(daily.kind, modifier = Modifier.matchParentSize(), alpha = CARD_PATTERN_ALPHA, tint = HavaliTheme.colors.deep)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.outfit_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.outfit_range,
                    Format.degrees(daily.nowC),
                    Format.degrees(daily.laterC),
                    Format.degrees(daily.minPersonalC)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = HavaliTheme.colors.mutedOnSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutfitTile(daily.outfit.base, HavaliIcons.Thermometer, Modifier.weight(1f))
                daily.outfit.outer?.let { OutfitTile(it, HavaliIcons.Umbrella, Modifier.weight(1f)) }
                OutfitTile(daily.outfit.shoes, HavaliIcons.Walk, Modifier.weight(1f))
            }
        }
    }
}

private const val CARD_PATTERN_ALPHA = 0.1f

@Composable
private fun OutfitTile(piece: OutfitPiece, icon: ImageVector, modifier: Modifier) {
    Column(
        modifier = modifier.background(
            HavaliTheme.colors.infoTint,
            MaterialTheme.shapes.small
        ).padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
        }
        Text(
            text = stringResource(piece.labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = HavaliTheme.colors.onInfoTint,
            textAlign = TextAlign.Center
        )
    }
}
