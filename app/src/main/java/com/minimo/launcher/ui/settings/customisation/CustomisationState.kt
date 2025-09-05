package com.minimo.launcher.ui.settings.customisation

import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeAppsAlignment
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode

data class CustomisationState(
    val themeMode: ThemeMode? = null,
    val homeAppsAlignment: HomeAppsAlignment? = null,
    val homeClockAlignment: HomeClockAlignment? = null,
    val showHomeClock: Boolean = false,
    val showStatusBar: Boolean = true,
    val homeTextSize: Float = Constants.DEFAULT_HOME_TEXT_SIZE.toFloat(),
    val autoOpenKeyboardAllApps: Boolean = false,
    val dynamicTheme: Boolean = false,
    val blackTheme: Boolean = false,
    val homeClockMode: HomeClockMode? = null,
    val doubleTapToLock: Boolean = false,
    val twentyFourHourFormat: Boolean = false,
    val showBatteryLevel: Boolean = false,
    val showHiddenAppsInSearch: Boolean = true,
    val drawerSearchBarAtBottom: Boolean = false,
    val applyHomeAppSizeToAllApps: Boolean = false,
    val autoOpenApp: Boolean = false,
    val hideAppDrawerArrow: Boolean = false,
    val notificationDot: Boolean = false,
    val homeAppVerticalPadding: Float = Constants.DEFAULT_HOME_VERTICAL_PADDING.toFloat(),
    val ignoreSpecialCharacters: String = ""
)
