package com.minimo.launcher.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.minimo.launcher.ui.settings.web_shortcuts.WebShortcutsScreen

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
    const val WEB_SHORTCUTS = "WEB_SHORTCUTS"
    const val SUPPORTER = "SUPPORTER"
    const val ABOUT_APP = "ABOUT_APP"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    onBackPressed: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LAUNCH,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
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
        composable(route = Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                statusBarVisible = statusBarVisible,
                navigationBarVisible = navigationBarVisible,
                onOpenAppDrawer = {
                    navController.navigate(Routes.APP_DRAWER) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Routes.APP_DRAWER,
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 180)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 180),
                            initialOffsetY = { it / 8 }
                        )
            },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 160)) +
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 160),
                            targetOffsetY = { it / 8 }
                        )
            }
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
                },
                onWebShortcutsClick = {
                    navController.navigate(Routes.WEB_SHORTCUTS)
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
        composable(route = Routes.WEB_SHORTCUTS) {
            WebShortcutsScreen(
                onBackClick = onBackPressed
            )
        }
    }
}
