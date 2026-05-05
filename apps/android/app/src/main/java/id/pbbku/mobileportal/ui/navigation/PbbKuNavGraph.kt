package id.pbbku.mobileportal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.feature.auth.AuthViewModel
import id.pbbku.mobileportal.feature.auth.LoginScreen
import id.pbbku.mobileportal.feature.auth.OnboardingScreen
import id.pbbku.mobileportal.feature.auth.OtpScreen
import id.pbbku.mobileportal.feature.auth.SplashScreen
import id.pbbku.mobileportal.feature.home.HomeScreen
import id.pbbku.mobileportal.feature.notifications.NotificationsScreen
import id.pbbku.mobileportal.feature.objectdetail.ObjectDetailScreen
import id.pbbku.mobileportal.feature.search.SearchScreen
import id.pbbku.mobileportal.feature.settings.SettingsScreen
import id.pbbku.mobileportal.ui.screen.PlaceholderScreen

private data class TopLevelRoute(
    val route: String,
    val label: String,
)

private object AppRoute {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val OTP = "otp"
    const val MAIN = "main"
}

private object MainRoute {
    const val HOME = "home"
    const val SEARCH = "search"
    const val OBJECT_DETAIL = "object_detail"
    const val BUILDINGS = "buildings"
    const val SPPT_HISTORY = "sppt_history"
    const val TUNGGAKAN = "tunggakan"
    const val REPORT = "report"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
}

private val topLevelRoutes = listOf(
    TopLevelRoute(MainRoute.HOME, "Beranda"),
    TopLevelRoute(MainRoute.SEARCH, "Cari"),
    TopLevelRoute(MainRoute.NOTIFICATIONS, "Notifikasi"),
    TopLevelRoute(MainRoute.SETTINGS, "Pengaturan"),
)

@Composable
fun PbbKuNavGraph(
    authViewModel: AuthViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val session by authViewModel.session.collectAsState()

    NavHost(
        navController = navController,
        startDestination = AppRoute.SPLASH,
    ) {
        composable(AppRoute.SPLASH) {
            SplashScreen()
            LaunchedEffect(session) {
                when (session?.isLoggedIn) {
                    true -> navController.navigateAndClear(AppRoute.MAIN)
                    false -> navController.navigateAndClear(AppRoute.ONBOARDING)
                    null -> Unit
                }
            }
        }
        composable(AppRoute.ONBOARDING) {
            OnboardingScreen(
                onContinue = { navController.navigate(AppRoute.LOGIN) },
            )
        }
        composable(AppRoute.LOGIN) {
            LoginScreen(
                onSubmitNik = authViewModel::requestOtp,
                onOtpRequested = { navController.navigate(AppRoute.OTP) },
            )
        }
        composable(AppRoute.OTP) {
            LaunchedEffect(Unit) {
                if (!authViewModel.hasPendingNik()) {
                    navController.navigateAndClear(AppRoute.LOGIN)
                }
            }
            OtpScreen(
                onVerifyOtp = authViewModel::verifyOtp,
                onVerified = { navController.navigateAndClear(AppRoute.MAIN) },
            )
        }
        composable(AppRoute.MAIN) {
            MainScaffold(
                session = session,
                onLogout = authViewModel::logout,
                onLoggedOut = { navController.navigateAndClear(AppRoute.LOGIN) },
            )
        }
    }
}

@Composable
private fun MainScaffold(
    session: SimulatedSession?,
    onLogout: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination

    LaunchedEffect(session?.isLoggedIn) {
        if (session?.isLoggedIn == false) {
            onLoggedOut()
        }
    }

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
                                popUpTo(navController.graph.findStartDestination().id) {
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
            startDestination = MainRoute.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MainRoute.HOME) { HomeScreen(session = session) }
            composable(MainRoute.SEARCH) {
                SearchScreen(
                    onOpenDetail = { nopDisplay ->
                        navController.navigate("${MainRoute.OBJECT_DETAIL}/$nopDisplay")
                    },
                )
            }
            composable("${MainRoute.OBJECT_DETAIL}/{nopDisplay}") { backStackEntry ->
                val nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty()
                ObjectDetailScreen(
                    nopDisplay = nopDisplay,
                    onBack = { navController.popBackStack() },
                    onOpenBuilding = { navController.navigate("${MainRoute.BUILDINGS}/$it") },
                    onOpenSpptHistory = { navController.navigate("${MainRoute.SPPT_HISTORY}/$it") },
                    onOpenTunggakan = { navController.navigate("${MainRoute.TUNGGAKAN}/$it") },
                    onOpenReport = { navController.navigate("${MainRoute.REPORT}/$it") },
                )
            }
            composable("${MainRoute.BUILDINGS}/{nopDisplay}") { backStackEntry ->
                RelatedPlaceholderScreen(
                    title = "Bangunan",
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                )
            }
            composable("${MainRoute.SPPT_HISTORY}/{nopDisplay}") { backStackEntry ->
                RelatedPlaceholderScreen(
                    title = "Histori SPPT",
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                )
            }
            composable("${MainRoute.TUNGGAKAN}/{nopDisplay}") { backStackEntry ->
                RelatedPlaceholderScreen(
                    title = "Tunggakan",
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                )
            }
            composable("${MainRoute.REPORT}/{nopDisplay}") { backStackEntry ->
                RelatedPlaceholderScreen(
                    title = "Laporan Perubahan Bangunan",
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                )
            }
            composable(MainRoute.NOTIFICATIONS) { NotificationsScreen() }
            composable(MainRoute.SETTINGS) {
                SettingsScreen(
                    session = session,
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun RelatedPlaceholderScreen(
    title: String,
    nopDisplay: String,
) {
    PlaceholderScreen(
        title = title,
        items = listOf(
            "NOP: ${Nop.parseOrNull(nopDisplay)?.asGroupedText() ?: nopDisplay}",
            "Fitur ini akan dilanjutkan pada tahap kontrak berikutnya.",
        ),
    )
}

private fun NavController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
