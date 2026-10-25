package com.kampplus.havali.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.icon
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.theme.HavaliTheme

/** Bir şehrin özet satırı için gereken veriler. Havası bilinmiyorsa [kind] ve [temperatureC] null olur. */
data class CityCardModel(val place: Place, val kind: WeatherKind?, val temperatureC: Double?)

/**
 * Arama sonuçlarındaki şehir kartı.
 * Seçili kart sarı zemine geçer (vurgulama); tıklama yukarıya bildirilir.
 */
@Composable
fun CityCard(model: CityCardModel, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (target, content) = when (selected) {
        true -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        false -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }
    val container by animateColorAsState(target, label = "city")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(HavaliTheme.colors.infoTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = model.kind?.icon ?: HavaliIcons.Map,
                contentDescription = model.kind?.let { stringResource(it.labelRes) },
                tint = HavaliTheme.colors.onInfoTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.place.name,
                style = MaterialTheme.typography.titleSmall,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = model.place.region,
                style = MaterialTheme.typography.bodySmall,
                color = content.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        model.temperatureC?.let { Text(text = Format.degrees(it), style = MaterialTheme.typography.titleMedium, color = content) }
    }
}

@Preview
@Composable
private fun CityCardPreview() = HavaliTheme {
    val place = Place("izmir", "İzmir", "Ege", GeoPoint(38.4, 27.1), MapZoom.REGION)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
        CityCard(CityCardModel(place, WeatherKind.RAIN, 14.0), selected = false, onClick = {})
        CityCard(CityCardModel(place.copy(name = "Alaçatı", region = "İzmir"), WeatherKind.CLEAR, 23.0), selected = true, onClick = {})
    }
}
