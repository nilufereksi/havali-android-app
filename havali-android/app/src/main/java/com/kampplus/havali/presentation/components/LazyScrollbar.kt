package com.kampplus.havali.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Ekranın sağındaki kaydırma çubuğu: fareyle tutup aşağı-yukarı sürüklenir ya da
 * bir noktasına tıklanır. Konum, görünen öğelerin ortalama boyundan tahmin edilir.
 * Liste kaydırılamıyorsa görünmez.
 */
@Composable
fun LazyScrollbar(state: LazyListState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val metrics by remember(state) { derivedStateOf { ScrollMetrics.of(state) } }
    val jump: (Float) -> Unit = { fraction -> scope.launch { state.scrollToItem(metrics.indexAt(fraction)) } }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(TOUCH_WIDTH)
            .alpha(metrics.visibility)
            .pointerInput(state) { detectTapGestures { jump(it.y / size.height) } }
            .pointerInput(state) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    jump(change.position.y / size.height)
                }
            }
    ) {
        val thumbHeight = maxHeight * metrics.thumbFraction
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .width(3.dp)
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f), RoundedCornerShape(percent = 50))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (maxHeight - thumbHeight) * metrics.progress)
                .width(6.dp)
                .height(thumbHeight)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(percent = 50))
        )
    }
}

private data class ScrollMetrics(val progress: Float, val thumbFraction: Float, val visibility: Float, val total: Int) {

    fun indexAt(fraction: Float): Int = (fraction.coerceIn(0f, 1f) * (total - 1)).toInt().coerceAtLeast(0)

    companion object {
        fun of(state: LazyListState): ScrollMetrics {
            val layout = state.layoutInfo
            val visible = layout.visibleItemsInfo
            val average = visible.sumOf { it.size }.toFloat() / visible.size.coerceAtLeast(1)
            val content = (average * layout.totalItemsCount).coerceAtLeast(1f)
            val viewport = (layout.viewportEndOffset - layout.viewportStartOffset).toFloat()
            val scrolled = state.firstVisibleItemIndex * average + state.firstVisibleItemScrollOffset
            val scrollable = state.canScrollForward || state.canScrollBackward
            return ScrollMetrics(
                progress = (scrolled / (content - viewport).coerceAtLeast(1f)).coerceIn(0f, 1f),
                thumbFraction = (viewport / content).coerceIn(MIN_THUMB, 1f),
                visibility = when (scrollable) {
                    true -> 1f
                    false -> 0f
                },
                total = layout.totalItemsCount
            )
        }
    }
}

private val TOUCH_WIDTH = 16.dp
private const val MIN_THUMB = 0.08f

/** Kaydırma çubuklu dikey liste: bütün ekranlar aynı bileşeni kullanır. */
@Composable
fun ScrollbarColumn(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    spacing: Dp = 16.dp,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxHeight(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content
        )
        LazyScrollbar(
            state = state,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding())
        )
    }
}
