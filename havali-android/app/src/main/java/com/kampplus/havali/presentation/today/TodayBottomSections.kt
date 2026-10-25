package com.kampplus.havali.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.CampingAdvice
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.common.textRes
import com.kampplus.havali.presentation.components.HavaliIcons
import com.kampplus.havali.presentation.components.RaisedCard
import com.kampplus.havali.presentation.components.SectionHeader
import com.kampplus.havali.presentation.components.VerdictBadge
import com.kampplus.havali.presentation.navigation.HavaliDestination
import com.kampplus.havali.presentation.theme.HavaliTheme

/** "Kamp için hava": odaktaki şehrin önerilen kamp alanları ve bugünkü kararı. Haritada çadır simgeleri de bunlardır. */
@Composable
fun CampingSection(placeName: String, advice: List<CampingAdvice>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = stringResource(R.string.camping_title, placeName))
        advice.forEach { CampingRow(it) }
    }
}

@Composable
private fun CampingRow(advice: CampingAdvice) {
    RaisedCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(38.dp).background(HavaliTheme.colors.map.wind, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(HavaliIcons.Tent, contentDescription = null, tint = HavaliTheme.colors.deep, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = advice.site.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(advice.verdict.reason.textRes, advice.verdict.value),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnBackground
                )
            }
            VerdictBadge(advice.verdict.suitability)
        }
    }
}

/**
 * Ana ekranın en altındaki yönlendirici: her sayfanın tek cümlelik özeti.
 * Kutucuğa dokununca o sayfaya geçilir; böylece dikkat haritadan sonra da dağılmaz.
 */
@Composable
fun ShortcutsSection(shortcuts: TodayShortcuts, onNavigate: (HavaliDestination) -> Unit, modifier: Modifier = Modifier) {
    val tiles = listOf(
        ShortcutTile(HavaliDestination.PLANS, HavaliIcons.Calendar, planLine(shortcuts)),
        ShortcutTile(
            HavaliDestination.ROAD_HEALTH,
            HavaliIcons.Road,
            stringResource(R.string.shortcut_road, stringResource(shortcuts.roadMode.labelRes), stringResource(shortcuts.roadRisk.labelRes))
        ),
        ShortcutTile(HavaliDestination.ROAD_HEALTH, HavaliIcons.Sun, uvLine(shortcuts.uvMax)),
        ShortcutTile(HavaliDestination.PREPARE, HavaliIcons.Bag, outfitLine(shortcuts))
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = stringResource(R.string.shortcuts_title))
        tiles.chunked(TILES_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tile -> ShortcutCard(tile, onClick = { onNavigate(tile.destination) }, modifier = Modifier.weight(1f)) }
            }
        }
    }
}

private data class ShortcutTile(val destination: HavaliDestination, val icon: ImageVector, val line: String)

@Composable
private fun ShortcutCard(tile: ShortcutTile, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 112.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(tile.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = stringResource(tile.destination.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = HavaliTheme.colors.mutedOnSurface,
                modifier = Modifier.fillMaxWidth().weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "›", style = MaterialTheme.typography.titleMedium, color = HavaliTheme.colors.mutedOnSurface)
        }
        Text(
            text = tile.line,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun planLine(shortcuts: TodayShortcuts): String = shortcuts.pendingVoteBy
    ?.let { stringResource(R.string.shortcut_vote, it) }
    ?: shortcuts.nextPlan?.let { item ->
        stringResource(R.string.shortcut_plan, item.plan.title, Format.time((item.departure?.leaveAt ?: item.plan.start.toLocalTime())))
    }
    ?: stringResource(R.string.shortcut_plan_empty)

@Composable
private fun uvLine(uv: Int): String = when {
    uv >= SUNSCREEN_UV -> stringResource(R.string.shortcut_uv_high, uv)
    else -> stringResource(R.string.shortcut_uv_low, uv)
}

@Composable
private fun outfitLine(shortcuts: TodayShortcuts): String {
    val outer = shortcuts.outfit.outer?.let { stringResource(it.labelRes) }
    val pieces = listOfNotNull(stringResource(shortcuts.outfit.base.labelRes), outer).joinToString(" + ")
    return stringResource(R.string.shortcut_outfit, pieces)
}

private const val TILES_PER_ROW = 2
private const val SUNSCREEN_UV = 3
