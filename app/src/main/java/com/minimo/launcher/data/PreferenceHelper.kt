package com.minimo.launcher.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        private val KEY_HIDE_APP_DRAWER_ARROW = booleanPreferencesKey("KEY_HIDE_APP_DRAWER_ARROW")
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

    fun getThemeMode(): Flow<ThemeMode> {
        return preferences.data.map {
            val mode = it[KEY_THEME_MODE]
            // Added mode check of "Black" for backward compatibility. Previously "Black" theme was part of ThemeMode.
            if (mode == "Black") {
                ThemeMode.Dark
            } else if (!mode.isNullOrBlank()
                && ThemeMode.entries.any { entry -> entry.name == mode }
            ) {
                ThemeMode.valueOf(mode)
            } else {
                ThemeMode.System
            }
        }
    }

    suspend fun setHomeAppsAlignmentHorizontal(alignment: HomeAppsAlignmentHorizontal) {
        preferences.edit {
            it[KEY_HOME_APPS_ALIGN_HORIZONTAL] = alignment.name
        }
    }

    fun getHomeAppsAlignmentHorizontal(): Flow<HomeAppsAlignmentHorizontal> {
        return preferences.data.map {
            val alignment = it[KEY_HOME_APPS_ALIGN_HORIZONTAL]
            if (!alignment.isNullOrBlank()
                && HomeAppsAlignmentHorizontal.entries.any { entry -> entry.name == alignment }
            ) {
                HomeAppsAlignmentHorizontal.valueOf(alignment)
            } else {
                HomeAppsAlignmentHorizontal.Start
            }
        }
    }

    suspend fun setHomeAppsAlignmentVertical(alignment: HomeAppsAlignmentVertical) {
        preferences.edit {
            it[KEY_HOME_APPS_ALIGN_VERTICAL] = alignment.name
        }
    }

    fun getHomeAppsAlignmentVertical(): Flow<HomeAppsAlignmentVertical> {
        return preferences.data.map {
            val alignment = it[KEY_HOME_APPS_ALIGN_VERTICAL]
            if (!alignment.isNullOrBlank()
                && HomeAppsAlignmentVertical.entries.any { entry -> entry.name == alignment }
            ) {
                HomeAppsAlignmentVertical.valueOf(alignment)
            } else {
                HomeAppsAlignmentVertical.Center
            }
        }
    }

    suspend fun setHomeClockAlignment(alignment: HomeClockAlignment) {
        preferences.edit {
            it[KEY_HOME_CLOCK_ALIGNMENT] = alignment.name
        }
    }

    fun getHomeClockAlignment(): Flow<HomeClockAlignment> {
        return preferences.data.map {
            val alignment = it[KEY_HOME_CLOCK_ALIGNMENT]
            if (!alignment.isNullOrBlank()
                && HomeClockAlignment.entries.any { entry -> entry.name == alignment }
            ) {
                HomeClockAlignment.valueOf(alignment)
            } else {
                HomeClockAlignment.Start
            }
        }
    }

    suspend fun setShowHomeClock(show: Boolean) {
        preferences.edit {
            it[KEY_SHOW_HOME_CLOCK] = show
        }
    }

    fun getShowHomeClock(): Flow<Boolean> {
        return preferences.data.map { it[KEY_SHOW_HOME_CLOCK] ?: false }
    }

    suspend fun setShowStatusBar(show: Boolean) {
        preferences.edit {
            it[KEY_SHOW_STATUS_BAR] = show
        }
    }

    fun getShowStatusBar(): Flow<Boolean> {
        return preferences.data.map { it[KEY_SHOW_STATUS_BAR] ?: true }
    }

    suspend fun setHomeTextSize(size: Int) {
        preferences.edit { preferences ->
            preferences[KEY_HOME_TEXT_SIZE] = size
        }
    }

    fun getHomeTextSizeFlow(): Flow<Int> {
        return preferences.data.map {
            it[KEY_HOME_TEXT_SIZE] ?: Constants.DEFAULT_HOME_TEXT_SIZE
        }
    }

    suspend fun setAutoOpenKeyboardAllApps(open: Boolean) {
        preferences.edit {
            it[KEY_AUTO_OPEN_KEYBOARD_ALL_APPS] = open
        }
    }

    fun getAutoOpenKeyboardAllApps(): Flow<Boolean> {
        return preferences.data.map { it[KEY_AUTO_OPEN_KEYBOARD_ALL_APPS] ?: false }
    }

    suspend fun setDynamicTheme(enable: Boolean) {
        preferences.edit {
            it[KEY_DYNAMIC_THEME] = enable
        }
    }

    fun getDynamicTheme(): Flow<Boolean> {
        return preferences.data.map { it[KEY_DYNAMIC_THEME] ?: false }
    }

    suspend fun setHomeClockMode(mode: HomeClockMode) {
        preferences.edit {
            it[KEY_HOME_CLOCK_MODE] = mode.name
        }
    }

    fun getHomeClockMode(): Flow<HomeClockMode> {
        return preferences.data.map {
            val mode = it[KEY_HOME_CLOCK_MODE]
            if (!mode.isNullOrBlank()
                && HomeClockMode.entries.any { entry -> entry.name == mode }
            ) {
                HomeClockMode.valueOf(mode)
            } else {
                HomeClockMode.Full
            }
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

    fun getTwentyFourHourFormat(): Flow<Boolean> {
        return preferences.data.map { it[KEY_TWENTY_FOUR_HOUR_FORMAT] ?: false }
    }

    suspend fun setShowBatteryLevel(enable: Boolean) {
        preferences.edit {
            it[KEY_SHOW_BATTERY_LEVEL] = enable
        }
    }

    fun getShowBatteryLevel(): Flow<Boolean> {
        return preferences.data.map { it[KEY_SHOW_BATTERY_LEVEL] ?: false }
    }

    suspend fun setShowHiddenAppsInSearch(enable: Boolean) {
        preferences.edit {
            it[KEY_SHOW_HIDDEN_APPS_IN_SEARCH] = enable
        }
    }

    fun getShowHiddenAppsInSearch(): Flow<Boolean> {
        return preferences.data.map { it[KEY_SHOW_HIDDEN_APPS_IN_SEARCH] ?: true }
    }

    suspend fun setDrawerSearchBarAtBottom(enable: Boolean) {
        preferences.edit {
            it[KEY_DRAWER_SEARCH_BAR_AT_BOTTOM] = enable
        }
    }

    fun getDrawerSearchBarAtBottom(): Flow<Boolean> {
        return preferences.data.map { it[KEY_DRAWER_SEARCH_BAR_AT_BOTTOM] ?: false }
    }

    suspend fun setHomeAppSizeToAllApps(enable: Boolean) {
        preferences.edit {
            it[KEY_APPLY_HOME_APP_SIZE_TO_ALL_APPS] = enable
        }
    }

    fun getHomeAppSizeToAllApps(): Flow<Boolean> {
        return preferences.data.map { it[KEY_APPLY_HOME_APP_SIZE_TO_ALL_APPS] ?: false }
    }

    suspend fun setBlackTheme(enable: Boolean) {
        preferences.edit {
            it[KEY_BLACK_THEME] = enable
        }
    }

    fun getBlackTheme(): Flow<Boolean> {
        return preferences.data.map {
            // Added themeMode check for backward compatibility. Previously "Black" theme was part of ThemeMode.
            val themeMode = it[KEY_THEME_MODE]
            if (themeMode == "Black") {
                true
            } else {
                it[KEY_BLACK_THEME] ?: false
            }
        }
    }

    suspend fun setSetWallpaperToThemeColor(enable: Boolean) {
        preferences.edit {
            it[KEY_SET_WALLPAPER_TO_THEME_COLOR] = enable
        }
    }

    fun getSetWallpaperToThemeColor(): Flow<Boolean> {
        return preferences.data.map { it[KEY_SET_WALLPAPER_TO_THEME_COLOR] ?: false }
    }

    suspend fun setEnableWallpaper(enable: Boolean) {
        preferences.edit {
            it[KEY_ENABLE_WALLPAPER] = enable
        }
    }

    fun getEnableWallpaper(): Flow<Boolean> {
        return preferences.data.map { it[KEY_ENABLE_WALLPAPER] ?: false }
    }

    suspend fun setLightTextOnWallpaper(enable: Boolean) {
        preferences.edit {
            it[KEY_LIGHT_TEXT_ON_WALLPAPER] = enable
        }
    }

    fun getLightTextOnWallpaper(): Flow<Boolean> {
        return preferences.data.map { it[KEY_LIGHT_TEXT_ON_WALLPAPER] ?: true }
    }

    suspend fun setDimWallpaper(enable: Boolean) {
        preferences.edit {
            it[KEY_DIM_WALLPAPER] = enable
        }
    }

    fun getDimWallpaper(): Flow<Boolean> {
        return preferences.data.map { it[KEY_DIM_WALLPAPER] ?: false }
    }

    suspend fun setAutoOpenApp(enable: Boolean) {
        preferences.edit {
            it[KEY_AUTO_OPEN_APP] = enable
        }
    }

    fun getAutoOpenApp(): Flow<Boolean> {
        return preferences.data.map { it[KEY_AUTO_OPEN_APP] ?: false }
    }

    suspend fun hideAppDrawerArrow(enable: Boolean) {
        preferences.edit {
            it[KEY_HIDE_APP_DRAWER_ARROW] = enable
        }
    }

    fun getHideAppDrawerArrow(): Flow<Boolean> {
        return preferences.data.map { it[KEY_HIDE_APP_DRAWER_ARROW] ?: false }
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

    fun getHomeAppVerticalPadding(): Flow<Int> {
        return preferences.data.map {
            it[KEY_HOME_APP_VERTICAL_PADDING] ?: Constants.DEFAULT_HOME_VERTICAL_PADDING
        }
    }

    suspend fun setIgnoreSpecialCharacters(characters: String) {
        preferences.edit {
            it[KEY_IGNORE_SPECIAL_CHARACTERS_IN_SEARCH] = characters
        }
    }

    fun getIgnoreSpecialCharacters(): Flow<String> {
        return preferences.data.map {
            it[KEY_IGNORE_SPECIAL_CHARACTERS_IN_SEARCH] ?: ""
        }
    }

    suspend fun hideAppDrawerSearch(enable: Boolean) {
        preferences.edit {
            it[KEY_HIDE_APP_DRAWER_SEARCH] = enable
        }
    }

    fun getHideAppDrawerSearch(): Flow<Boolean> {
        return preferences.data.map { it[KEY_HIDE_APP_DRAWER_SEARCH] ?: false }
    }

    suspend fun showScreenTimeWidget(enable: Boolean) {
        preferences.edit {
            it[KEY_SHOW_SCREEN_TIME_WIDGET] = enable
        }
    }

    fun getShowScreenTimeWidget(): Flow<Boolean> {
        return preferences.data.map { it[KEY_SHOW_SCREEN_TIME_WIDGET] ?: false }
    }

    suspend fun setClockAppPreference(appData: String) {
        preferences.edit {
            it[KEY_CLOCK_APP_PREFERENCE] = appData
        }
    }

    fun getClockAppPreference(): Flow<String> {
        return preferences.data.map { it[KEY_CLOCK_APP_PREFERENCE] ?: "" }
    }

    suspend fun setCalendarAppPreference(appData: String) {
        preferences.edit {
            it[KEY_CALENDAR_APP_PREFERENCE] = appData
        }
    }

    fun getCalendarAppPreference(): Flow<String> {
        return preferences.data.map { it[KEY_CALENDAR_APP_PREFERENCE] ?: "" }
    }

    suspend fun setScreenTimeAppPreference(appData: String) {
        preferences.edit {
            it[KEY_SCREEN_TIME_APP_PREFERENCE] = appData
        }
    }

    fun getScreenTimeAppPreference(): Flow<String> {
        return preferences.data.map { it[KEY_SCREEN_TIME_APP_PREFERENCE] ?: "" }
    }

    suspend fun setSwipeLeftAppPreference(appData: String) {
        preferences.edit {
            it[KEY_SWIPE_LEFT_APP_PREFERENCE] = appData
        }
    }

    fun getSwipeLeftAppPreference(): Flow<String> {
        return preferences.data.map { it[KEY_SWIPE_LEFT_APP_PREFERENCE] ?: "" }
    }

    suspend fun setSwipeRightAppPreference(appData: String) {
        preferences.edit {
            it[KEY_SWIPE_RIGHT_APP_PREFERENCE] = appData
        }
    }

    fun getSwipeRightAppPreference(): Flow<String> {
        return preferences.data.map { it[KEY_SWIPE_RIGHT_APP_PREFERENCE] ?: "" }
    }

    suspend fun setFontPreference(font: String) {
        preferences.edit {
            it[KEY_FONT_PREFERENCE] = font
        }
    }

    fun getFontPreference(): Flow<String> {
        return preferences.data.map { it[KEY_FONT_PREFERENCE] ?: "" }
    }

    suspend fun setMinimoSettingsPosition(position: MinimoSettingsPosition) {
        preferences.edit {
            it[KEY_MINIMO_SETTINGS_POSITION] = position.name
        }
    }

    fun getMinimoSettingsPosition(): Flow<MinimoSettingsPosition> {
        return preferences.data.map {
            val position = it[KEY_MINIMO_SETTINGS_POSITION]
            if (!position.isNullOrBlank()
                && MinimoSettingsPosition.entries.any { entry -> entry.name == position }
            ) {
                MinimoSettingsPosition.valueOf(position)
            } else {
                MinimoSettingsPosition.Auto
            }
        }
    }
}
