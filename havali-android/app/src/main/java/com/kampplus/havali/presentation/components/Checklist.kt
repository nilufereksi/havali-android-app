package com.kampplus.havali.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.ChecklistGroup
import com.kampplus.havali.domain.model.ChecklistItem
import com.kampplus.havali.domain.model.ChecklistSection
import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.common.noteRes
import com.kampplus.havali.presentation.common.titleRes
import com.kampplus.havali.presentation.common.valueHintRes
import com.kampplus.havali.presentation.theme.HavaliTheme

/**
 * İşaretlenebilir liste kartı (çıkış listesi ve valiz ortak kullanır).
 * Bütün satır dokunulabilir (toggleable), yalnızca küçük kutucuk değil: parmakla kullanım kolaylaşır.
 */
@Composable
fun ChecklistCard(
    title: String,
    sections: List<ChecklistSection>,
    checked: Set<ItemKey>,
    done: Int,
    total: Int,
    onToggle: (ItemKey) -> Unit,
    modifier: Modifier = Modifier
) {
    HavaliCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Pill(
                text = stringResource(R.string.checklist_progress, done, total),
                container = HavaliTheme.colors.infoTint,
                content = HavaliTheme.colors.onInfoTint
            )
        }
        sections.forEach { section ->
            Text(
                text = stringResource(section.group.titleRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = HavaliTheme.colors.mutedOnSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
            section.items.forEach { item -> ChecklistRow(item, item.key in checked, onToggle) }
        }
    }
}

@Composable
private fun ChecklistRow(item: ChecklistItem, checked: Boolean, onToggle: (ItemKey) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle(item.key) }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = HavaliTheme.colors.deep, checkmarkColor = MaterialTheme.colorScheme.primary)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.key.labelRes),
                style = MaterialTheme.typography.titleSmall,
                textDecoration = TextDecoration.LineThrough.takeIf { checked }
            )
            Text(text = itemHint(item), style = MaterialTheme.typography.bodySmall, color = HavaliTheme.colors.mutedOnSurface)
        }
    }
}

/** Sayısı olan maddede ipucu sayıyla kurulur ("yağış %70"), olmayanda sabit açıklama gösterilir. */
@Composable
private fun itemHint(item: ChecklistItem): String =
    item.value?.let { stringResource(item.key.valueHintRes, it) } ?: stringResource(item.key.noteRes)

@Preview(widthDp = 390)
@Composable
private fun ChecklistPreview() = HavaliTheme {
    ChecklistCard(
        title = "Çıkmadan önce",
        sections = listOf(
            ChecklistSection(ChecklistGroup.MODE, listOf(ChecklistItem(ItemKey.PROTECTIVE_GEAR, 9), ChecklistItem(ItemKey.VEHICLE_LOCK))),
            ChecklistSection(ChecklistGroup.WEATHER, listOf(ChecklistItem(ItemKey.UMBRELLA, 70)))
        ),
        checked = setOf(ItemKey.VEHICLE_LOCK),
        done = 1,
        total = 3,
        onToggle = {}
    )
}
