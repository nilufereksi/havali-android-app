package com.kampplus.havali.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/*
 * Havalı paleti: saks laciverti zemin + Turkcell sarısına yakın orta sıcaklıkta sarı.
 * Renkler yalnızca burada tanımlıdır; ekranlar renk kodu değil anlamlı ad kullanır.
 */

internal val Navy = Color(0xFF1F3B6E)
internal val NavyRaised = Color(0xFF284A84)
internal val NavyDeep = Color(0xFF152B52)
internal val NavyLine = Color(0xFF2A4677)
internal val MapSea = Color(0xFF0F1B2B)
internal val MapLand = Color(0xFF1B2D47)
internal val MapLandDetail = Color(0xFF213756)
internal val MapBorder = Color(0xFF3A5580)
internal val MapNeighbor = Color(0xFF152438)
internal val Yellow = Color(0xFFFFC20E)
internal val YellowSoft = Color(0xFFFFF3C4)
internal val YellowRaised = Color(0xFFFFD84D)
internal val YellowDeep = Color(0xFFE9AE00)
internal val NavyMuted = Color(0xFF3A4E7A)
internal val YellowInk = Color(0xFF4A3700)
internal val Ink = Color(0xFF0E1A33)
internal val White = Color(0xFFFFFFFF)
internal val BlueTint = Color(0xFFE3EAF6)
internal val DangerTint = Color(0xFFFDE4DF)
internal val DangerInk = Color(0xFF7A1F0E)
internal val Danger = Color(0xFFE5533D)
internal val MutedOnNavy = Color(0xFFB9C7E0)
internal val MutedOnWhite = Color(0xFF556274)
internal val NavInactive = Color(0xFF9FB0CF)
internal val RainBlue = Color(0xFF5AA9FF)
internal val SnowWhite = Color(0xFFEAF3FF)
internal val SunGlow = Color(0xFFFFD35A)
internal val Teal = Color(0xFF4FC7B8)

/**
 * Material renk şemasında karşılığı olmayan marka renkleri.
 * `HavaliTheme.colors.raised` gibi okunur.
 */
@Immutable
data class HavaliColors(
    val raised: Color,
    val deep: Color,
    val line: Color,
    val mutedOnBackground: Color,
    val mutedOnSurface: Color,
    val navInactive: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val infoTint: Color,
    val onInfoTint: Color,
    val dangerTint: Color,
    val onDangerTint: Color,
    val map: MapColors
)

@Immutable
data class MapColors(
    val sea: Color = MapSea,
    val land: Color = MapLand,
    val landDetail: Color = MapLandDetail,
    val border: Color = MapBorder,
    val neighbor: Color = MapNeighbor,
    val rain: Color = RainBlue,
    val snow: Color = SnowWhite,
    val sun: Color = SunGlow,
    val wind: Color = Teal,
    val label: Color = White,
    val labelBackground: Color = NavyDeep
)
