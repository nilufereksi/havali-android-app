package com.kampplus.havali.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.kampplus.havali.R
import com.kampplus.havali.presentation.components.HavaliIcons

/** Uygulamanın dört ana ekranı. Rota adı, menü metni ve ikonu birlikte tanımlıdır. */
enum class HavaliDestination(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    TODAY("today", R.string.nav_today, HavaliIcons.Map),
    PLANS("plans", R.string.nav_plans, HavaliIcons.Calendar),
    ROAD_HEALTH("road_health", R.string.nav_road_health, HavaliIcons.Road),
    PREPARE("prepare", R.string.nav_prepare, HavaliIcons.Bag);

    companion object {
        fun fromRoute(route: String?): HavaliDestination = entries.firstOrNull { it.route == route } ?: TODAY
    }
}
