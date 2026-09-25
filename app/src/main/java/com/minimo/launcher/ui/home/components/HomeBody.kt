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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.ui.components.ScreenTimeView
import com.minimo.launcher.ui.components.TimeAndDateView
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.home.HomeScreenState
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.openDefaultCalendarApp
import com.minimo.launcher.utils.openDefaultClockApp
import com.minimo.launcher.utils.openDigitalWellbeing
import com.minimo.launcher.utils.openPowerUsageSummary

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
        remember(state.enableWallpaper, state.lightTextOnWallpaper, onSurfaceColor) {
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
        if (state.showHomeClock || state.showBatteryLevel || state.showScreenTimeWidget) {
            Column(
                modifier = Modifier.padding(
                    horizontal = Dimens.APP_HORIZONTAL_SPACING,
                    vertical = 16.dp
                )
            ) {
                if (state.showHomeClock || state.showBatteryLevel) {
                    TimeAndDateView(
                        horizontalAlignment = state.homeClockAlignment,
                        showHomeClock = state.showHomeClock,
                        clockMode = state.homeClockMode,
                        timeTextSize = state.homeTimeTextSize,
                        dateTextSize = state.homeDateTextSize,
                        timeFont = state.homeTimeFont,
                        dateFormat = state.homeDateFormat,
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
                    if (state.showHomeClock || state.showBatteryLevel) {
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

        val homeRows = remember(state.folders, state.favouriteApps) { state.homeRows }
        LazyColumn(
            state = homeLazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = lazyColumnPadding,
            verticalArrangement = state.appsArrangementVertical
        ) {
            items(items = homeRows, key = { it.key }) { row ->
                LauncherListItem(
                    modifier = Modifier.animateItem(),
                    row = row,
                    state = state,
                    viewModel = viewModel,
                    home = true,
                    iconCacheRevision = iconCacheRevision,
                    textColor = textColor,
                    textShadow = textShadow,
                    statusBarVisible = statusBarVisible,
                    navigationBarVisible = navigationBarVisible,
                    useDarkStatusBarIcons = useDarkBottomSheetStatusBarIcons,
                    useDarkNavigationBarIcons = useDarkBottomSheetNavigationBarIcons,
                    onDeleteShortcut = onDeleteShortcutClick
                )
            }

        }
    }
}
