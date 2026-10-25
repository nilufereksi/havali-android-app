package com.kampplus.havali.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Tasarımdaki çizgi ikonlar (24x24, 1.8 kalınlık), SVG yol verisinden üretilir.
 *
 * material-icons-core yalnızca temel ikonları içerir; hava, ulaşım ve spor ikonları
 * burada tanımlıdır. Her ikon `by lazy` ile ilk kullanımda bir kez oluşturulur.
 * Renk Icon(tint = ...) ile verilir, buradaki siyah yalnızca maskedir.
 */
object HavaliIcons {
    val Map by icon("M9 4L3 6.5v13.5l6-2.5 6 2.5 6-2.5V4l-6 2.5z M9 4v13.5 M15 6.5V20")
    val Calendar by icon("M6 5h12a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z M4 10h16 M9 3v4 M15 3v4")
    val Road by icon("M8 3L4 21 M16 3l4 18 M12 5v2 M12 11v2 M12 17v2")
    val Bag by icon(
        "M7.5 7h9A2.5 2.5 0 0 1 19 9.5v8a2.5 2.5 0 0 1-2.5 2.5h-9A2.5 2.5 0 0 1 5 17.5v-8A2.5 2.5 0 0 1 7.5 7z M9 7V4.5A1.5 1.5 0 0 1 10.5 3h3A1.5 1.5 0 0 1 15 4.5V7"
    )

    val Sun by icon(
        "M12 8a4 4 0 1 1 0 8a4 4 0 1 1 0-8z M12 2v2 M12 20v2 M4.9 4.9l1.4 1.4 M17.7 17.7l1.4 1.4 M2 12h2 M20 12h2 M4.9 19.1l1.4-1.4 M17.7 6.3l1.4-1.4"
    )
    val Cloud by icon("M7 18h10a4 4 0 0 0 .6-7.95A6 6 0 0 0 6.2 9.1A4.5 4.5 0 0 0 7 18z")
    val Rain by icon("M7 14h10a4 4 0 0 0 .6-7.95A6 6 0 0 0 6.2 5.1A4.5 4.5 0 0 0 7 14z M8 17l-1 3 M12 17l-1 3 M16 17l-1 3")
    val Snow by icon("M12 3v18 M4.2 7.5l15.6 9 M4.2 16.5l15.6-9 M9.5 4.5L12 6l2.5-1.5 M9.5 19.5L12 18l2.5 1.5")
    val Storm by icon("M7 14h10a4 4 0 0 0 .6-7.95A6 6 0 0 0 6.2 5.1A4.5 4.5 0 0 0 7 14z M13 14l-3 4h4l-3 4")
    val Fog by icon("M4 8h16 M6 12h12 M4 16h16 M8 20h8")
    val Wind by icon("M3 8h11a3 3 0 1 0-3-3 M3 12h16a3 3 0 1 1-3 3 M3 16h8")
    val Drop by icon("M12 3l5.5 8a6.5 6.5 0 1 1-11 0z")
    val Thermometer by icon("M10 13.5V5a2 2 0 0 1 4 0v8.5a4 4 0 1 1-4 0z M12 9v6")
    val Umbrella by icon("M12 3a9 9 0 0 1 9 9H3a9 9 0 0 1 9-9z M12 12v7a2 2 0 0 1-4 0")
    val Alert by icon("M12 4l9 16H3z M12 10v4 M12 17v.5")
    val Pause by icon("M8 5v14 M16 5v14")
    val Minus by icon("M5 12h14")
    val Moon by icon("M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z")
    val ChevronDown by icon("M6 9l6 6 6-6")
    val Undo by icon("M9 14L4 9l5-5 M4 9h10.5a5.5 5.5 0 0 1 0 11H11")
    val Tent by icon("M12 4L3 20h18z M12 4l-1.5 -2 M12 4l1.5 -2 M12 12l-3.5 8 M12 12l3.5 8")
    val Exclamation by icon("M12 4v10 M12 18.5v.5")
    val Shield by icon("M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z")
    val Leaf by icon("M5 19c0-8 5-14 15-14 0 10-6 15-14 15 M5 19l7-7")

    val Walk by icon("M13 3.5a1.5 1.5 0 1 1 0 3a1.5 1.5 0 1 1 0-3z M10 21l2-6 2.5 2.5V21 M8 12l2.5-4 3 1.5 2.5 3 M10.5 8L9 14")
    val Bike by icon(
        "M2 17.5a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0-7 0z M15 17.5a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0-7 0z M5.5 17.5L9 10h6l3.5 7.5 M9 10l3 7.5L15 10 M8 6h3"
    )
    val Motorbike by icon("M2 16a3 3 0 1 0 6 0a3 3 0 1 0-6 0z M16 16a3 3 0 1 0 6 0a3 3 0 1 0-6 0z M5 16l4-6h5l5 6 M14 10l-2-4h3 M8 10h4")
    val Car by icon("M5 16v-5l2-5h10l2 5v5 M3 16h18v3h-2.5 M5.5 19H3 M7.5 13.5h.01 M16.5 13.5h.01 M5 11h14")
    val Transit by icon("M6 3h12a2 2 0 0 1 2 2v11H4V5a2 2 0 0 1 2-2z M4 11h16 M8 16v3 M16 16v3 M8 13.5h.01 M16 13.5h.01")
    val Plane by icon("M10.5 3.5c0-1 .7-1.5 1.5-1.5s1.5.5 1.5 1.5V9l7 4v2l-7-2v4.5l2 1.5v1.5L12 20l-3.5 1v-1.5l2-1.5V13l-7 2v-2l7-4z")
    val Bus by icon("M5 4h14a1 1 0 0 1 1 1v12H4V5a1 1 0 0 1 1-1z M4 9h16 M4 13h16 M7 17v3 M17 17v3")
    val Train by icon(
        "M7 3h10a3 3 0 0 1 3 3v8a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3V6a3 3 0 0 1 3-3z M4 10h16 M8 17l-2 4 M16 17l2 4 M8.5 13.5h.01 M15.5 13.5h.01"
    )

    val Swim by icon(
        "M2 18c2 0 2-1.5 4-1.5s2 1.5 4 1.5 2-1.5 4-1.5 2 1.5 4 1.5 2-1.5 4-1.5 M7 13l4-3-2-3 4-2 3 4 M17 7a1.5 1.5 0 1 1 0 3a1.5 1.5 0 1 1 0-3z"
    )
    val Wave by icon("M2 12c2 0 2-2 4-2s2 2 4 2 2-2 4-2 2 2 4 2 2-2 4-2 M2 17c2 0 2-2 4-2s2 2 4 2 2-2 4-2 2 2 4 2 2-2 4-2")
    val Sail by icon("M12 3v14 M12 3l7 13h-7 M12 6L6 16h6 M3 20h18")
    val Ski by icon("M4 19l16-6 M13 3a1.5 1.5 0 1 1 0 3a1.5 1.5 0 1 1 0-3z M8 13l3-4 3 2 2 3 M11 9l-3 1")
    val Mountain by icon("M3 20l6-11 4 6 2-3 6 8z")
    val Run by icon("M14 3.5a1.5 1.5 0 1 1 0 3a1.5 1.5 0 1 1 0-3z M6 20l3-5 3 2 1-5 3 3h3 M9 9l3-2 2 2-1 3 M5 12l3-3")

    private fun icon(pathData: String) = lazy {
        ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(
                pathData = addPathNodes(pathData),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = STROKE_WIDTH,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
            .build()
    }

    private const val STROKE_WIDTH = 1.8f
}
