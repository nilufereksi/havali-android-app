package com.kampplus.havali.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.presentation.common.UiError
import com.kampplus.havali.presentation.theme.HavaliTheme

/*
 * LoadState'in Loading / Empty / Error hâllerinin ortak görünümleri.
 * Her ekran aynı bileşenleri kullandığı için hata deneyimi her yerde tutarlıdır.
 */

@Composable
fun LoadingView(modifier: Modifier = Modifier, label: String = stringResource(R.string.loading)) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = HavaliTheme.colors.mutedOnBackground)
    }
}

@Composable
fun ErrorView(
    error: UiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = HavaliIcons.Alert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = stringResource(error.messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = stringResource(R.string.retry), modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
fun EmptyView(title: String, body: String, modifier: Modifier = Modifier, contentColor: Color = MaterialTheme.colorScheme.onBackground) {
    Column(
        modifier = modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = contentColor, textAlign = TextAlign.Center)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Hata · uçak modu", showBackground = true, backgroundColor = 0xFF1F3B6E)
@Composable
private fun ErrorViewPreview() = HavaliTheme { ErrorView(error = UiError.NO_CONNECTION, onRetry = {}) }

@Preview(name = "Boş sonuç", showBackground = true, backgroundColor = 0xFF1F3B6E)
@Composable
private fun EmptyViewPreview() = HavaliTheme {
    EmptyView(title = "Sonuç bulunamadı", body = "Yazımı kontrol et ya da başka bir şehir dene.")
}
