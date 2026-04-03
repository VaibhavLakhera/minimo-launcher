package com.minimo.launcher.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.minimo.launcher.ui.favourite_apps.FavouriteAppsScreen
import com.minimo.launcher.ui.favourite_apps.reorder_apps.ReorderAppsScreen
import com.minimo.launcher.ui.hidden_apps.HiddenAppsScreen
import com.minimo.launcher.ui.home.HomeScreen
import com.minimo.launcher.ui.intro.IntroScreen
import com.minimo.launcher.ui.launch.LaunchScreen
import com.minimo.launcher.ui.settings.SettingsScreen
import com.minimo.launcher.ui.settings.SupporterScreen
import com.minimo.launcher.ui.settings.customisation.CustomisationScreen

object Routes {
    const val LAUNCH = "LAUNCH"
    const val INTRO = "INTRO"
    const val HOME = "HOME"
    const val SETTINGS = "SETTINGS"
    const val SETTINGS_CUSTOMISATION = "SETTINGS_CUSTOMISATION"
    const val HIDDEN_APPS = "HIDDEN_APPS"
    const val FAVOURITE_APPS = "FAVOURITE_APPS"
    const val SETTINGS_REORDER_APPS = "SETTINGS_REORDER_APPS"
    const val SUPPORTER = "SUPPORTER"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
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
            SolidScreenWrapper {
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
        }
        composable(route = Routes.INTRO) {
            SolidScreenWrapper {
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
        }
        composable(route = Routes.HOME) {
            HomeScreen(
                viewModel = hiltViewModel(it),
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onAddFavouriteAppsClick = {
                    navController.navigate(Routes.FAVOURITE_APPS)
                }
            )
        }
        composable(route = Routes.SETTINGS) {
            SolidScreenWrapper {
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
                    }
                )
            }
        }
        composable(route = Routes.SUPPORTER) {
            SolidScreenWrapper {
                SupporterScreen(
                    viewModel = hiltViewModel(it),
                    onBackClick = onBackPressed
                )
            }
        }
        composable(route = Routes.HIDDEN_APPS) {
            SolidScreenWrapper {
                HiddenAppsScreen(
                    viewModel = hiltViewModel(it),
                    onBackClick = onBackPressed
                )
            }
        }
        composable(route = Routes.SETTINGS_CUSTOMISATION) {
            SolidScreenWrapper {
                CustomisationScreen(
                    viewModel = hiltViewModel(it),
                    onBackClick = onBackPressed
                )
            }
        }
        composable(route = Routes.FAVOURITE_APPS) {
            SolidScreenWrapper {
                FavouriteAppsScreen(
                    viewModel = hiltViewModel(it),
                    onBackClick = onBackPressed,
                    onReorderClick = {
                        navController.navigate(Routes.SETTINGS_REORDER_APPS)
                    }
                )
            }
        }
        composable(route = Routes.SETTINGS_REORDER_APPS) {
            SolidScreenWrapper {
                ReorderAppsScreen(
                    viewModel = hiltViewModel(it),
                    onBackClick = onBackPressed,
                )
            }
        }
    }
}

// A screen wrapper applied to all except home because we have option to show wallpaper on home.
@Composable
private fun SolidScreenWrapper(content: @Composable () -> Unit) {
    val safeInsets =
        WindowInsets.statusBars.union(WindowInsets.ime).union(WindowInsets.displayCutout)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(safeInsets)
    ) {
        content()
    }
}
