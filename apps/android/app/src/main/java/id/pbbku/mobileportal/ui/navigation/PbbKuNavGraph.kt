package id.pbbku.mobileportal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.pbbku.mobileportal.feature.home.HomeScreen
import id.pbbku.mobileportal.feature.notifications.NotificationsScreen
import id.pbbku.mobileportal.feature.search.SearchScreen
import id.pbbku.mobileportal.feature.settings.SettingsScreen

private data class TopLevelRoute(
    val route: String,
    val label: String,
)

private val topLevelRoutes = listOf(
    TopLevelRoute("home", "Beranda"),
    TopLevelRoute("search", "Cari"),
    TopLevelRoute("notifications", "Notifikasi"),
    TopLevelRoute("settings", "Pengaturan"),
)

@Composable
fun PbbKuNavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelRoutes.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(destination.label) },
                        icon = { Text(destination.label.take(1)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") { HomeScreen() }
            composable("search") { SearchScreen() }
            composable("notifications") { NotificationsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
