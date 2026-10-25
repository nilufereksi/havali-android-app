package com.kampplus.havali.presentation.today

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.havali.R
import com.kampplus.havali.presentation.common.dataOrNull
import com.kampplus.havali.presentation.components.ScrollbarColumn
import com.kampplus.havali.presentation.navigation.HavaliDestination
import com.kampplus.havali.presentation.theme.HavaliTheme
import kotlinx.coroutines.launch

/**
 * Ekranın ViewModel'e bağlandığı giriş noktası (route).
 * `collectAsStateWithLifecycle`: uygulama arka plandayken akış dinlenmez, pil harcanmaz.
 */
@Composable
fun TodayRoute(
    twoPane: Boolean,
    contentPadding: PaddingValues,
    onNavigate: (HavaliDestination) -> Unit,
    viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreen(state = state, onEvent = viewModel::onEvent, onNavigate = onNavigate, twoPane = twoPane, contentPadding = contentPadding)
}

/**
 * Responsive yerleşim:
 * - Telefon (dar): harita üstte, içerik altında tek sütun.
 * - Tablet / yatay (geniş): harita solda tam boy, içerik sağda kayar.
 * İçerik iki düzende de aynı [todayContent] fonksiyonundan gelir; tekrar yok.
 */
@Composable
fun TodayScreen(
    state: TodayUiState,
    onEvent: (TodayEvent) -> Unit,
    onNavigate: (HavaliDestination) -> Unit,
    twoPane: Boolean,
    contentPadding: PaddingValues
) {
    val bottom = contentPadding.calculateBottomPadding() + 24.dp
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val scrollDown: () -> Unit = { scope.launch { listState.animateScrollToItem(1) } }
    when (twoPane) {
        true -> Row(modifier = Modifier.fillMaxSize()) {
            MapSection(state, onEvent, onScrollHint = null, modifier = Modifier.weight(1.15f).fillMaxHeight())
            ScrollbarColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 20.dp, top = 56.dp, bottom = bottom),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                state = listState,
                spacing = 20.dp
            ) { todayContent(state, onEvent, onNavigate) }
        }
        false -> ScrollbarColumn(
            contentPadding = PaddingValues(bottom = bottom),
            modifier = Modifier.fillMaxSize(),
            state = listState,
            spacing = 20.dp
        ) {
            // İlk açılışta ekranın tamamı haritadır (alt menü hariç); aşağı ok ve sağdaki çubuk ayrıntılara indirir.
            item(key = "map") {
                val height = LocalConfiguration.current.screenHeightDp.dp - contentPadding.calculateBottomPadding()
                MapSection(state, onEvent, onScrollHint = scrollDown, modifier = Modifier.fillMaxWidth().height(height))
            }
            todayContent(state, onEvent, onNavigate, horizontalPadding = Modifier.padding(start = 16.dp, end = 20.dp))
        }
    }
}

private fun LazyListScope.todayContent(
    state: TodayUiState,
    onEvent: (TodayEvent) -> Unit,
    onNavigate: (HavaliDestination) -> Unit,
    horizontalPadding: Modifier = Modifier
) {
    state.focus.dataOrNull()?.let { focus ->
        item(key = "activities") { ActivitiesSection(focus.place.name, focus.activities, modifier = horizontalPadding) }
        item(key = "summary") {
            SummarySection(
                placeName = focus.place.name,
                facts = focus.summary,
                departure = state.nextDeparture.takeIf { focus.isHome },
                feedback = state.feedback,
                onFeedback = { onEvent(TodayEvent.GiveFeedback(it)) },
                modifier = horizontalPadding
            )
        }
        focus.camping.takeIf { it.isNotEmpty() }?.let { camping ->
            item(key = "camping") { CampingSection(focus.place.name, camping, modifier = horizontalPadding) }
        }
    }
    state.shortcuts?.let { shortcuts ->
        item(key = "shortcuts") { ShortcutsSection(shortcuts, onNavigate, modifier = horizontalPadding) }
    }
    item(key = "attribution") {
        Text(
            text = stringResource(R.string.data_attribution),
            style = MaterialTheme.typography.labelSmall,
            color = HavaliTheme.colors.mutedOnBackground,
            modifier = horizontalPadding
        )
    }
}
