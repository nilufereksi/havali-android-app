package com.kampplus.havali.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.presentation.theme.HavaliTheme

/** Beyaz içerik kartı: lacivert zemin üstünde okunur bilgi blokları. */
@Composable
fun HavaliCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = contentColorOn(container)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** Lacivertin bir ton açığı kart: ikincil bilgi için. */
@Composable
fun RaisedCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    HavaliCard(modifier = modifier, container = HavaliTheme.colors.raised, content = content)

@Composable
private fun contentColorOn(container: Color): Color = when (container) {
    MaterialTheme.colorScheme.surface -> MaterialTheme.colorScheme.onSurface
    MaterialTheme.colorScheme.primary -> MaterialTheme.colorScheme.onPrimary
    HavaliTheme.colors.accentSoft -> HavaliTheme.colors.onAccentSoft
    HavaliTheme.colors.infoTint -> HavaliTheme.colors.onInfoTint
    else -> MaterialTheme.colorScheme.onBackground
}

/** Bölüm başlığı. Ekran okuyucu bunu "başlık" olarak duyurur. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        trailing()
    }
}

/** Küçük yuvarlak etiket ("Uygun", "12° · %70"). */
@Composable
fun Pill(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = modifier
            .background(container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

/** Uygunluk renkleri: uygun = sarı, dikkat = açık mavi, uygun değil = açık kırmızı. */
data class VerdictColors(val container: Color, val content: Color)

@Composable
fun Suitability.colors(): VerdictColors = when (this) {
    Suitability.GOOD -> VerdictColors(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
    Suitability.CAUTION -> VerdictColors(HavaliTheme.colors.infoTint, HavaliTheme.colors.onInfoTint)
    Suitability.BAD -> VerdictColors(HavaliTheme.colors.dangerTint, HavaliTheme.colors.onDangerTint)
}
