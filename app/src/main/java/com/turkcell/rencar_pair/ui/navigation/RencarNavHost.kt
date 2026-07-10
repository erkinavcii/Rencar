package com.turkcell.rencar_pair.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.rencar_pair.data.local.TokenManager
import com.turkcell.rencar_pair.ui.auth.license.LicenseRoute
import com.turkcell.rencar_pair.ui.auth.login.LoginRoute
import com.turkcell.rencar_pair.ui.auth.otp.OtpRoute
import com.turkcell.rencar_pair.ui.auth.register.RegisterRoute
import com.turkcell.rencar_pair.ui.home.HomeRoute
import com.turkcell.rencar_pair.ui.onboarding.OnboardingRoute
import com.turkcell.rencar_pair.ui.splash.SplashRoute
import com.turkcell.rencar_pair.ui.history.HistoryRoute
import com.turkcell.rencar_pair.ui.profile.ProfileRoute
import com.turkcell.rencar_pair.ui.reservation.ReservationRoute
import com.turkcell.rencar_pair.ui.activerental.ActiveRentalRoute
import com.turkcell.rencar_pair.ui.tripsummary.TripSummaryRoute
import com.turkcell.rencar_pair.ui.vehiclecondition.VehicleConditionRoute
import com.turkcell.rencar_pair.ui.wallet.WalletRoute

private const val ROUTE_SPLASH     = "splash"
private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_LOGIN      = "login"
private const val ROUTE_OTP        = "otp"
private const val ROUTE_REGISTER   = "register"
private const val ROUTE_LICENSE    = "license"
private const val ROUTE_HOME       = "home"
private const val ROUTE_WALLET     = "wallet"
private const val ROUTE_HISTORY    = "history"
private const val ROUTE_PROFILE    = "profile"
private const val ROUTE_RESERVATION = "reservation"
private const val ROUTE_VEHICLE_CONDITION = "vehicle-condition"
private const val ROUTE_ACTIVE_RENTAL = "active-rental"
private const val ROUTE_TRIP_SUMMARY = "trip-summary"

