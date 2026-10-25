package com.kampplus.havali.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kampplus.havali.R
import com.kampplus.havali.presentation.components.HavaliIcons
import com.kampplus.havali.presentation.plans.PlansRoute
import com.kampplus.havali.presentation.prepare.PrepareRoute
import com.kampplus.havali.presentation.road.RoadHealthRoute
import com.kampplus.havali.presentation.theme.HavaliTheme
import com.kampplus.havali.presentation.today.TodayRoute

/**
 * Uygulama iskeleti ve gezinme.
 *
 * Responsive: dar ekranda (telefon dikey) alt menü, daha geniş ekranda (yatay telefon,
 * katlanabilir, tablet) yan menü (NavigationRail) kullanılır ve ekranlar iki sütuna geçer.
 */
@Composable
fun HavaliApp(windowSizeClass: WindowSizeClass, darkTheme: Boolean, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = HavaliDestination.fromRoute(backStack?.destination?.route)
    val navigate: (HavaliDestination) -> Unit = { navController.navigateSingleTop(it) }
    Box(modifier = Modifier.fillMaxSize()) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = { BottomMenu(current, navigate) }
            ) { padding -> HavaliNavHost(navController, navigate, twoPane = false, contentPadding = padding) }
            else -> Row(modifier = Modifier.fillMaxSize()) {
                SideMenu(current, navigate)
                Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
                    HavaliNavHost(navController, navigate, twoPane = true, contentPadding = padding)
                }
            }
        }
        ThemeToggle(
            darkTheme = darkTheme,
            onToggle = onToggleTheme,
            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(top = 6.dp, end = 12.dp)
        )
    }
}

/** Her ekranın sağ üstündeki yuvarlak tema düğmesi: koyuda güneş (açığa geç), açıkta ay (koyuya geç). */
@Composable
private fun ThemeToggle(darkTheme: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val (icon, label) = when (darkTheme) {
        true -> HavaliIcons.Sun to R.string.theme_light
        false -> HavaliIcons.Moon to R.string.theme_dark
    }
    FilledIconButton(
        onClick = onToggle,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = HavaliTheme.colors.map.labelBackground),
        modifier = modifier.size(40.dp)
    ) {
        Icon(icon, contentDescription = stringResource(label), tint = THEME_ICON_TINT)
    }
}

@Composable
private fun HavaliNavHost(
    navController: NavHostController,
    navigate: (HavaliDestination) -> Unit,
    twoPane: Boolean,
    contentPadding: PaddingValues
) {
    NavHost(navController = navController, startDestination = HavaliDestination.TODAY.route) {
        composable(HavaliDestination.TODAY.route) { TodayRoute(twoPane, contentPadding, onNavigate = navigate) }
        composable(HavaliDestination.PLANS.route) { PlansRoute(twoPane, contentPadding) }
        composable(HavaliDestination.ROAD_HEALTH.route) { RoadHealthRoute(twoPane, contentPadding) }
        composable(HavaliDestination.PREPARE.route) { PrepareRoute(twoPane, contentPadding) }
    }
}

/** Sekmeler arasında geçerken her sekmenin kaydırma konumu korunur, geri tuşu yığını şişmez. */
private fun NavHostController.navigateSingleTop(destination: HavaliDestination) = navigate(destination.route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
private fun BottomMenu(current: HavaliDestination, onSelect: (HavaliDestination) -> Unit) {
    NavigationBar(containerColor = HavaliTheme.colors.deep) {
        HavaliDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == current,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.labelRes), maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = HavaliTheme.colors.navInactive,
                    unselectedTextColor = HavaliTheme.colors.navInactive
                )
            )
        }
    }
}

@Composable
private fun SideMenu(current: HavaliDestination, onSelect: (HavaliDestination) -> Unit) {
    NavigationRail(containerColor = HavaliTheme.colors.deep) {
        HavaliDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = destination == current,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.labelRes), maxLines = 1) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = HavaliTheme.colors.navInactive,
                    unselectedTextColor = HavaliTheme.colors.navInactive
                )
            )
        }
    }
}

private val THEME_ICON_TINT = Color(0xFFFFC20E)
