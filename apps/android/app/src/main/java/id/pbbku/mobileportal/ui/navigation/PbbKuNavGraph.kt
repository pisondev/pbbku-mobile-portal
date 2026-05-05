package id.pbbku.mobileportal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.feature.auth.AuthViewModel
import id.pbbku.mobileportal.feature.auth.LoginScreen
import id.pbbku.mobileportal.feature.auth.OnboardingScreen
import id.pbbku.mobileportal.feature.auth.OtpScreen
import id.pbbku.mobileportal.feature.auth.SplashScreen
import id.pbbku.mobileportal.feature.building.BuildingDetailScreen
import id.pbbku.mobileportal.feature.building.BuildingListScreen
import id.pbbku.mobileportal.feature.home.HomeScreen
import id.pbbku.mobileportal.feature.notifications.NotificationsScreen
import id.pbbku.mobileportal.feature.objectdetail.ObjectDetailScreen
import id.pbbku.mobileportal.feature.payment.PaymentInfoScreen
import id.pbbku.mobileportal.feature.report.ReportDraftScreen
import id.pbbku.mobileportal.feature.search.SearchScreen
import id.pbbku.mobileportal.feature.settings.SettingsScreen
import id.pbbku.mobileportal.feature.sppt.SpptHistoryScreen
import id.pbbku.mobileportal.feature.sppt.TaxBillDetailScreen
import id.pbbku.mobileportal.feature.sppt.TunggakanScreen

private data class TopLevelRoute(
    val route: String,
    val label: String,
    val iconText: String,
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
    const val BUILDING_DETAIL = "building_detail"
    const val SPPT_HISTORY = "sppt_history"
    const val TAX_BILL_DETAIL = "tax_bill_detail"
    const val TUNGGAKAN = "tunggakan"
    const val PAYMENT_INFO = "payment_info"
    const val REPORT = "report"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
}

private val topLevelRoutes = listOf(
    TopLevelRoute(MainRoute.HOME, "Beranda", "B"),
    TopLevelRoute(MainRoute.SEARCH, "Cari", "C"),
    TopLevelRoute(MainRoute.NOTIFICATIONS, "Notifikasi", "N"),
    TopLevelRoute(MainRoute.SETTINGS, "Pengaturan", "P"),
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
            ) {
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
                        icon = {
                            Text(
                                text = destination.iconText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
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
            composable(MainRoute.HOME) {
                HomeScreen(
                    session = session,
                    onOpenSearch = { navController.navigate(MainRoute.SEARCH) },
                    onOpenNotifications = { navController.navigate(MainRoute.NOTIFICATIONS) },
                )
            }
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
                BuildingListScreen(
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenDetail = { nopDisplay, noBng ->
                        navController.navigate("${MainRoute.BUILDING_DETAIL}/$nopDisplay/$noBng")
                    },
                )
            }
            composable("${MainRoute.BUILDING_DETAIL}/{nopDisplay}/{noBng}") { backStackEntry ->
                val nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty()
                val noBng = backStackEntry.arguments?.getString("noBng").orEmpty()
                BuildingDetailScreen(
                    nopDisplay = nopDisplay,
                    noBng = noBng,
                    onBack = { navController.popBackStack() },
                    onOpenReport = { reportNopDisplay, reportNoBng ->
                        navController.navigate("${MainRoute.REPORT}/$reportNopDisplay/$reportNoBng")
                    },
                )
            }
            composable("${MainRoute.SPPT_HISTORY}/{nopDisplay}") { backStackEntry ->
                SpptHistoryScreen(
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenDetail = { detailNopDisplay, taxYear ->
                        navController.navigate("${MainRoute.TAX_BILL_DETAIL}/$detailNopDisplay/$taxYear")
                    },
                    onOpenPayment = { paymentNopDisplay, taxYear ->
                        navController.navigate("${MainRoute.PAYMENT_INFO}/$paymentNopDisplay/$taxYear")
                    },
                )
            }
            composable("${MainRoute.TUNGGAKAN}/{nopDisplay}") { backStackEntry ->
                TunggakanScreen(
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenDetail = { detailNopDisplay, taxYear ->
                        navController.navigate("${MainRoute.TAX_BILL_DETAIL}/$detailNopDisplay/$taxYear")
                    },
                    onOpenPayment = { paymentNopDisplay, taxYear ->
                        navController.navigate("${MainRoute.PAYMENT_INFO}/$paymentNopDisplay/$taxYear")
                    },
                )
            }
            composable("${MainRoute.TAX_BILL_DETAIL}/{nopDisplay}/{taxYear}") { backStackEntry ->
                val nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty()
                val taxYear = backStackEntry.arguments?.getString("taxYear").orEmpty()
                TaxBillDetailScreen(
                    nopDisplay = nopDisplay,
                    taxYear = taxYear,
                    onBack = { navController.popBackStack() },
                    onOpenPayment = { paymentNopDisplay, paymentTaxYear ->
                        navController.navigate("${MainRoute.PAYMENT_INFO}/$paymentNopDisplay/$paymentTaxYear")
                    },
                )
            }
            composable("${MainRoute.PAYMENT_INFO}/{nopDisplay}/{taxYear}") { backStackEntry ->
                PaymentInfoScreen(
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                    taxYear = backStackEntry.arguments?.getString("taxYear").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
            composable("${MainRoute.REPORT}/{nopDisplay}") { backStackEntry ->
                ReportDraftScreen(
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                    noBng = null,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("${MainRoute.REPORT}/{nopDisplay}/{noBng}") { backStackEntry ->
                ReportDraftScreen(
                    nopDisplay = backStackEntry.arguments?.getString("nopDisplay").orEmpty(),
                    noBng = backStackEntry.arguments?.getString("noBng").orEmpty(),
                    onBack = { navController.popBackStack() },
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

private fun NavController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
