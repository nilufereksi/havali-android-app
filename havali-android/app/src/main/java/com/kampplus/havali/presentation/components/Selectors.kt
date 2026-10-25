package com.kampplus.havali.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.presentation.common.icon
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.theme.HavaliTheme

/**
 * İki-üç seçenekli sekme ("Yol durumu | Sağlık ve bakım").
 * Seçili olan sarı zemin alır; renk geçişi animasyonludur.
 * Durum dışarıdan gelir (state hoisting): bileşen yalnızca çizer ve tıklamayı bildirir.
 */
@Composable
fun SegmentedTabs(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HavaliTheme.colors.deep, RoundedCornerShape(percent = 50))
            .padding(4.dp)
            .selectableGroup()
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val colors = selectionColors(selected, idleContainer = Color.Transparent)
            val container by animateColorAsState(colors.container, label = "tab")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(container)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.content,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Simgeli seçim çipi (ulaşım şekli, rapor türü...). */
@Composable
fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val colors = selectionColors(selected, idleContainer = HavaliTheme.colors.raised)
    val container by animateColorAsState(colors.container, label = "chip")
    val content = colors.content
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(container)
            .border(1.dp, colors.border, RoundedCornerShape(percent = 50))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon?.let { Icon(imageVector = it, contentDescription = null, tint = content, modifier = Modifier.size(18.dp)) }
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
    }
}

/** Seçili / seçili değil renkleri tek yerde: sarı zemin + lacivert yazı ya da sakin zemin + beyaz yazı. */
private data class SelectionColors(val container: Color, val content: Color, val border: Color)

@Composable
private fun selectionColors(selected: Boolean, idleContainer: Color): SelectionColors = when (selected) {
    true -> SelectionColors(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, Color.Transparent)
    false -> SelectionColors(idleContainer, MaterialTheme.colorScheme.onBackground, HavaliTheme.colors.line)
}

/** Ulaşım şekli seçici; dar ekranda yatay kaydırılır. */
@Composable
fun ModeSelector(modes: List<TravelMode>, selected: TravelMode, onSelect: (TravelMode) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { mode ->
            ChoiceChip(label = stringResource(mode.labelRes), selected = mode == selected, onClick = { onSelect(mode) }, icon = mode.icon)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1F3B6E)
@Composable
private fun SelectorsPreview() = HavaliTheme {
    Row { SegmentedTabs(options = listOf("Yol durumu", "Sağlık ve bakım"), selectedIndex = 0, onSelect = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF1F3B6E)
@Composable
private fun ModeSelectorPreview() = HavaliTheme {
    ModeSelector(modes = TravelMode.commuteModes, selected = TravelMode.MOTORBIKE, onSelect = {})
}
