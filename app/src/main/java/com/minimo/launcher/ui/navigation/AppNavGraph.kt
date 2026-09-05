package com.minimo.launcher.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.minimo.launcher.ui.favourite_apps.FavouriteAppsScreen
import com.minimo.launcher.ui.favourite_apps.reorder_apps.ReorderAppsScreen
import com.minimo.launcher.ui.hidden_apps.HiddenAppsScreen
import com.minimo.launcher.ui.home.AppDrawerScreen
import com.minimo.launcher.ui.home.HomeScreen
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.ui.intro.IntroScreen
import com.minimo.launcher.ui.launch.LaunchScreen
import com.minimo.launcher.ui.settings.SettingsScreen
import com.minimo.launcher.ui.settings.SupporterScreen
import com.minimo.launcher.ui.settings.about.AboutAppScreen
import com.minimo.launcher.ui.settings.customisation.CustomisationScreen

private const val DRAWER_TRANSITION_DURATION_MILLIS = 300
private const val OUTGOING_SCREEN_FADE_DURATION_MILLIS = 50
private const val INCOMING_SCREEN_FADE_DELAY_MILLIS = 50

object Routes {
    const val LAUNCH = "LAUNCH"
    const val INTRO = "INTRO"
    const val HOME = "HOME"
    const val APP_DRAWER = "APP_DRAWER"
    const val SETTINGS = "SETTINGS"
    const val SETTINGS_CUSTOMISATION = "SETTINGS_CUSTOMISATION"
    const val HIDDEN_APPS = "HIDDEN_APPS"
    const val FAVOURITE_APPS = "FAVOURITE_APPS"
    const val SETTINGS_REORDER_APPS = "SETTINGS_REORDER_APPS"
    const val SUPPORTER = "SUPPORTER"
    const val ABOUT_APP = "ABOUT_APP"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    enableWallpaper: Boolean,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    onBackPressed: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LAUNCH,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { launcherPopEnterTransition() },
        popExitTransition = { launcherPopExitTransition() },
        // Navigation 2.10 defaults predictive Back to scaleOut; match toolbar Back instead.
        predictivePopEnterTransition = { launcherPopEnterTransition() },
        predictivePopExitTransition = { launcherPopExitTransition() }
    ) {
        composable(route = Routes.LAUNCH) {
            LaunchScreen(
                viewModel = hiltViewModel(it),
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(route = Routes.INTRO) {
            IntroScreen(
                viewModel = hiltViewModel(it),
                onIntroCompleted = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(
            route = Routes.HOME,
            exitTransition = {
                if (targetState.destination.route == Routes.APP_DRAWER) {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = OUTGOING_SCREEN_FADE_DURATION_MILLIS
                        )
                    ) +
                            slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
                                ),
                                targetOffsetY = { -it / 8 }
                            )
                } else {
                    ExitTransition.None
                }
            }
        ) {
            HomeScreen(
                viewModel = homeViewModel,
                enableWallpaper = enableWallpaper,
                statusBarVisible = statusBarVisible,
                navigationBarVisible = navigationBarVisible,
                onOpenAppDrawer = {
                    navController.navigate(Routes.APP_DRAWER) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Routes.APP_DRAWER,
            enterTransition = {
                fadeIn(
                    animationSpec = keyframes {
                        durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
                        // Keep Drawer invisible until Home is fully faded out.
                        0f at INCOMING_SCREEN_FADE_DELAY_MILLIS using FastOutSlowInEasing
                    }
                ) +
                        slideInVertically(
                            animationSpec = tween(
                                durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
                            ),
                            initialOffsetY = { it / 8 }
                        )
            },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { launcherPopExitTransition() }
        ) {
            AppDrawerScreen(
                viewModel = homeViewModel,
                statusBarVisible = statusBarVisible,
                navigationBarVisible = navigationBarVisible,
                onCloseAppDrawer = {
                    if (navController.currentDestination?.route == Routes.APP_DRAWER) {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(route = Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = onBackPressed,
                onHiddenAppsClick = {
                    navController.navigate(Routes.HIDDEN_APPS)
                },
                onCustomisationClick = {
                    navController.navigate(Routes.SETTINGS_CUSTOMISATION)
                },
                onFavouriteAppsClick = {
                    navController.navigate(Routes.FAVOURITE_APPS)
                },
                onSupporterClick = {
                    navController.navigate(Routes.SUPPORTER)
                },
                onAboutAppClick = {
                    navController.navigate(Routes.ABOUT_APP)
                }
            )
        }
        composable(route = Routes.ABOUT_APP) {
            AboutAppScreen(
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.SUPPORTER) {
            SupporterScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.HIDDEN_APPS) {
            HiddenAppsScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.SETTINGS_CUSTOMISATION) {
            CustomisationScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.FAVOURITE_APPS) {
            FavouriteAppsScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed,
                onReorderClick = {
                    navController.navigate(Routes.SETTINGS_REORDER_APPS)
                }
            )
        }
        composable(route = Routes.SETTINGS_REORDER_APPS) {
            ReorderAppsScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed,
            )
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.launcherPopEnterTransition(): EnterTransition {
    if (initialState.destination.route != Routes.APP_DRAWER ||
        targetState.destination.route != Routes.HOME
    ) return EnterTransition.None

    return fadeIn(
        animationSpec = keyframes {
            durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
            // Keep Home invisible until Drawer is fully faded out.
            0f at INCOMING_SCREEN_FADE_DELAY_MILLIS using FastOutSlowInEasing
        }
    ) + slideInVertically(
        animationSpec = tween(durationMillis = DRAWER_TRANSITION_DURATION_MILLIS),
        initialOffsetY = { -it / 8 }
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.launcherPopExitTransition(): ExitTransition {
    if (initialState.destination.route != Routes.APP_DRAWER) return ExitTransition.None

    return fadeOut(
        animationSpec = tween(durationMillis = OUTGOING_SCREEN_FADE_DURATION_MILLIS)
    ) + slideOutVertically(
        animationSpec = tween(durationMillis = DRAWER_TRANSITION_DURATION_MILLIS),
        targetOffsetY = { it / 8 }
    )
}
