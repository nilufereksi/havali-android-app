package com.kampplus.havali.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/*
 * Tema: marka görünümü laciverttir (darkTheme = true). Açık şema da tanımlıdır;
 * önizlemelerde `HavaliTheme(darkTheme = false)` ile denenebilir. Bileşenler renkleri
 * hep MaterialTheme.colorScheme / HavaliTheme.colors üzerinden okur, böylece tema
 * değişince her şey kendiliğinden uyum sağlar.
 */

private val BrandScheme: ColorScheme = darkColorScheme(
    primary = Yellow,
    onPrimary = NavyDeep,
    secondary = BlueTint,
    onSecondary = Navy,
    background = Navy,
    onBackground = White,
    surface = White,
    onSurface = Ink,
    surfaceVariant = NavyRaised,
    onSurfaceVariant = White,
    error = Danger,
    onError = White,
    outline = NavyLine,
    surfaceContainer = White,
    surfaceContainerHigh = White,
    surfaceContainerLow = White,
    surfaceContainerHighest = White,
    surfaceContainerLowest = White
)

/** Açık tema koyunun tam tersidir: zemin sarı, yazı ve vurgu saks laciverti. */
private val LightScheme: ColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Yellow,
    secondary = Navy,
    onSecondary = White,
    background = Yellow,
    onBackground = Navy,
    surface = White,
    onSurface = Ink,
    surfaceVariant = YellowRaised,
    onSurfaceVariant = Navy,
    error = Danger,
    onError = White,
    outline = Navy,
    surfaceContainer = White,
    surfaceContainerHigh = White,
    surfaceContainerLow = White,
    surfaceContainerHighest = White,
    surfaceContainerLowest = White
)

private val BrandColors = HavaliColors(
    raised = NavyRaised,
    deep = NavyDeep,
    line = NavyLine,
    mutedOnBackground = MutedOnNavy,
    mutedOnSurface = MutedOnWhite,
    navInactive = NavInactive,
    accentSoft = YellowSoft,
    onAccentSoft = YellowInk,
    infoTint = BlueTint,
    onInfoTint = Navy,
    dangerTint = DangerTint,
    onDangerTint = DangerInk,
    map = MapColors()
)

private val LightColors = BrandColors.copy(
    raised = YellowRaised,
    deep = YellowDeep,
    line = Navy.copy(alpha = 0.25f),
    mutedOnBackground = NavyMuted,
    navInactive = NavyMuted
)

private val HavaliShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

private val LocalHavaliColors = staticCompositionLocalOf { BrandColors }

@Composable
fun HavaliTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val (scheme, colors) = when (darkTheme) {
        true -> BrandScheme to BrandColors
        false -> LightScheme to LightColors
    }
    CompositionLocalProvider(LocalHavaliColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = HavaliTypography, shapes = HavaliShapes, content = content)
    }
}

/** `MaterialTheme` gibi: `HavaliTheme.colors.raised`. */
object HavaliTheme {
    val colors: HavaliColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHavaliColors.current
}