@Composable
fun RencarNavHost(
    tokenManager: TokenManager,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val handleTabNavigation: (NavigationTab) -> Unit = { tab ->
        val targetRoute = when (tab) {
            NavigationTab.HARITA -> ROUTE_HOME
            NavigationTab.GECMIS -> ROUTE_HISTORY
            NavigationTab.CUZDAN -> ROUTE_WALLET
            NavigationTab.PROFIL -> ROUTE_PROFILE
        }
        navController.navigate(targetRoute) {
            // Keep the state of the tabs and avoid multiple instances
            popUpTo(ROUTE_HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_SPLASH,
        modifier = modifier,
    ) {
        composable(ROUTE_SPLASH) {
            SplashRoute(
                onSplashFinished = {
                    val destination =
                        if (tokenManager.getAccessToken() != null) ROUTE_HOME else ROUTE_ONBOARDING
                    navController.navigate(destination) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(ROUTE_ONBOARDING) {
            OnboardingRoute(
                onNavigateToRegister = { navController.navigate(ROUTE_REGISTER) },
                onNavigateToLogin    = { navController.navigate(ROUTE_LOGIN) },
            )
        }

        composable(ROUTE_LOGIN) {
            LoginRoute(
                onNavigateToOtp      = { phone -> navController.navigate("$ROUTE_OTP/$phone") },
                onNavigateToRegister = { navController.navigate(ROUTE_REGISTER) },
                onBack               = { navController.popBackStack() },
            )
        }

        composable(
            route = "$ROUTE_OTP/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType }),
        ) {
            OtpRoute(
                onNavigateToHome = { role ->
                    val destination = if (role == "PENDING") ROUTE_LICENSE else ROUTE_HOME
                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(ROUTE_REGISTER) {
            RegisterRoute(
                onNavigateToOnboarding = {
                    navController.popBackStack(ROUTE_ONBOARDING, inclusive = false)
                },
                onNavigateToLogin = { navController.navigate(ROUTE_LOGIN) },
                onBack            = { navController.popBackStack() },
            )
        }

        composable(ROUTE_LICENSE) {
            LicenseRoute(
                onNavigateToNext = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_HOME) {
            HomeRoute(
                onTabSelected = handleTabNavigation,
                onNavigateToReservation = { vehicleId, brand, model, plate, pricePerDay ->
                    navController.navigate(
                        "$ROUTE_RESERVATION/$vehicleId/$brand/$model/$plate/$pricePerDay",
                    )
                },
            )
        }

        composable(
            route = "$ROUTE_RESERVATION/{vehicleId}/{brand}/{model}/{plate}/{pricePerDay}",
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("brand") { type = NavType.StringType },
                navArgument("model") { type = NavType.StringType },
                navArgument("plate") { type = NavType.StringType },
                navArgument("pricePerDay") { type = NavType.StringType },
            ),
        ) {
            ReservationRoute(
                onBack = { navController.popBackStack() },
                onNavigateToVehicleCondition = { rentalId, vehicleId, brand, model, plate, pricePerDay ->
                    navController.navigate(
                        "$ROUTE_VEHICLE_CONDITION/$rentalId/$vehicleId/$brand/$model/$plate/$pricePerDay",
                    ) {
                        popUpTo(ROUTE_HOME)
                    }
                },
            )
        }

        composable(
            route = "$ROUTE_VEHICLE_CONDITION/{rentalId}/{vehicleId}/{brand}/{model}/{plate}/{pricePerDay}",
            arguments = listOf(
                navArgument("rentalId") { type = NavType.StringType },
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("brand") { type = NavType.StringType },
                navArgument("model") { type = NavType.StringType },
                navArgument("plate") { type = NavType.StringType },
                navArgument("pricePerDay") { type = NavType.StringType },
            ),
        ) {
            VehicleConditionRoute(
                onBack = { navController.popBackStack() },
                onNavigateToActiveRental = { rentalId, vehicleId, brand, model, plate, pricePerDay ->
                    navController.navigate(
                        "$ROUTE_ACTIVE_RENTAL/$rentalId/$vehicleId/$brand/$model/$plate/$pricePerDay",
                    ) {
                        popUpTo(ROUTE_HOME)
                    }
                },
            )
        }

        composable(
            route = "$ROUTE_ACTIVE_RENTAL/{rentalId}/{vehicleId}/{brand}/{model}/{plate}/{pricePerDay}",
            arguments = listOf(
                navArgument("rentalId") { type = NavType.StringType },
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("brand") { type = NavType.StringType },
                navArgument("model") { type = NavType.StringType },
                navArgument("plate") { type = NavType.StringType },
                navArgument("pricePerDay") { type = NavType.StringType },
            ),
        ) {
            ActiveRentalRoute(
                onBack = { navController.popBackStack() },
                onNavigateToTripSummary = { rentalId, brand, model, plate, durationSeconds, distanceMeters, totalPrice ->
                    navController.navigate(
                        "$ROUTE_TRIP_SUMMARY/$rentalId/$brand/$model/$plate/$durationSeconds/$distanceMeters/$totalPrice",
                    ) {
                        popUpTo(ROUTE_HOME)
                    }
                },
            )
        }

        composable(
            route = "$ROUTE_TRIP_SUMMARY/{rentalId}/{brand}/{model}/{plate}/{durationSeconds}/{distanceMeters}/{totalPrice}",
            arguments = listOf(
                navArgument("rentalId") { type = NavType.StringType },
                navArgument("brand") { type = NavType.StringType },
                navArgument("model") { type = NavType.StringType },
                navArgument("plate") { type = NavType.StringType },
                navArgument("durationSeconds") { type = NavType.StringType },
                navArgument("distanceMeters") { type = NavType.StringType },
                navArgument("totalPrice") { type = NavType.StringType },
            ),
        ) {
            TripSummaryRoute(
                onNavigateHome = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(ROUTE_WALLET) {
            WalletRoute(
                onBack = {
                    navController.popBackStack()
                },
                onTabSelected = handleTabNavigation
            )
        }

        composable(ROUTE_HISTORY) {
            HistoryRoute(
                onTabSelected = handleTabNavigation,
            )
        }

        composable(ROUTE_PROFILE) {
            ProfileRoute(
                onNavigateToOnboarding = {
                    navController.navigate(ROUTE_ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onTabSelected = handleTabNavigation,
            )
        }
    }
}