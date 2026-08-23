package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.ui.components.ScreenTimeView
import com.minimo.launcher.ui.components.TimeAndDateView
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.home.HomeScreenState
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.launchAppInfo
import com.minimo.launcher.utils.openDefaultCalendarApp
import com.minimo.launcher.utils.openDefaultClockApp
import com.minimo.launcher.utils.openDigitalWellbeing
import com.minimo.launcher.utils.openPowerUsageSummary
import com.minimo.launcher.utils.uninstallApp

@Composable
fun HomeBody(
    paddingValues: PaddingValues,
    state: HomeScreenState,
    viewModel: HomeViewModel,
    homeLazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    systemNavigationHeight: Dp,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    useDarkBottomSheetStatusBarIcons: Boolean,
    useDarkBottomSheetNavigationBarIcons: Boolean,
    onDeleteShortcutClick: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    val iconCacheRevision by viewModel.iconCacheRevision.collectAsStateWithLifecycle()

    fun launchPreferredApp(preference: String, fallback: () -> Unit) {
        if (!viewModel.onPreferenceAppLaunchRequest(preference)) {
            fallback()
        }
    }

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val textColor =
        remember(state.enableWallpaper, state.lightTextOnWallpaper) {
            if (state.enableWallpaper) {
                if (state.lightTextOnWallpaper) Color.White else Color.Black
            } else {
                onSurfaceColor
            }
        }

    val textShadow = remember(state.enableWallpaper, state.lightTextOnWallpaper) {
        if (state.enableWallpaper && state.lightTextOnWallpaper) {
            Shadow(
                color = Color.Black.copy(alpha = 0.5f),
                offset = Offset(2f, 2f),
                blurRadius = 4f
            )
        } else {
            null
        }
    }

    val lazyColumnPadding = remember(systemNavigationHeight, paddingValues) {
        PaddingValues(
            bottom = max(systemNavigationHeight, paddingValues.calculateBottomPadding()) + 16.dp
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
    ) {
        if (state.showHomeClock || state.showScreenTimeWidget) {
            Column(
                modifier = Modifier.padding(
                    horizontal = Dimens.APP_HORIZONTAL_SPACING,
                    vertical = 16.dp
                )
            ) {
                if (state.showHomeClock) {
                    TimeAndDateView(
                        horizontalAlignment = state.homeClockAlignment,
                        clockMode = state.homeClockMode,
                        twentyFourHourFormat = state.twentyFourHourFormat,
                        showBatteryLevel = state.showBatteryLevel,
                        textColor = textColor,
                        textShadow = textShadow,
                        onClockClick = {
                            launchPreferredApp(state.clockAppPreference) {
                                context.openDefaultClockApp()
                            }
                        },
                        onDateClick = {
                            launchPreferredApp(state.calendarAppPreference) {
                                context.openDefaultCalendarApp()
                            }
                        },
                        onBatteryClick = {
                            launchPreferredApp(state.batteryAppPreference) {
                                context.openPowerUsageSummary()
                            }
                        }
                    )
                }

                if (state.showScreenTimeWidget && state.screenTime.isNotEmpty()) {
                    if (state.showHomeClock) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    ScreenTimeView(
                        horizontalAlignment = state.homeClockAlignment,
                        screenTime = state.screenTime,
                        refreshScreenTime = viewModel::refreshScreenTime,
                        onClick = {
                            launchPreferredApp(state.screenTimeAppPreference) {
                                context.openDigitalWellbeing()
                            }
                        },
                        textColor = textColor,
                        textShadow = textShadow
                    )
                }
            }
        }

        LazyColumn(
            state = homeLazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = lazyColumnPadding,
            verticalArrangement = state.appsArrangementVertical
        ) {
            items(items = state.favouriteApps, key = { it.id }) { appInfo ->
                val textSize = state.homeTextSize.sp
                val appIconSizeScale = state.appIconSizePercent / 100f
                val iconSizePx = with(LocalDensity.current) {
                    appIconSizeFor(textSize, appIconSizeScale).roundToPx()
                }
                val appIcon by produceState<ImageBitmap?>(
                    initialValue = null,
                    key1 = state.showAppIconInHome,
                    key2 = appInfo.id,
                    key3 = iconSizePx to iconCacheRevision
                ) {
                    if (state.showAppIconInHome) {
                        value = viewModel.loadAppIcon(appInfo, iconSizePx)
                    }
                }

                AppNameItem(
                    modifier = Modifier.animateItem(),
                    appName = appInfo.name,
                    isFavourite = appInfo.isFavourite,
                    isHidden = appInfo.isHidden,
                    isShortcut = appInfo.isShortcut,
                    isWorkProfile = appInfo.isWorkProfile,
                    onClick = { viewModel.onAppLaunchRequest(appInfo) },
                    onToggleFavouriteClick = {
                        viewModel.onToggleFavouriteAppClick(
                            appInfo
                        )
                    },
                    onRenameClick = { viewModel.onRenameAppClick(appInfo) },
                    onToggleHideClick = { viewModel.onToggleHideClick(appInfo) },
                    onAppInfoClick = { context.launchAppInfo(appInfo) },
                    onLaunchDelayClick = { viewModel.onLaunchDelayClick(appInfo) },
                    appsArrangement = state.appsArrangementHorizontal,
                    textSize = textSize,
                    onUninstallClick = { context.uninstallApp(appInfo) },
                    onDeleteShortcutClick = { onDeleteShortcutClick(appInfo) },
                    showNotificationDot = appInfo.showNotificationDot,
                    compactTouchArea = state.compactAppTouchArea,
                    showAppIcon = state.showAppIconInHome,
                    appIcon = appIcon,
                    appIconSizeScale = appIconSizeScale,
                    appIconAlignment = state.homeAppIconAlignment,
                    verticalPadding = state.homeAppVerticalPadding.dp,
                    bottomSheetStatusBarVisible = statusBarVisible,
                    bottomSheetNavigationBarVisible = navigationBarVisible,
                    useDarkBottomSheetStatusBarIcons = useDarkBottomSheetStatusBarIcons,
                    useDarkBottomSheetNavigationBarIcons = useDarkBottomSheetNavigationBarIcons,
                    textColor = textColor,
                    shadow = textShadow
                )
            }

        }
    }
}
