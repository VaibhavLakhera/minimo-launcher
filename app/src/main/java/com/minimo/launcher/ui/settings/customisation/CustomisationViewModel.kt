package com.minimo.launcher.ui.settings.customisation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.ui.entities.toAppPreferenceTarget
import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.AppIconAlignment
import com.minimo.launcher.utils.FastScrollerAlignment
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.ScreenOrientation
import com.minimo.launcher.utils.SearchMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
                    // Resolve custom app names and clear preferences for apps no longer in the DB.
                    val clockAppName = getAppNameFromPref(prefs.clockAppPreference)
                    if (prefs.clockAppPreference.isNotBlank() && clockAppName.isEmpty()) {
                        preferenceHelper.setClockAppPreference("")
                    }

                    val batteryAppName = getAppNameFromPref(prefs.batteryAppPreference)
                    if (prefs.batteryAppPreference.isNotBlank() && batteryAppName.isEmpty()) {
                        preferenceHelper.setBatteryAppPreference("")
                    }

                    val calendarAppName = getAppNameFromPref(prefs.calendarAppPreference)
                    if (prefs.calendarAppPreference.isNotBlank() && calendarAppName.isEmpty()) {
                        preferenceHelper.setCalendarAppPreference("")
                    }

                    val screenTimeAppName = getAppNameFromPref(prefs.screenTimeAppPreference)
                    if (prefs.screenTimeAppPreference.isNotBlank() && screenTimeAppName.isEmpty()) {
                        preferenceHelper.setScreenTimeAppPreference("")
                    }

                    val swipeLeftAppName = getAppNameFromPref(prefs.swipeLeftAppPreference)
                    if (prefs.swipeLeftAppPreference.isNotBlank() && swipeLeftAppName.isEmpty()) {
                        preferenceHelper.setSwipeLeftAppPreference("")
                    }

                    val swipeRightAppName = getAppNameFromPref(prefs.swipeRightAppPreference)
                    if (prefs.swipeRightAppPreference.isNotBlank() && swipeRightAppName.isEmpty()) {
                        preferenceHelper.setSwipeRightAppPreference("")
                    }

                    _state.update { state ->
                        state.copy(
                            initialLoaded = true,
                            themeMode = prefs.themeMode,
                            fontPreference = prefs.fontPreference,
                            screenOrientation = prefs.screenOrientation,
                            homeAppsAlignmentHorizontal = prefs.homeAppsAlignmentHorizontal,
                            drawerAppsAlignmentHorizontal = prefs.drawerAppsAlignmentHorizontal,
                            homeAppsAlignmentVertical = prefs.homeAppsAlignmentVertical,
                            homeClockAlignment = prefs.homeClockAlignment,
                            showHomeClock = prefs.showHomeClock,
                            showStatusBar = prefs.showStatusBar,
                            showNavigationBar = prefs.showNavigationBar,
                            homeTextSize = prefs.homeTextSize.toFloat(),
                            autoOpenKeyboardAllApps = prefs.autoOpenKeyboardAllApps,
                            dynamicTheme = prefs.dynamicTheme,
                            homeClockMode = prefs.homeClockMode,
                            doubleTapToLock = prefs.doubleTapToLock,
                            twentyFourHourFormat = prefs.twentyFourHourFormat,
                            showBatteryLevel = prefs.showBatteryLevel,
                            showHiddenAppsInSearch = prefs.showHiddenAppsInSearch,
                            drawerSearchBarAtBottom = prefs.drawerSearchBarAtBottom,
                            showAppIconInHome = prefs.showAppIconInHome,
                            showAppIconInDrawer = prefs.showAppIconInDrawer,
                            homeAppIconAlignment = prefs.homeAppIconAlignment,
                            drawerAppIconAlignment = prefs.drawerAppIconAlignment,
                            appIconSizePercent = prefs.appIconSizePercent.toFloat(),
                            applyHomeAppSizeToAllApps = prefs.applyHomeAppSizeToAllApps,
                            blackTheme = prefs.blackTheme,
                            setWallpaperToThemeColor = prefs.setWallpaperToThemeColor,
                            enableWallpaper = prefs.enableWallpaper,
                            enableWallpaperOnDrawer = prefs.enableWallpaperOnDrawer,
                            lightTextOnWallpaper = prefs.lightTextOnWallpaper,
                            dimWallpaper = prefs.dimWallpaper,
                            dimWallpaperPercentage = prefs.dimWallpaperPercentage.toFloat(),
                            autoOpenApp = prefs.autoOpenApp,
                            notificationDot = prefs.notificationDot,
                            homeAppVerticalPadding = prefs.homeAppVerticalPadding.toFloat(),
                            ignoreSpecialCharacters = prefs.ignoreSpecialCharacters,
                            searchMode = prefs.searchMode,
                            searchBarBackground = prefs.searchBarBackground,
                            searchBarBorderPercent = prefs.searchBarBorderPercent.toFloat(),
                            hideAppDrawerSearch = prefs.hideAppDrawerSearch,
                            hideSettingsIcon = prefs.hideSettingsIcon,
                            minimoSettingsPosition = prefs.minimoSettingsPosition,
                            showScreenTimeWidget = prefs.showScreenTimeWidget,
                            clockAppPreference = if (clockAppName.isEmpty()) "" else prefs.clockAppPreference,
                            clockAppName = clockAppName,
                            batteryAppPreference = if (batteryAppName.isEmpty()) "" else prefs.batteryAppPreference,
                            batteryAppName = batteryAppName,
                            calendarAppPreference = if (calendarAppName.isEmpty()) "" else prefs.calendarAppPreference,
                            calendarAppName = calendarAppName,
                            screenTimeAppPreference = if (screenTimeAppName.isEmpty()) "" else prefs.screenTimeAppPreference,
                            screenTimeAppName = screenTimeAppName,
                            swipeLeftAppPreference = if (swipeLeftAppName.isEmpty()) "" else prefs.swipeLeftAppPreference,
                            swipeLeftAppName = swipeLeftAppName,
                            swipeRightAppPreference = if (swipeRightAppName.isEmpty()) "" else prefs.swipeRightAppPreference,
                            swipeRightAppName = swipeRightAppName,
                            keyboardOpenDelay = prefs.keyboardOpenDelay,
                            enableFastScroller = prefs.enableFastScroller,
                            fastScrollerAlignment = prefs.fastScrollerAlignment,
                            backOpensAppDrawer = prefs.backOpensAppDrawer,
                            compactAppTouchArea = prefs.compactAppTouchArea,
                            keyboardDoneOpensFirstApp = prefs.keyboardDoneOpensFirstApp
                        )
                    }
                }
        }
    }

    private suspend fun getAppNameFromPref(pref: String): String {
        if (pref.isBlank()) return ""
        val target = pref.toAppPreferenceTarget() ?: return ""
        val entity = appInfoDao.getApp(
            target.itemType,
            target.targetId,
            target.packageName,
            target.userHandle
        )
        if (entity != null) {
            return entity.alternateAppName.ifEmpty { entity.appName }
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

    fun onScreenOrientationChanged(orientation: ScreenOrientation) {
        viewModelScope.launch {
            preferenceHelper.setScreenOrientation(orientation)
        }
    }

    fun onHomeAppsAlignmentHorizontalChanged(alignment: HomeAppsAlignmentHorizontal) {
        viewModelScope.launch {
            preferenceHelper.setHomeAppsAlignmentHorizontal(alignment)
        }
    }

    fun onDrawerAppsAlignmentHorizontalChanged(alignment: HomeAppsAlignmentHorizontal) {
        viewModelScope.launch {
            preferenceHelper.setDrawerAppsAlignmentHorizontal(alignment)
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

    fun onToggleShowNavigationBar() {
        viewModelScope.launch {
            preferenceHelper.setShowNavigationBar(_state.value.showNavigationBar.not())
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

    fun onToggleEnableWallpaperOnDrawer() {
        viewModelScope.launch {
            preferenceHelper.setEnableWallpaperOnDrawer(
                _state.value.enableWallpaperOnDrawer.not()
            )
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

    fun onDimWallpaperPercentageChanged(percentage: Int) {
        viewModelScope.launch {
            preferenceHelper.setDimWallpaperPercentage(percentage)
        }
    }

    fun onDoubleTapToLockChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferenceHelper.setDoubleTapToLock(enabled)
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

    fun onToggleShowAppIconInHome() {
        viewModelScope.launch {
            preferenceHelper.setShowAppIconInHome(_state.value.showAppIconInHome.not())
        }
    }

    fun onToggleShowAppIconInDrawer() {
        viewModelScope.launch {
            preferenceHelper.setShowAppIconInDrawer(_state.value.showAppIconInDrawer.not())
        }
    }

    fun onHomeAppIconAlignmentChanged(alignment: AppIconAlignment) {
        viewModelScope.launch {
            preferenceHelper.setHomeAppIconAlignment(alignment)
        }
    }

    fun onDrawerAppIconAlignmentChanged(alignment: AppIconAlignment) {
        viewModelScope.launch {
            preferenceHelper.setDrawerAppIconAlignment(alignment)
        }
    }

    fun onSearchModeChanged(mode: SearchMode) {
        viewModelScope.launch {
            preferenceHelper.setSearchMode(mode)
        }
    }

    fun onToggleSearchBarBackground() {
        viewModelScope.launch {
            preferenceHelper.setSearchBarBackground(_state.value.searchBarBackground.not())
        }
    }

    fun onSearchBarBorderPercentChanged(percent: Int) {
        viewModelScope.launch {
            preferenceHelper.setSearchBarBorderPercent(percent)
        }
    }

    fun onAppIconSizePercentChanged(percent: Int) {
        viewModelScope.launch {
            preferenceHelper.setAppIconSizePercent(percent)
        }
    }

    fun onToggleApplyHomeAppSizeToAllApps() {
        viewModelScope.launch {
            preferenceHelper.setHomeAppSizeToAllApps(_state.value.applyHomeAppSizeToAllApps.not())
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
            preferenceHelper.setIgnoreSpecialCharacters(characters)
        }
    }

    fun onToggleHideAppDrawerSearch() {
        viewModelScope.launch {
            preferenceHelper.hideAppDrawerSearch(_state.value.hideAppDrawerSearch.not())
        }
    }

    fun onToggleHideSettingsIcon() {
        viewModelScope.launch {
            preferenceHelper.setHideSettingsIcon(_state.value.hideSettingsIcon.not())
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

    fun onBatteryAppChanged(appData: String) {
        viewModelScope.launch {
            preferenceHelper.setBatteryAppPreference(appData)
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

    fun onToggleFastScroller() {
        viewModelScope.launch {
            preferenceHelper.setEnableFastScroller(_state.value.enableFastScroller.not())
        }
    }

    fun onFastScrollerAlignmentChanged(alignment: FastScrollerAlignment) {
        viewModelScope.launch {
            preferenceHelper.setFastScrollerAlignment(alignment)
        }
    }

    fun onToggleBackOpensAppDrawer() {
        viewModelScope.launch {
            preferenceHelper.setBackOpensAppDrawer(_state.value.backOpensAppDrawer.not())
        }
    }

    fun onToggleCompactAppTouchArea() {
        viewModelScope.launch {
            preferenceHelper.setCompactAppTouchArea(_state.value.compactAppTouchArea.not())
        }
    }

    fun onToggleKeyboardDoneOpensFirstApp() {
        viewModelScope.launch {
            preferenceHelper.setKeyboardDoneOpensFirstApp(
                _state.value.keyboardDoneOpensFirstApp.not()
            )
        }
    }
}
