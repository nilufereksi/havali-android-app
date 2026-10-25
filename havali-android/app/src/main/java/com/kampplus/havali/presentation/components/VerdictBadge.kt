package com.kampplus.havali.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.presentation.common.labelRes

/** "Uygun / Dikkat / Uygun değil" rozeti. Renk her yerde aynı anlamı taşır. */
@Composable
fun VerdictBadge(suitability: Suitability, modifier: Modifier = Modifier, text: String = stringResource(suitability.labelRes)) {
    val colors = suitability.colors()
    Pill(text = text, container = colors.container, content = colors.content, modifier = modifier)
}
