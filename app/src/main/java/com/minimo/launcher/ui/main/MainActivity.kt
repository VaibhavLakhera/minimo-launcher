package com.minimo.launcher.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.ui.navigation.AppNavGraph
import com.minimo.launcher.ui.navigation.Routes
import com.minimo.launcher.ui.theme.AppTheme
import com.minimo.launcher.utils.AppsManager
import com.minimo.launcher.utils.HomePressedNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appsManager: AppsManager

    private val viewModel: MainViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setupOrientationChangeListener()
        appsManager.registerCallback()

        setContent {
            val state by viewModel.state.collectAsState()
            // Keep the transparent window background until the saved appearance is available.
            if (!state.initialLoaded) return@setContent

            val navController = rememberNavController()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val isHomeScreen = currentRoute == Routes.HOME
            val enableWallpaperOnCurrentScreen = when (currentRoute) {
                null, Routes.LAUNCH, Routes.HOME -> state.enableWallpaper
                Routes.APP_DRAWER -> state.enableWallpaperOnDrawer
                else -> false
            }

            ObserveNavigationEvents(
                navController = navController,
                homePressedNotifier = viewModel.getHomePressedNotifier()
            )

            AppTheme(
                themeMode = state.themeMode,
                statusBarVisible = state.statusBarVisible,
                navigationBarVisible = state.navigationBarVisible,
                useDynamicTheme = state.useDynamicTheme,
                blackTheme = state.blackTheme,
                setWallpaperToThemeColor = state.setWallpaperToThemeColor,
                enableWallpaper = enableWallpaperOnCurrentScreen,
                isHomeScreen = isHomeScreen,
                lightTextOnWallpaper = state.lightTextOnWallpaper,
                fontPreference = state.fontPreference
            ) {
                val backgroundColor = when {
                    !enableWallpaperOnCurrentScreen -> MaterialTheme.colorScheme.surface
                    state.dimWallpaper -> Color.Black.copy(
                        alpha = state.dimWallpaperPercentage / 100f
                    )

                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                ) {
                    AppNavGraph(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        enableWallpaper = state.enableWallpaper,
                        statusBarVisible = state.statusBarVisible,
                        navigationBarVisible = state.navigationBarVisible,
                        onBackPressed = {
                            onBackPressedDispatcher.onBackPressed()
                        }
                    )
                }
            }
        }
    }

    private fun setupOrientationChangeListener() {
        lifecycleScope.launch {
            viewModel.state
                .filter { it.initialLoaded }
                .map { it.screenOrientation.orientation }
                .distinctUntilChanged()
                .collect { orientation ->
                    requestedOrientation = orientation
                }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.onHomeButtonPressed()
        }
    }

    override fun onDestroy() {
        appsManager.unregisterCallback()
        super.onDestroy()
    }
}

@Composable
private fun ObserveNavigationEvents(
    navController: NavHostController,
    homePressedNotifier: HomePressedNotifier
) {
    LaunchedEffect(
        key1 = navController,
        key2 = homePressedNotifier
    ) {
        homePressedNotifier.homePressedEvent.collect {
            if (navController.currentDestination?.route != Routes.HOME) {
                navController.popBackStack(Routes.HOME, inclusive = false)
            }
        }
    }
}
