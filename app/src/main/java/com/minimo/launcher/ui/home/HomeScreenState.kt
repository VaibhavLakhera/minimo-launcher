package com.minimo.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.utils.AppIconAlignment
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.FastScrollerAlignment
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.MinimoSettingsPosition
import com.minimo.launcher.utils.SearchMode

data class PendingAppLaunch(
    val app: AppInfo,
    val deadlineElapsedRealtimeMillis: Long
)

data class HomeScreenState(
    val whatsNewDescription: String? = null,
    val initialLoaded: Boolean = false,
    val favouriteApps: List<AppInfo> = emptyList(),
    val allApps: List<AppInfo> = emptyList(),
    val filteredAllApps: List<AppInfo> = emptyList(),
    val renameAppDialog: AppInfo? = null,
    val launchDelayDialog: AppInfo? = null,
    val launchConfirmDialog: PendingAppLaunch? = null,
    val searchText: String = "",
    val appsArrangementHorizontal: Arrangement.Horizontal = Arrangement.Start,
    val drawerAppsArrangementHorizontal: Arrangement.Horizontal = Arrangement.Start,
    val appsArrangementVertical: Arrangement.Vertical = Arrangement.Center,
    val showHomeClock: Boolean = false,
    val homeClockAlignment: Alignment.Horizontal = Alignment.Start,
    val homeTextSize: Int = Constants.DEFAULT_HOME_TEXT_SIZE,
    val autoOpenKeyboardAllApps: Boolean = false,
    val homeClockMode: HomeClockMode = HomeClockMode.Full,
    val doubleTapToLock: Boolean = false,
    val twentyFourHourFormat: Boolean = false,
    val showBatteryLevel: Boolean = false,
    val showHiddenAppsInSearch: Boolean = true,
    val drawerSearchBarAtBottom: Boolean = false,
    val showAppIconInHome: Boolean = false,
    val showAppIconInDrawer: Boolean = false,
    val homeAppIconAlignment: AppIconAlignment = AppIconAlignment.Left,
    val drawerAppIconAlignment: AppIconAlignment = AppIconAlignment.Left,
    val appIconSizePercent: Int = Constants.DEFAULT_APP_ICON_SIZE_PERCENT,
    val applyHomeAppSizeToAllApps: Boolean = false,
    val autoOpenApp: Boolean = false,
    val homeAppVerticalPadding: Int = Constants.DEFAULT_HOME_VERTICAL_PADDING,
    val ignoreSpecialCharacters: String = "",
    val searchMode: SearchMode = SearchMode.Contains,
    val searchBarBackground: Boolean = false,
    val searchPreferencesLoaded: Boolean = false,
    val searchBarBorderPercent: Int = Constants.DEFAULT_SEARCH_BAR_BORDER_PERCENT,
    val hideAppDrawerSearch: Boolean = false,
    val hideSettingsIcon: Boolean = false,
    val showScreenTimeWidget: Boolean = false,
    val screenTime: String = "",
    val enableWallpaper: Boolean = false,
    val enableWallpaperOnDrawer: Boolean = false,
    val lightTextOnWallpaper: Boolean = true,
    val clockAppPreference: String = "",
    val batteryAppPreference: String = "",
    val calendarAppPreference: String = "",
    val screenTimeAppPreference: String = "",
    val swipeLeftAppPreference: String = "",
    val swipeRightAppPreference: String = "",
    val minimoSettingsPosition: MinimoSettingsPosition = MinimoSettingsPosition.Auto,
    val keyboardOpenDelay: Long = Constants.DEFAULT_KEYBOARD_OPEN_DELAY,
    val enableFastScroller: Boolean = false,
    val fastScrollerAlignment: FastScrollerAlignment = FastScrollerAlignment.Right,
    val backOpensAppDrawer: Boolean = true,
    val compactAppTouchArea: Boolean = false,
    val keyboardDoneOpensFirstApp: Boolean = false
)
