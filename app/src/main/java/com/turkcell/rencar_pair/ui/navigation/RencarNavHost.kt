package com.turkcell.rencar_pair.ui.navigation

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
import com.turkcell.rencar_pair.ui.home.HomeScreen
import com.turkcell.rencar_pair.ui.onboarding.OnboardingRoute
import com.turkcell.rencar_pair.ui.splash.SplashRoute

private const val ROUTE_SPLASH     = "splash"
private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_LOGIN      = "login"
private const val ROUTE_OTP        = "otp"
private const val ROUTE_REGISTER   = "register"
private const val ROUTE_LICENSE    = "license"
private const val ROUTE_HOME       = "home"

@Composable
fun RencarNavHost(
    tokenManager: TokenManager,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
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
            HomeScreen(
                tokenManager = tokenManager,
                onLogout = {
                    navController.navigate(ROUTE_ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
