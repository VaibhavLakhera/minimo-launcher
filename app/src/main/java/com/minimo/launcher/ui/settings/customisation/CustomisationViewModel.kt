package com.minimo.launcher.ui.settings.customisation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomisationViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper,
    private val appInfoDao: AppInfoDao
) : ViewModel() {
    private val _state = MutableStateFlow(CustomisationState())
    val state: StateFlow<CustomisationState> = _state

    init {
        viewModelScope.launch {
            preferenceHelper.getCustomisationPreferencesFlow()
                .distinctUntilChanged()
                .collect { prefs ->
                    _state.update { state ->
                        state.copy(
                            themeMode = prefs.themeMode,
                            fontPreference = prefs.fontPreference,
                            homeAppsAlignmentHorizontal = prefs.homeAppsAlignmentHorizontal,
                            homeAppsAlignmentVertical = prefs.homeAppsAlignmentVertical,
                            homeClockAlignment = prefs.homeClockAlignment,
                            showHomeClock = prefs.showHomeClock,
                            showStatusBar = prefs.showStatusBar,
                            homeTextSize = prefs.homeTextSize.toFloat(),
                            autoOpenKeyboardAllApps = prefs.autoOpenKeyboardAllApps,
                            dynamicTheme = prefs.dynamicTheme,
                            homeClockMode = prefs.homeClockMode,
                            doubleTapToLock = prefs.doubleTapToLock,
                            twentyFourHourFormat = prefs.twentyFourHourFormat,
                            showBatteryLevel = prefs.showBatteryLevel,
                            showHiddenAppsInSearch = prefs.showHiddenAppsInSearch,
                            drawerSearchBarAtBottom = prefs.drawerSearchBarAtBottom,
                            applyHomeAppSizeToAllApps = prefs.applyHomeAppSizeToAllApps,
                            blackTheme = prefs.blackTheme,
                            setWallpaperToThemeColor = prefs.setWallpaperToThemeColor,
                            enableWallpaper = prefs.enableWallpaper,
                            lightTextOnWallpaper = prefs.lightTextOnWallpaper,
                            dimWallpaper = prefs.dimWallpaper,
                            autoOpenApp = prefs.autoOpenApp,
                            notificationDot = prefs.notificationDot,
                            homeAppVerticalPadding = prefs.homeAppVerticalPadding.toFloat(),
                            ignoreSpecialCharacters = prefs.ignoreSpecialCharacters,
                            hideAppDrawerSearch = prefs.hideAppDrawerSearch,
                            minimoSettingsPosition = prefs.minimoSettingsPosition,
                            showScreenTimeWidget = prefs.showScreenTimeWidget,
                            clockAppPreference = prefs.clockAppPreference,
                            calendarAppPreference = prefs.calendarAppPreference,
                            screenTimeAppPreference = prefs.screenTimeAppPreference,
                            swipeLeftAppPreference = prefs.swipeLeftAppPreference,
                            swipeRightAppPreference = prefs.swipeRightAppPreference,
                            keyboardOpenDelay = prefs.keyboardOpenDelay
                        )
                    }

                    // Special checks for custom apps.
                    // If preference value exist, we app doesn't exist in DB then clear the preference value
                    val clockAppName = getAppNameFromPref(prefs.clockAppPreference)
                    if (prefs.clockAppPreference.isNotBlank() && clockAppName.isEmpty()) {
                        preferenceHelper.setClockAppPreference("")
                    } else {
                        _state.update { it.copy(clockAppName = clockAppName) }
                    }

                    val calendarAppName = getAppNameFromPref(prefs.calendarAppPreference)
                    if (prefs.calendarAppPreference.isNotBlank() && calendarAppName.isEmpty()) {
                        preferenceHelper.setCalendarAppPreference("")
                    } else {
                        _state.update { it.copy(calendarAppName = calendarAppName) }
                    }

                    val screenTimeAppName = getAppNameFromPref(prefs.screenTimeAppPreference)
                    if (prefs.screenTimeAppPreference.isNotBlank() && screenTimeAppName.isEmpty()) {
                        preferenceHelper.setScreenTimeAppPreference("")
                    } else {
                        _state.update { it.copy(screenTimeAppName = screenTimeAppName) }
                    }

                    val swipeLeftAppName = getAppNameFromPref(prefs.swipeLeftAppPreference)
                    if (prefs.swipeLeftAppPreference.isNotBlank() && swipeLeftAppName.isEmpty()) {
                        preferenceHelper.setSwipeLeftAppPreference("")
                    } else {
                        _state.update { it.copy(swipeLeftAppName = swipeLeftAppName) }
                    }

                    val swipeRightAppName = getAppNameFromPref(prefs.swipeRightAppPreference)
                    if (prefs.swipeRightAppPreference.isNotBlank() && swipeRightAppName.isEmpty()) {
                        preferenceHelper.setSwipeRightAppPreference("")
                    } else {
                        _state.update { it.copy(swipeRightAppName = swipeRightAppName) }
                    }
                }
        }
    }

    private suspend fun getAppNameFromPref(pref: String): String {
        if (pref.isBlank()) return ""
        val parts = pref.split("|")
        if (parts.size == 3) {
            val packageName = parts[0]
            val className = parts[1]
            val userHandle = parts[2].toIntOrNull() ?: return ""
            val entity = appInfoDao.getApp(className, packageName, userHandle)
            if (entity != null) {
                return entity.alternateAppName.ifEmpty { entity.appName }
            }
        }
        return ""
    }

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch {
            preferenceHelper.setThemeMode(mode)
        }
    }

    fun onFontPreferenceChanged(font: String) {
        viewModelScope.launch {
            preferenceHelper.setFontPreference(font)
        }
    }

    fun onHomeAppsAlignmentHorizontalChanged(alignment: HomeAppsAlignmentHorizontal) {
        viewModelScope.launch {
            preferenceHelper.setHomeAppsAlignmentHorizontal(alignment)
        }
    }

    fun onHomeAppsAlignmentVerticalChanged(alignment: HomeAppsAlignmentVertical) {
        viewModelScope.launch {
            preferenceHelper.setHomeAppsAlignmentVertical(alignment)
        }
    }

    fun onHomeClockAlignmentChanged(alignment: HomeClockAlignment) {
        viewModelScope.launch {
            preferenceHelper.setHomeClockAlignment(alignment)
        }
    }

    fun onHomeClockModeChanged(mode: HomeClockMode) {
        viewModelScope.launch {
            preferenceHelper.setHomeClockMode(mode)
        }
    }

    fun onToggleShowHomeClock() {
        viewModelScope.launch {
            preferenceHelper.setShowHomeClock(_state.value.showHomeClock.not())
        }
    }

    fun onToggleTwentyFourHourFormat() {
        viewModelScope.launch {
            preferenceHelper.setTwentyFourHourFormat(_state.value.twentyFourHourFormat.not())
        }
    }

    fun onToggleShowBatteryLevel() {
        viewModelScope.launch {
            preferenceHelper.setShowBatteryLevel(_state.value.showBatteryLevel.not())
        }
    }

    fun onToggleShowStatusBar() {
        viewModelScope.launch {
            preferenceHelper.setShowStatusBar(_state.value.showStatusBar.not())
        }
    }

    fun onHomeTextSizeChanged(size: Int) {
        viewModelScope.launch {
            preferenceHelper.setHomeTextSize(size)
        }
    }

    fun onToggleAutoOpenKeyboardAllApps() {
        viewModelScope.launch {
            preferenceHelper.setAutoOpenKeyboardAllApps(_state.value.autoOpenKeyboardAllApps.not())
        }
    }

    fun onToggleDynamicTheme() {
        viewModelScope.launch {
            preferenceHelper.setDynamicTheme(_state.value.dynamicTheme.not())
        }
    }

    fun onToggleBlackTheme() {
        viewModelScope.launch {
            preferenceHelper.setBlackTheme(_state.value.blackTheme.not())
        }
    }

    fun onToggleSetWallpaperToThemeColor() {
        viewModelScope.launch {
            preferenceHelper.setSetWallpaperToThemeColor(_state.value.setWallpaperToThemeColor.not())
        }
    }

    fun onToggleEnableWallpaper() {
        viewModelScope.launch {
            preferenceHelper.setEnableWallpaper(_state.value.enableWallpaper.not())
        }
    }

    fun onToggleLightTextOnWallpaper() {
        viewModelScope.launch {
            preferenceHelper.setLightTextOnWallpaper(_state.value.lightTextOnWallpaper.not())
        }
    }

    fun onToggleDimWallpaper() {
        viewModelScope.launch {
            preferenceHelper.setDimWallpaper(_state.value.dimWallpaper.not())
        }
    }

    fun onToggleDoubleTapToLock() {
        viewModelScope.launch {
            preferenceHelper.setDoubleTapToLock(_state.value.doubleTapToLock.not())
        }
    }

    fun onToggleShowHiddenAppsInSearch() {
        viewModelScope.launch {
            preferenceHelper.setShowHiddenAppsInSearch(_state.value.showHiddenAppsInSearch.not())
        }
    }

    fun onToggleDrawerSearchBarAtBottom() {
        viewModelScope.launch {
            preferenceHelper.setDrawerSearchBarAtBottom(_state.value.drawerSearchBarAtBottom.not())
        }
    }

    fun onToggleApplyHomeAppSizeToAllApps() {
        viewModelScope.launch {
            preferenceHelper.setHomeAppSizeToAllApps(_state.value.applyHomeAppSizeToAllApps.not())
        }
    }

    /*
    * On start of the screen, if the preference flag is enabled and
    * lock screen permission is not active, then set the preference flag to false
    * */
    fun onLockScreenPermissionNotEnableOnStarted() {
        viewModelScope.launch {
            val doubleTapToLock = preferenceHelper.getDoubleTapToLock().firstOrNull() ?: false
            if (doubleTapToLock) {
                preferenceHelper.setDoubleTapToLock(false)
            }
        }
    }

    fun onToggleAutoOpenApp() {
        viewModelScope.launch {
            preferenceHelper.setAutoOpenApp(_state.value.autoOpenApp.not())
        }
    }

    fun onToggleNotificationDot() {
        viewModelScope.launch {
            preferenceHelper.setNotificationDot(_state.value.notificationDot.not())
        }
    }

    fun onNotificationPermissionNotGrantedOnStarted() {
        viewModelScope.launch {
            preferenceHelper.setNotificationDot(false)
        }
    }

    fun onHomeVerticalPaddingChanged(padding: Int) {
        viewModelScope.launch {
            preferenceHelper.setHomeAppVerticalPadding(padding)
        }
    }

    fun onUpdateIgnoreSpecialCharacters(characters: String) {
        viewModelScope.launch {
            val uniqueCharacters = characters.trim().toSet().joinToString("")
            preferenceHelper.setIgnoreSpecialCharacters(uniqueCharacters)
        }
    }

    fun onToggleHideAppDrawerSearch() {
        viewModelScope.launch {
            preferenceHelper.hideAppDrawerSearch(_state.value.hideAppDrawerSearch.not())
        }
    }

    fun onToggleShowScreenTimeWidget() {
        viewModelScope.launch {
            preferenceHelper.showScreenTimeWidget(_state.value.showScreenTimeWidget.not())
        }
    }

    fun onAppUsagePermissionNotGrantedOnStarted() {
        viewModelScope.launch {
            preferenceHelper.showScreenTimeWidget(false)
        }
    }

    fun onClockAppChanged(appData: String) {
        viewModelScope.launch {
            preferenceHelper.setClockAppPreference(appData)
        }
    }

    fun onCalendarAppChanged(appData: String) {
        viewModelScope.launch {
            preferenceHelper.setCalendarAppPreference(appData)
        }
    }

    fun onScreenTimeAppChanged(appData: String) {
        viewModelScope.launch {
            preferenceHelper.setScreenTimeAppPreference(appData)
        }
    }

    fun onSwipeLeftAppChanged(appData: String) {
        viewModelScope.launch {
            preferenceHelper.setSwipeLeftAppPreference(appData)
        }
    }

    fun onSwipeRightAppChanged(appData: String) {
        viewModelScope.launch {
            preferenceHelper.setSwipeRightAppPreference(appData)
        }
    }

    fun onMinimoSettingsPositionChanged(position: com.minimo.launcher.utils.MinimoSettingsPosition) {
        viewModelScope.launch {
            preferenceHelper.setMinimoSettingsPosition(position)
        }
    }

    fun onKeyboardOpenDelayChanged(delay: Long) {
        viewModelScope.launch {
            preferenceHelper.setKeyboardOpenDelay(delay)
        }
    }
}
