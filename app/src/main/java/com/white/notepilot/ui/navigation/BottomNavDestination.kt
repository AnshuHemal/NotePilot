package com.white.notepilot.ui.navigation

import androidx.compose.runtime.Immutable
import com.white.notepilot.R

@Immutable
sealed class BottomNavDestination(
    val route: String,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val label: String
) {
    object Home : BottomNavDestination(
        route = Routes.Home.route,
        selectedIcon = R.drawable.home,
        unselectedIcon = R.drawable.home_outlined,
        label = "Home"
    )

    object Notifications : BottomNavDestination(
        route = "notifications",
        selectedIcon = R.drawable.notification,
        unselectedIcon = R.drawable.notification_outlined,
        label = "Notifications"
    )

    object Account : BottomNavDestination(
        route = "account",
        selectedIcon = R.drawable.account,
        unselectedIcon = R.drawable.account_outlined,
        label = "Account"
    )

    object Settings : BottomNavDestination(
        route = "settings",
        selectedIcon = R.drawable.settings,
        unselectedIcon = R.drawable.settings_outlined,
        label = "Settings"
    )
}
