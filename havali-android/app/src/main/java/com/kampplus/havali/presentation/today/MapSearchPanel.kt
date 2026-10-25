package com.kampplus.havali.presentation.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.components.CityCard
import com.kampplus.havali.presentation.components.CityCardModel
import com.kampplus.havali.presentation.components.EmptyView
import com.kampplus.havali.presentation.components.ErrorView
import com.kampplus.havali.presentation.components.LoadingView
import com.kampplus.havali.presentation.theme.HavaliTheme

/**
 * Şehir arama paneli: haritanın üstünde açılır, dikkati haritadan almaz.
 *
 * - Yazılan metin `rememberSaveable` ile tutulur: ekran dönse de kaybolmaz.
 * - Her harfte bilinen yerler anında süzülür (LazyColumn + CityCard).
 * - Sonuç yoksa boş durum gösterilir; "İnternette ara" Open-Meteo'ya sorar ve
 *   yükleniyor / hata (Tekrar dene) / boş / sonuç hâllerinin hepsini çizer.
 * - Bir sonuca dokununca harita o yere yaklaşır ve panel kapanır.
 */
@Composable
fun MapSearchPanel(
    search: SearchUiState,
    selectedId: String?,
    onEvent: (TodayEvent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf(search.query) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        onEvent(TodayEvent.QueryChanged(query))
    }
    val choose: (Place) -> Unit = { place ->
        keyboard?.hide()
        onEvent(TodayEvent.SelectPlace(place))
        onClose()
    }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onEvent(TodayEvent.QueryChanged(it))
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.search_close)) }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onEvent(TodayEvent.SearchOnline) }),
                shape = MaterialTheme.shapes.small
            )
            SearchResults(search, selectedId, choose, onSearchOnline = { onEvent(TodayEvent.SearchOnline) })
        }
    }
}

@Composable
private fun SearchResults(search: SearchUiState, selectedId: String?, onChoose: (Place) -> Unit, onSearchOnline: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    when (val remote = search.remote) {
        null -> CityList(search.local, selectedId, onChoose, empty = {
            EmptyView(
                title = stringResource(R.string.search_empty_title),
                body = stringResource(R.string.search_empty_body),
                contentColor = onSurface
            )
        })
        LoadState.Loading -> LoadingView(label = stringResource(R.string.search_loading))
        LoadState.Empty -> EmptyView(
            title = stringResource(R.string.search_empty_title),
            body = stringResource(R.string.search_empty_body),
            contentColor = onSurface
        )
        is LoadState.Error -> ErrorView(error = remote.error, onRetry = onSearchOnline, contentColor = onSurface)
        is LoadState.Success -> CityList(remote.data, selectedId, onChoose, empty = {})
    }
    TextButton(onClick = onSearchOnline, enabled = search.query.isNotBlank() && search.remote == null) {
        Text(stringResource(R.string.search_online, search.query))
    }
}

@Composable
private fun CityList(cities: List<CityCardModel>, selectedId: String?, onChoose: (Place) -> Unit, empty: @Composable () -> Unit) {
    when (cities.isEmpty()) {
        true -> empty()
        false -> LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(cities, key = { it.place.id }) { city ->
                CityCard(model = city, selected = city.place.id == selectedId, onClick = { onChoose(city.place) })
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1B2B)
@Composable
private fun MapSearchPanelPreview() = HavaliTheme {
    val izmir = Place("izmir", "İzmir", "Ege", GeoPoint(38.4, 27.1), MapZoom.REGION)
    val alacati = Place("alacati", "Alaçatı", "İzmir", GeoPoint(38.28, 26.37), MapZoom.DISTRICT)
    MapSearchPanel(
        search = SearchUiState(
            query = "iz",
            local = listOf(CityCardModel(izmir, WeatherKind.RAIN, 14.0), CityCardModel(alacati, WeatherKind.CLEAR, 23.0))
        ),
        selectedId = "izmir",
        onEvent = {},
        onClose = {}
    )
}
