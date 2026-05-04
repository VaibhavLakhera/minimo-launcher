package com.minimo.launcher.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.MinimoSettingsPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceHelper @Inject constructor(
    private val preferences: DataStore<Preferences>
) {
    companion object {
        private val KEY_INTRO_COMPLETED = booleanPreferencesKey("KEY_INTRO_COMPLETED")
        private val KEY_THEME_MODE = stringPreferencesKey("KEY_THEME_MODE")
        private val KEY_SET_WALLPAPER_TO_THEME_COLOR =
            booleanPreferencesKey("KEY_SET_WALLPAPER_TO_THEME_COLOR")
        private val KEY_ENABLE_WALLPAPER = booleanPreferencesKey("KEY_ENABLE_WALLPAPER")
        private val KEY_LIGHT_TEXT_ON_WALLPAPER =
            booleanPreferencesKey("KEY_LIGHT_TEXT_ON_WALLPAPER")
        private val KEY_DIM_WALLPAPER = booleanPreferencesKey("KEY_DIM_WALLPAPER")
        private val KEY_HOME_APPS_ALIGN_HORIZONTAL = stringPreferencesKey("KEY_HOME_APPS_ALIGN")
        private val KEY_HOME_APPS_ALIGN_VERTICAL =
            stringPreferencesKey("KEY_HOME_APPS_ALIGN_VERTICAL")
        private val KEY_HOME_CLOCK_ALIGNMENT = stringPreferencesKey("KEY_HOME_CLOCK_ALIGNMENT")
        private val KEY_HOME_CLOCK_MODE = stringPreferencesKey("KEY_HOME_CLOCK_MODE")
        private val KEY_SHOW_HOME_CLOCK = booleanPreferencesKey("KEY_SHOW_HOME_CLOCK")
        private val KEY_SHOW_STATUS_BAR = booleanPreferencesKey("KEY_SHOW_STATUS_BAR")
        private val KEY_HOME_TEXT_SIZE = intPreferencesKey("KEY_HOME_TEXT_SIZE")
        private val KEY_AUTO_OPEN_KEYBOARD_ALL_APPS =
            booleanPreferencesKey("KEY_AUTO_OPEN_KEYBOARD_ALL_APPS")
        private val KEY_DYNAMIC_THEME = booleanPreferencesKey("KEY_DYNAMIC_THEME")
        private val KEY_DOUBLE_TAP_TO_LOCK = booleanPreferencesKey("KEY_DOUBLE_TAP_TO_LOCK")
        private val KEY_TWENTY_FOUR_HOUR_FORMAT =
            booleanPreferencesKey("KEY_TWENTY_FOUR_HOUR_FORMAT")
        private val KEY_SHOW_BATTERY_LEVEL = booleanPreferencesKey("KEY_SHOW_BATTERY_LEVEL")
        private val KEY_SHOW_HIDDEN_APPS_IN_SEARCH =
            booleanPreferencesKey("KEY_SHOW_HIDDEN_APPS_IN_SEARCH")
        private val KEY_DRAWER_SEARCH_BAR_AT_BOTTOM =
            booleanPreferencesKey("KEY_DRAWER_SEARCH_BAR_AT_BOTTOM")
        private val KEY_APPLY_HOME_APP_SIZE_TO_ALL_APPS =
            booleanPreferencesKey("KEY_APPLY_HOME_APP_SIZE_TO_ALL_APPS")
        private val KEY_BLACK_THEME = booleanPreferencesKey("KEY_BLACK_THEME")
        private val KEY_AUTO_OPEN_APP = booleanPreferencesKey("KEY_AUTO_OPEN_APP")
        private val KEY_NOTIFICATION_DOT = booleanPreferencesKey("KEY_NOTIFICATION_DOT")
        private val KEY_HOME_APP_VERTICAL_PADDING =
            intPreferencesKey("KEY_HOME_APP_VERTICAL_PADDING")
        private val KEY_IGNORE_SPECIAL_CHARACTERS_IN_SEARCH =
            stringPreferencesKey("KEY_IGNORE_SPECIAL_CHARACTERS_IN_SEARCH")
        private val KEY_HIDE_APP_DRAWER_SEARCH = booleanPreferencesKey("KEY_HIDE_APP_DRAWER_SEARCH")
        private val KEY_SHOW_SCREEN_TIME_WIDGET =
            booleanPreferencesKey("KEY_SHOW_SCREEN_TIME_WIDGET")
        private val KEY_CLOCK_APP_PREFERENCE = stringPreferencesKey("KEY_CLOCK_APP_PREFERENCE")
        private val KEY_CALENDAR_APP_PREFERENCE =
            stringPreferencesKey("KEY_CALENDAR_APP_PREFERENCE")
        private val KEY_SCREEN_TIME_APP_PREFERENCE =
            stringPreferencesKey("KEY_SCREEN_TIME_APP_PREFERENCE")
        private val KEY_SWIPE_LEFT_APP_PREFERENCE =
            stringPreferencesKey("KEY_SWIPE_LEFT_APP_PREFERENCE")
        private val KEY_SWIPE_RIGHT_APP_PREFERENCE =
            stringPreferencesKey("KEY_SWIPE_RIGHT_APP_PREFERENCE")
        private val KEY_FONT_PREFERENCE = stringPreferencesKey("KEY_FONT_PREFERENCE")
        private val KEY_MINIMO_SETTINGS_POSITION =
            stringPreferencesKey("KEY_MINIMO_SETTINGS_POSITION")
        private val KEY_KEYBOARD_OPEN_DELAY = longPreferencesKey("KEY_KEYBOARD_OPEN_DELAY")
    }

    suspend fun setIsIntroCompleted(isCompleted: Boolean) {
        preferences.edit {
            it[KEY_INTRO_COMPLETED] = isCompleted
        }
    }

    fun getIsIntroCompletedFlow(): Flow<Boolean> {
        return preferences.data.map { it[KEY_INTRO_COMPLETED] ?: false }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        preferences.edit {
            it[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setHomeAppsAlignmentHorizontal(alignment: HomeAppsAlignmentHorizontal) {
        preferences.edit {
            it[KEY_HOME_APPS_ALIGN_HORIZONTAL] = alignment.name
        }
    }

    suspend fun setHomeAppsAlignmentVertical(alignment: HomeAppsAlignmentVertical) {
        preferences.edit {
            it[KEY_HOME_APPS_ALIGN_VERTICAL] = alignment.name
        }
    }

    suspend fun setHomeClockAlignment(alignment: HomeClockAlignment) {
        preferences.edit {
            it[KEY_HOME_CLOCK_ALIGNMENT] = alignment.name
        }
    }

    suspend fun setShowHomeClock(show: Boolean) {
        preferences.edit {
            it[KEY_SHOW_HOME_CLOCK] = show
        }
    }

    suspend fun setShowStatusBar(show: Boolean) {
        preferences.edit {
            it[KEY_SHOW_STATUS_BAR] = show
        }
    }

    suspend fun setHomeTextSize(size: Int) {
        preferences.edit { preferences ->
            preferences[KEY_HOME_TEXT_SIZE] = size
        }
    }

    suspend fun setAutoOpenKeyboardAllApps(open: Boolean) {
        preferences.edit {
            it[KEY_AUTO_OPEN_KEYBOARD_ALL_APPS] = open
        }
    }

    suspend fun setDynamicTheme(enable: Boolean) {
        preferences.edit {
            it[KEY_DYNAMIC_THEME] = enable
        }
    }

    suspend fun setHomeClockMode(mode: HomeClockMode) {
        preferences.edit {
            it[KEY_HOME_CLOCK_MODE] = mode.name
        }
    }

    suspend fun setDoubleTapToLock(enable: Boolean) {
        preferences.edit {
            it[KEY_DOUBLE_TAP_TO_LOCK] = enable
        }
    }

    fun getDoubleTapToLock(): Flow<Boolean> {
        return preferences.data.map { it[KEY_DOUBLE_TAP_TO_LOCK] ?: false }
    }

    suspend fun setTwentyFourHourFormat(enable: Boolean) {
        preferences.edit {
            it[KEY_TWENTY_FOUR_HOUR_FORMAT] = enable
        }
    }

    suspend fun setShowBatteryLevel(enable: Boolean) {
        preferences.edit {
            it[KEY_SHOW_BATTERY_LEVEL] = enable
        }
    }

    suspend fun setShowHiddenAppsInSearch(enable: Boolean) {
        preferences.edit {
            it[KEY_SHOW_HIDDEN_APPS_IN_SEARCH] = enable
        }
    }

    suspend fun setDrawerSearchBarAtBottom(enable: Boolean) {
        preferences.edit {
            it[KEY_DRAWER_SEARCH_BAR_AT_BOTTOM] = enable
        }
    }

    suspend fun setHomeAppSizeToAllApps(enable: Boolean) {
        preferences.edit {
            it[KEY_APPLY_HOME_APP_SIZE_TO_ALL_APPS] = enable
        }
    }

    suspend fun setBlackTheme(enable: Boolean) {
        preferences.edit {
            it[KEY_BLACK_THEME] = enable
        }
    }

    suspend fun setSetWallpaperToThemeColor(enable: Boolean) {
        preferences.edit {
            it[KEY_SET_WALLPAPER_TO_THEME_COLOR] = enable
        }
    }

    suspend fun setEnableWallpaper(enable: Boolean) {
        preferences.edit {
            it[KEY_ENABLE_WALLPAPER] = enable
        }
    }

    suspend fun setLightTextOnWallpaper(enable: Boolean) {
        preferences.edit {
            it[KEY_LIGHT_TEXT_ON_WALLPAPER] = enable
        }
    }

    suspend fun setDimWallpaper(enable: Boolean) {
        preferences.edit {
            it[KEY_DIM_WALLPAPER] = enable
        }
    }

    suspend fun setAutoOpenApp(enable: Boolean) {
        preferences.edit {
            it[KEY_AUTO_OPEN_APP] = enable
        }
    }

    suspend fun setNotificationDot(enable: Boolean) {
        preferences.edit {
            it[KEY_NOTIFICATION_DOT] = enable
        }
    }

    fun getNotificationDot(): Flow<Boolean> {
        return preferences.data.map { it[KEY_NOTIFICATION_DOT] ?: false }
    }

    suspend fun setHomeAppVerticalPadding(padding: Int) {
        preferences.edit {
            it[KEY_HOME_APP_VERTICAL_PADDING] = padding
        }
    }

    suspend fun setIgnoreSpecialCharacters(characters: String) {
        preferences.edit {
            it[KEY_IGNORE_SPECIAL_CHARACTERS_IN_SEARCH] = characters
        }
    }

    suspend fun hideAppDrawerSearch(enable: Boolean) {
        preferences.edit {
            it[KEY_HIDE_APP_DRAWER_SEARCH] = enable
        }
    }

    suspend fun showScreenTimeWidget(enable: Boolean) {
        preferences.edit {
            it[KEY_SHOW_SCREEN_TIME_WIDGET] = enable
        }
    }

    suspend fun setClockAppPreference(appData: String) {
        preferences.edit {
            it[KEY_CLOCK_APP_PREFERENCE] = appData
        }
    }

    suspend fun setCalendarAppPreference(appData: String) {
        preferences.edit {
            it[KEY_CALENDAR_APP_PREFERENCE] = appData
        }
    }

    suspend fun setScreenTimeAppPreference(appData: String) {
        preferences.edit {
            it[KEY_SCREEN_TIME_APP_PREFERENCE] = appData
        }
    }

    suspend fun setSwipeLeftAppPreference(appData: String) {
        preferences.edit {
            it[KEY_SWIPE_LEFT_APP_PREFERENCE] = appData
        }
    }

    suspend fun setSwipeRightAppPreference(appData: String) {
        preferences.edit {
            it[KEY_SWIPE_RIGHT_APP_PREFERENCE] = appData
        }
    }

    suspend fun setFontPreference(font: String) {
        preferences.edit {
            it[KEY_FONT_PREFERENCE] = font
        }
    }

    suspend fun setKeyboardOpenDelay(delay: Long) {
        preferences.edit {
            it[KEY_KEYBOARD_OPEN_DELAY] = delay
        }
    }

    suspend fun setMinimoSettingsPosition(position: MinimoSettingsPosition) {
        preferences.edit {
            it[KEY_MINIMO_SETTINGS_POSITION] = position.name
        }
    }

    fun getMainPreferencesFlow(): Flow<MainPreferences> {
        return preferences.data.map { prefs ->
            MainPreferences(
                themeMode = getThemeModeFromPref(prefs[KEY_THEME_MODE]),
                fontPreference = prefs[KEY_FONT_PREFERENCE] ?: "",
                showStatusBar = prefs[KEY_SHOW_STATUS_BAR] ?: true,
                dynamicTheme = prefs[KEY_DYNAMIC_THEME] ?: false,
                blackTheme = getBlackThemeFromPref(prefs[KEY_BLACK_THEME], prefs[KEY_THEME_MODE]),
                setWallpaperToThemeColor = prefs[KEY_SET_WALLPAPER_TO_THEME_COLOR] ?: false,
                enableWallpaper = prefs[KEY_ENABLE_WALLPAPER] ?: false,
                lightTextOnWallpaper = prefs[KEY_LIGHT_TEXT_ON_WALLPAPER] ?: true
            )
        }
    }

    fun getHomePreferencesFlow(): Flow<HomePreferences> {
        return preferences.data.map { prefs ->
            HomePreferences(
                homeAppsAlignmentHorizontal = getHomeAppsAlignmentHorizontalFromPref(prefs[KEY_HOME_APPS_ALIGN_HORIZONTAL]),
                homeAppsAlignmentVertical = getHomeAppsAlignmentVerticalFromPref(prefs[KEY_HOME_APPS_ALIGN_VERTICAL]),
                homeClockAlignment = getHomeClockAlignmentFromPref(prefs[KEY_HOME_CLOCK_ALIGNMENT]),
                showHomeClock = prefs[KEY_SHOW_HOME_CLOCK] ?: false,
                homeTextSize = prefs[KEY_HOME_TEXT_SIZE] ?: Constants.DEFAULT_HOME_TEXT_SIZE,
                autoOpenKeyboardAllApps = prefs[KEY_AUTO_OPEN_KEYBOARD_ALL_APPS] ?: false,
                homeClockMode = getHomeClockModeFromPref(prefs[KEY_HOME_CLOCK_MODE]),
                doubleTapToLock = prefs[KEY_DOUBLE_TAP_TO_LOCK] ?: false,
                twentyFourHourFormat = prefs[KEY_TWENTY_FOUR_HOUR_FORMAT] ?: false,
                showBatteryLevel = prefs[KEY_SHOW_BATTERY_LEVEL] ?: false,
                showHiddenAppsInSearch = prefs[KEY_SHOW_HIDDEN_APPS_IN_SEARCH] ?: true,
                drawerSearchBarAtBottom = prefs[KEY_DRAWER_SEARCH_BAR_AT_BOTTOM] ?: false,
                applyHomeAppSizeToAllApps = prefs[KEY_APPLY_HOME_APP_SIZE_TO_ALL_APPS] ?: false,
                autoOpenApp = prefs[KEY_AUTO_OPEN_APP] ?: false,
                homeAppVerticalPadding = prefs[KEY_HOME_APP_VERTICAL_PADDING] ?: Constants.DEFAULT_HOME_VERTICAL_PADDING,
                ignoreSpecialCharacters = prefs[KEY_IGNORE_SPECIAL_CHARACTERS_IN_SEARCH] ?: "",
                hideAppDrawerSearch = prefs[KEY_HIDE_APP_DRAWER_SEARCH] ?: false,
                minimoSettingsPosition = getMinimoSettingsPositionFromPref(prefs[KEY_MINIMO_SETTINGS_POSITION]),
                enableWallpaper = prefs[KEY_ENABLE_WALLPAPER] ?: false,
                showScreenTimeWidget = prefs[KEY_SHOW_SCREEN_TIME_WIDGET] ?: false,
                lightTextOnWallpaper = prefs[KEY_LIGHT_TEXT_ON_WALLPAPER] ?: true,
                dimWallpaper = prefs[KEY_DIM_WALLPAPER] ?: false,
                clockAppPreference = prefs[KEY_CLOCK_APP_PREFERENCE] ?: "",
                calendarAppPreference = prefs[KEY_CALENDAR_APP_PREFERENCE] ?: "",
                screenTimeAppPreference = prefs[KEY_SCREEN_TIME_APP_PREFERENCE] ?: "",
                swipeLeftAppPreference = prefs[KEY_SWIPE_LEFT_APP_PREFERENCE] ?: "",
                swipeRightAppPreference = prefs[KEY_SWIPE_RIGHT_APP_PREFERENCE] ?: "",
                keyboardOpenDelay = prefs[KEY_KEYBOARD_OPEN_DELAY]
                    ?: Constants.DEFAULT_KEYBOARD_OPEN_DELAY
            )
        }
    }

    fun getCustomisationPreferencesFlow(): Flow<CustomisationPreferences> {
        return preferences.data.map { prefs ->
            CustomisationPreferences(
                themeMode = getThemeModeFromPref(prefs[KEY_THEME_MODE]),
                fontPreference = prefs[KEY_FONT_PREFERENCE] ?: "",
                homeAppsAlignmentHorizontal = getHomeAppsAlignmentHorizontalFromPref(prefs[KEY_HOME_APPS_ALIGN_HORIZONTAL]),
                homeAppsAlignmentVertical = getHomeAppsAlignmentVerticalFromPref(prefs[KEY_HOME_APPS_ALIGN_VERTICAL]),
                homeClockAlignment = getHomeClockAlignmentFromPref(prefs[KEY_HOME_CLOCK_ALIGNMENT]),
                showHomeClock = prefs[KEY_SHOW_HOME_CLOCK] ?: false,
                showStatusBar = prefs[KEY_SHOW_STATUS_BAR] ?: true,
                homeTextSize = prefs[KEY_HOME_TEXT_SIZE] ?: Constants.DEFAULT_HOME_TEXT_SIZE,
                autoOpenKeyboardAllApps = prefs[KEY_AUTO_OPEN_KEYBOARD_ALL_APPS] ?: false,
                dynamicTheme = prefs[KEY_DYNAMIC_THEME] ?: false,
                homeClockMode = getHomeClockModeFromPref(prefs[KEY_HOME_CLOCK_MODE]),
                doubleTapToLock = prefs[KEY_DOUBLE_TAP_TO_LOCK] ?: false,
                twentyFourHourFormat = prefs[KEY_TWENTY_FOUR_HOUR_FORMAT] ?: false,
                showBatteryLevel = prefs[KEY_SHOW_BATTERY_LEVEL] ?: false,
                showHiddenAppsInSearch = prefs[KEY_SHOW_HIDDEN_APPS_IN_SEARCH] ?: true,
                drawerSearchBarAtBottom = prefs[KEY_DRAWER_SEARCH_BAR_AT_BOTTOM] ?: false,
                applyHomeAppSizeToAllApps = prefs[KEY_APPLY_HOME_APP_SIZE_TO_ALL_APPS] ?: false,
                blackTheme = getBlackThemeFromPref(prefs[KEY_BLACK_THEME], prefs[KEY_THEME_MODE]),
                setWallpaperToThemeColor = prefs[KEY_SET_WALLPAPER_TO_THEME_COLOR] ?: false,
                enableWallpaper = prefs[KEY_ENABLE_WALLPAPER] ?: false,
                lightTextOnWallpaper = prefs[KEY_LIGHT_TEXT_ON_WALLPAPER] ?: true,
                dimWallpaper = prefs[KEY_DIM_WALLPAPER] ?: false,
                autoOpenApp = prefs[KEY_AUTO_OPEN_APP] ?: false,
                notificationDot = prefs[KEY_NOTIFICATION_DOT] ?: false,
                homeAppVerticalPadding = prefs[KEY_HOME_APP_VERTICAL_PADDING] ?: Constants.DEFAULT_HOME_VERTICAL_PADDING,
                ignoreSpecialCharacters = prefs[KEY_IGNORE_SPECIAL_CHARACTERS_IN_SEARCH] ?: "",
                hideAppDrawerSearch = prefs[KEY_HIDE_APP_DRAWER_SEARCH] ?: false,
                minimoSettingsPosition = getMinimoSettingsPositionFromPref(prefs[KEY_MINIMO_SETTINGS_POSITION]),
                showScreenTimeWidget = prefs[KEY_SHOW_SCREEN_TIME_WIDGET] ?: false,
                clockAppPreference = prefs[KEY_CLOCK_APP_PREFERENCE] ?: "",
                calendarAppPreference = prefs[KEY_CALENDAR_APP_PREFERENCE] ?: "",
                screenTimeAppPreference = prefs[KEY_SCREEN_TIME_APP_PREFERENCE] ?: "",
                swipeLeftAppPreference = prefs[KEY_SWIPE_LEFT_APP_PREFERENCE] ?: "",
                swipeRightAppPreference = prefs[KEY_SWIPE_RIGHT_APP_PREFERENCE] ?: "",
                keyboardOpenDelay = prefs[KEY_KEYBOARD_OPEN_DELAY]
                    ?: Constants.DEFAULT_KEYBOARD_OPEN_DELAY
            )
        }
    }

    private fun getThemeModeFromPref(mode: String?): ThemeMode {
        // Added mode check of "Black" for backward compatibility. Previously "Black" theme was part of ThemeMode.
        if (mode == "Black") {
            return ThemeMode.Dark
        }

        if (!mode.isNullOrBlank() && ThemeMode.entries.any { entry -> entry.name == mode }) {
            return ThemeMode.valueOf(mode)
        }

        return ThemeMode.System
    }

    private fun getBlackThemeFromPref(blackTheme: Boolean?, themeMode: String?): Boolean {
        if (themeMode == "Black") return true
        return blackTheme ?: false
    }

    private fun getHomeAppsAlignmentHorizontalFromPref(alignment: String?): HomeAppsAlignmentHorizontal {
        if (!alignment.isNullOrBlank() && HomeAppsAlignmentHorizontal.entries.any { entry -> entry.name == alignment }) {
            return HomeAppsAlignmentHorizontal.valueOf(alignment)
        }
        return HomeAppsAlignmentHorizontal.Start
    }

    private fun getHomeAppsAlignmentVerticalFromPref(alignment: String?): HomeAppsAlignmentVertical {
        if (!alignment.isNullOrBlank() && HomeAppsAlignmentVertical.entries.any { entry -> entry.name == alignment }) {
            return HomeAppsAlignmentVertical.valueOf(alignment)
        }
        return HomeAppsAlignmentVertical.Center
    }

    private fun getHomeClockAlignmentFromPref(alignment: String?): HomeClockAlignment {
        if (!alignment.isNullOrBlank() && HomeClockAlignment.entries.any { entry -> entry.name == alignment }) {
            return HomeClockAlignment.valueOf(alignment)
        }
        return HomeClockAlignment.Start
    }

    private fun getHomeClockModeFromPref(mode: String?): HomeClockMode {
        if (!mode.isNullOrBlank() && HomeClockMode.entries.any { entry -> entry.name == mode }) {
            return HomeClockMode.valueOf(mode)
        }
        return HomeClockMode.Full
    }

    private fun getMinimoSettingsPositionFromPref(position: String?): MinimoSettingsPosition {
        if (!position.isNullOrBlank() && MinimoSettingsPosition.entries.any { entry -> entry.name == position }) {
            return MinimoSettingsPosition.valueOf(position)
        }
        return MinimoSettingsPosition.Auto
    }
}
