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
            preferenceHelper.getThemeMode()
                .distinctUntilChanged()
                .collect { mode ->
                    _state.update {
                        it.copy(themeMode = mode)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getFontPreference()
                .distinctUntilChanged()
                .collect { font ->
                    _state.update {
                        it.copy(fontPreference = font)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeAppsAlignmentHorizontal()
                .distinctUntilChanged()
                .collect { alignment ->
                    _state.update {
                        it.copy(homeAppsAlignmentHorizontal = alignment)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeAppsAlignmentVertical()
                .distinctUntilChanged()
                .collect { alignment ->
                    _state.update {
                        it.copy(homeAppsAlignmentVertical = alignment)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeClockAlignment()
                .distinctUntilChanged()
                .collect { alignment ->
                    _state.update {
                        it.copy(homeClockAlignment = alignment)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getShowHomeClock()
                .distinctUntilChanged()
                .collect { show ->
                    _state.update {
                        it.copy(showHomeClock = show)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getShowStatusBar()
                .distinctUntilChanged()
                .collect { show ->
                    _state.update {
                        it.copy(showStatusBar = show)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeTextSizeFlow()
                .distinctUntilChanged()
                .collect { size ->
                    _state.update {
                        it.copy(homeTextSize = size.toFloat())
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getAutoOpenKeyboardAllApps()
                .distinctUntilChanged()
                .collect { open ->
                    _state.update {
                        it.copy(autoOpenKeyboardAllApps = open)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getDynamicTheme()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(dynamicTheme = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeClockMode()
                .distinctUntilChanged()
                .collect { mode ->
                    _state.update {
                        it.copy(homeClockMode = mode)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getDoubleTapToLock()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(doubleTapToLock = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getTwentyFourHourFormat()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(twentyFourHourFormat = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getShowBatteryLevel()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(showBatteryLevel = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getShowHiddenAppsInSearch()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(showHiddenAppsInSearch = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getDrawerSearchBarAtBottom()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(drawerSearchBarAtBottom = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeAppSizeToAllApps()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(applyHomeAppSizeToAllApps = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getBlackTheme()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(blackTheme = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getSetWallpaperToThemeColor()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(setWallpaperToThemeColor = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getEnableWallpaper()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(enableWallpaper = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getLightTextOnWallpaper()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(lightTextOnWallpaper = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getDimWallpaper()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(dimWallpaper = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getAutoOpenApp()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(autoOpenApp = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHideAppDrawerArrow()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(hideAppDrawerArrow = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getNotificationDot()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(notificationDot = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeAppVerticalPadding()
                .distinctUntilChanged()
                .collect { padding ->
                    _state.update {
                        it.copy(homeAppVerticalPadding = padding.toFloat())
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getIgnoreSpecialCharacters()
                .distinctUntilChanged()
                .collect { characters ->
                    _state.update {
                        it.copy(ignoreSpecialCharacters = characters)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHideAppDrawerSearch()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(hideAppDrawerSearch = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getShowScreenTimeWidget()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(showScreenTimeWidget = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getClockAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    val appName = getAppNameFromPref(pref)
                    if (pref.isNotBlank() && appName.isEmpty()) {
                        preferenceHelper.setClockAppPreference("")
                    } else {
                        _state.update {
                            it.copy(clockAppPreference = pref, clockAppName = appName)
                        }
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getCalendarAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    val appName = getAppNameFromPref(pref)
                    if (pref.isNotBlank() && appName.isEmpty()) {
                        preferenceHelper.setCalendarAppPreference("")
                    } else {
                        _state.update {
                            it.copy(calendarAppPreference = pref, calendarAppName = appName)
                        }
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getScreenTimeAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    val appName = getAppNameFromPref(pref)
                    if (pref.isNotBlank() && appName.isEmpty()) {
                        preferenceHelper.setScreenTimeAppPreference("")
                    } else {
                        _state.update {
                            it.copy(screenTimeAppPreference = pref, screenTimeAppName = appName)
                        }
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getSwipeLeftAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    val appName = getAppNameFromPref(pref)
                    if (pref.isNotBlank() && appName.isEmpty()) {
                        preferenceHelper.setSwipeLeftAppPreference("")
                    } else {
                        _state.update {
                            it.copy(swipeLeftAppPreference = pref, swipeLeftAppName = appName)
                        }
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getSwipeRightAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    val appName = getAppNameFromPref(pref)
                    if (pref.isNotBlank() && appName.isEmpty()) {
                        preferenceHelper.setSwipeRightAppPreference("")
                    } else {
                        _state.update {
                            it.copy(swipeRightAppPreference = pref, swipeRightAppName = appName)
                        }
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

    fun onToggleHideAppDrawerArrow() {
        viewModelScope.launch {
            preferenceHelper.hideAppDrawerArrow(_state.value.hideAppDrawerArrow.not())
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
}
