package com.minimo.launcher.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            val navController = rememberNavController()
            val state by viewModel.state.collectAsState()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val isHomeScreen = navBackStackEntry?.destination?.route == Routes.HOME

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
                enableWallpaper = state.enableWallpaper,
                isHomeScreen = isHomeScreen,
                lightTextOnWallpaper = state.lightTextOnWallpaper,
                fontPreference = state.fontPreference
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

    private fun setupOrientationChangeListener() {
        lifecycleScope.launch {
            viewModel.state
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
