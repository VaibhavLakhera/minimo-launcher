package com.minimo.launcher.data

import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.MinimoSettingsPosition

data class MainPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontPreference: String = "",
    val showStatusBar: Boolean = true,
    val dynamicTheme: Boolean = false,
    val blackTheme: Boolean = false,
    val setWallpaperToThemeColor: Boolean = false,
    val enableWallpaper: Boolean = false,
    val lightTextOnWallpaper: Boolean = true
)

data class HomePreferences(
    val homeAppsAlignmentHorizontal: HomeAppsAlignmentHorizontal = HomeAppsAlignmentHorizontal.Start,
    val homeAppsAlignmentVertical: HomeAppsAlignmentVertical = HomeAppsAlignmentVertical.Center,
    val homeClockAlignment: HomeClockAlignment = HomeClockAlignment.Start,
    val showHomeClock: Boolean = false,
    val homeTextSize: Int = Constants.DEFAULT_HOME_TEXT_SIZE,
    val autoOpenKeyboardAllApps: Boolean = false,
    val homeClockMode: HomeClockMode = HomeClockMode.Full,
    val doubleTapToLock: Boolean = false,
    val twentyFourHourFormat: Boolean = false,
    val showBatteryLevel: Boolean = false,
    val showHiddenAppsInSearch: Boolean = true,
    val drawerSearchBarAtBottom: Boolean = false,
    val applyHomeAppSizeToAllApps: Boolean = false,
    val autoOpenApp: Boolean = false,
    val hideAppDrawerArrow: Boolean = false,
    val homeAppVerticalPadding: Int = Constants.DEFAULT_HOME_VERTICAL_PADDING,
    val ignoreSpecialCharacters: String = "",
    val hideAppDrawerSearch: Boolean = false,
    val minimoSettingsPosition: MinimoSettingsPosition = MinimoSettingsPosition.Auto,
    val enableWallpaper: Boolean = false,
    val showScreenTimeWidget: Boolean = false,
    val lightTextOnWallpaper: Boolean = true,
    val dimWallpaper: Boolean = false,
    val clockAppPreference: String = "",
    val calendarAppPreference: String = "",
    val screenTimeAppPreference: String = "",
    val swipeLeftAppPreference: String = "",
    val swipeRightAppPreference: String = ""
)

data class CustomisationPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontPreference: String = "",
    val homeAppsAlignmentHorizontal: HomeAppsAlignmentHorizontal = HomeAppsAlignmentHorizontal.Start,
    val homeAppsAlignmentVertical: HomeAppsAlignmentVertical = HomeAppsAlignmentVertical.Center,
    val homeClockAlignment: HomeClockAlignment = HomeClockAlignment.Start,
    val showHomeClock: Boolean = false,
    val showStatusBar: Boolean = true,
    val homeTextSize: Int = Constants.DEFAULT_HOME_TEXT_SIZE,
    val autoOpenKeyboardAllApps: Boolean = false,
    val dynamicTheme: Boolean = false,
    val homeClockMode: HomeClockMode = HomeClockMode.Full,
    val doubleTapToLock: Boolean = false,
    val twentyFourHourFormat: Boolean = false,
    val showBatteryLevel: Boolean = false,
    val showHiddenAppsInSearch: Boolean = true,
    val drawerSearchBarAtBottom: Boolean = false,
    val applyHomeAppSizeToAllApps: Boolean = false,
    val blackTheme: Boolean = false,
    val setWallpaperToThemeColor: Boolean = false,
    val enableWallpaper: Boolean = false,
    val lightTextOnWallpaper: Boolean = true,
    val dimWallpaper: Boolean = false,
    val autoOpenApp: Boolean = false,
    val hideAppDrawerArrow: Boolean = false,
    val notificationDot: Boolean = false,
    val homeAppVerticalPadding: Int = Constants.DEFAULT_HOME_VERTICAL_PADDING,
    val ignoreSpecialCharacters: String = "",
    val hideAppDrawerSearch: Boolean = false,
    val minimoSettingsPosition: MinimoSettingsPosition = MinimoSettingsPosition.Auto,
    val showScreenTimeWidget: Boolean = false,
    val clockAppPreference: String = "",
    val calendarAppPreference: String = "",
    val screenTimeAppPreference: String = "",
    val swipeLeftAppPreference: String = "",
    val swipeRightAppPreference: String = ""
)
