package com.turkcell.rencar_pair.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.rencar_pair.ui.onboarding.OnboardingRoute

private const val ROUTE_ONBOARDING = "onboarding"

@Composable
fun RencarNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_ONBOARDING,
        modifier = modifier,
    ) {
        composable(ROUTE_ONBOARDING) {
            OnboardingRoute(
                onNavigateToHome = { /* TODO: navigate to home when implemented */ },
                onNavigateToLogin = { /* TODO: navigate to login when implemented */ },
            )
        }
    }
}
