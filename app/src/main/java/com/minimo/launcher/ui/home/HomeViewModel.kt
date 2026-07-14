package com.minimo.launcher.ui.home

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.R
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.data.ShortcutInfoDao
import com.minimo.launcher.data.usecase.UpdateAllAppsUseCase
import com.minimo.launcher.data.usecase.UpdateAllShortcutsUseCase
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.utils.AppIconRepository
import com.minimo.launcher.utils.AppUtils
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.MinimoSettingsPosition
import com.minimo.launcher.utils.NotificationDotsNotifier
import com.minimo.launcher.utils.ScreenTimeHelper
import com.minimo.launcher.utils.ShortcutsUtils
import com.minimo.launcher.utils.isAppUsagePermissionGranted
import com.minimo.launcher.utils.updateNotificationDots
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val updateAllAppsUseCase: UpdateAllAppsUseCase,
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils,
    private val preferenceHelper: PreferenceHelper,
    private val notificationDotsNotifier: NotificationDotsNotifier,
    @ApplicationContext
    private val applicationContext: Context,
    private val screenTimeHelper: ScreenTimeHelper,
    private val shortcutInfoDao: ShortcutInfoDao,
    private val updateAllShortcutsUseCase: UpdateAllShortcutsUseCase,
    private val shortcutsUtils: ShortcutsUtils,
    private val appIconRepository: AppIconRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state

    private val _launchApp = Channel<AppInfo>(Channel.BUFFERED)
    val launchApp: Flow<AppInfo> = _launchApp.receiveAsFlow()

    private var lastScreenTimeUpdateTime = 0L

    init {
        viewModelScope.launch {
            updateAllAppsUseCase.invoke()
        }

        viewModelScope.launch {
            if (shortcutInfoDao.hasShortcuts()) {
                updateAllShortcutsUseCase.invoke()
            }
        }

        viewModelScope.launch {
            appInfoDao.getAllAppsFlow()
                .collect { appInfoList ->
                    val dbApps = appUtils.mapToAppInfo(
                        entities = appInfoList,
                        notificationDots = notificationDotsNotifier.getNotificationDots()
                    )

                    _state.update { state ->
                        val allApps = getCombinedAllApps(
                            dbApps = dbApps,
                            hideAppDrawerSearch = state.hideAppDrawerSearch,
                            minimoSettingsPosition = state.minimoSettingsPosition
                        )

                        state.copy(
                            allApps = allApps,
                            filteredAllApps = getAppsWithSearch(
                                searchText = state.searchText,
                                apps = allApps,
                                includeHiddenApps = state.showHiddenAppsInSearch,
                                ignoreSpecialCharacters = state.ignoreSpecialCharacters
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            appInfoDao.getFavouriteAppsFlow()
                .collect { appInfoList ->
                    _state.update {
                        it.copy(
                            initialLoaded = true,
                            favouriteApps = appUtils.mapToAppInfo(
                                entities = appInfoList,
                                notificationDots = notificationDotsNotifier.getNotificationDots()
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            shortcutInfoDao.getFavouriteShortcutsFlow()
                .collect { shortcutEntities ->
                    _state.update {
                        it.copy(
                            favouriteShortcuts = shortcutsUtils.mapToShortcutInfo(shortcutEntities)
                        )
                    }
                }
        }

        viewModelScope.launch {
            notificationDotsNotifier.notificationDots.collect { notificationDotSet ->
                val allApps = _state.value.allApps.updateNotificationDots(notificationDotSet)
                val favouriteApps =
                    _state.value.favouriteApps.updateNotificationDots(notificationDotSet)
                _state.update {
                    it.copy(
                        allApps = allApps,
                        filteredAllApps = getAppsWithSearch(
                            searchText = it.searchText,
                            apps = allApps,
                            includeHiddenApps = it.showHiddenAppsInSearch,
                            ignoreSpecialCharacters = it.ignoreSpecialCharacters
                        ),
                        favouriteApps = favouriteApps
                    )
                }
            }
        }

        viewModelScope.launch {
            preferenceHelper.getHomePreferencesFlow()
                .distinctUntilChanged()
                .collect { prefs ->
                    if (!prefs.showAppIconInHome &&
                        !prefs.showAppIconInDrawer &&
                        (_state.value.showAppIconInHome || _state.value.showAppIconInDrawer)
                    ) {
                        appIconRepository.clear()
                    }

                    _state.update { state ->
                        val homeAppsArrangementHorizontal =
                            when (prefs.homeAppsAlignmentHorizontal) {
                                HomeAppsAlignmentHorizontal.Start -> Arrangement.Start
                                HomeAppsAlignmentHorizontal.Center -> Arrangement.Center
                                HomeAppsAlignmentHorizontal.End -> Arrangement.End
                            }

                        val drawerAppsArrangementHorizontal =
                            when (prefs.drawerAppsAlignmentHorizontal) {
                                HomeAppsAlignmentHorizontal.Start -> Arrangement.Start
                                HomeAppsAlignmentHorizontal.Center -> Arrangement.Center
                                HomeAppsAlignmentHorizontal.End -> Arrangement.End
                            }

                        val homeAppsArrangementVertical = when (prefs.homeAppsAlignmentVertical) {
                            HomeAppsAlignmentVertical.Top -> Arrangement.Top
                            HomeAppsAlignmentVertical.Center -> Arrangement.Center
                            HomeAppsAlignmentVertical.Bottom -> Arrangement.Bottom
                        }

                        val homeClockAlignment = when (prefs.homeClockAlignment) {
                            HomeClockAlignment.Start -> Alignment.Start
                            HomeClockAlignment.Center -> Alignment.CenterHorizontally
                            HomeClockAlignment.End -> Alignment.End
                        }

                        var newAllApps = state.allApps
                        var newFilteredApps = state.filteredAllApps
                        var clearSearchText = state.searchText

                        if (state.hideAppDrawerSearch != prefs.hideAppDrawerSearch || state.minimoSettingsPosition != prefs.minimoSettingsPosition) {
                            val dbApps =
                                state.allApps.filterNot { it.packageName == Constants.MINIMO_SETTINGS_PACKAGE }
                            newAllApps = getCombinedAllApps(
                                dbApps = dbApps,
                                hideAppDrawerSearch = prefs.hideAppDrawerSearch,
                                minimoSettingsPosition = prefs.minimoSettingsPosition
                            )
                            if (prefs.hideAppDrawerSearch) {
                                clearSearchText = ""
                            }
                            newFilteredApps = getAppsWithSearch(
                                searchText = clearSearchText,
                                apps = newAllApps,
                                includeHiddenApps = prefs.showHiddenAppsInSearch,
                                ignoreSpecialCharacters = prefs.ignoreSpecialCharacters
                            )
                        } else if (state.showHiddenAppsInSearch != prefs.showHiddenAppsInSearch || state.ignoreSpecialCharacters != prefs.ignoreSpecialCharacters) {
                            newFilteredApps = getAppsWithSearch(
                                searchText = clearSearchText,
                                apps = newAllApps,
                                includeHiddenApps = prefs.showHiddenAppsInSearch,
                                ignoreSpecialCharacters = prefs.ignoreSpecialCharacters
                            )
                        }

                        // Refresh screen time when the preference flag is enabled
                        if (prefs.showScreenTimeWidget && !state.showScreenTimeWidget) {
                            refreshScreenTime()
                        }

                        state.copy(
                            appsArrangementHorizontal = homeAppsArrangementHorizontal,
                            drawerAppsArrangementHorizontal = drawerAppsArrangementHorizontal,
                            appsArrangementVertical = homeAppsArrangementVertical,
                            homeClockAlignment = homeClockAlignment,
                            showHomeClock = prefs.showHomeClock,
                            homeTextSize = prefs.homeTextSize,
                            autoOpenKeyboardAllApps = prefs.autoOpenKeyboardAllApps,
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
                            appIconSizePercent = prefs.appIconSizePercent,
                            applyHomeAppSizeToAllApps = prefs.applyHomeAppSizeToAllApps,
                            autoOpenApp = prefs.autoOpenApp,
                            homeAppVerticalPadding = prefs.homeAppVerticalPadding,
                            ignoreSpecialCharacters = prefs.ignoreSpecialCharacters,
                            hideAppDrawerSearch = prefs.hideAppDrawerSearch,
                            minimoSettingsPosition = prefs.minimoSettingsPosition,
                            enableWallpaper = prefs.enableWallpaper,
                            showScreenTimeWidget = prefs.showScreenTimeWidget,
                            lightTextOnWallpaper = prefs.lightTextOnWallpaper,
                            dimWallpaper = prefs.dimWallpaper,
                            dimWallpaperPercentage = prefs.dimWallpaperPercentage,
                            clockAppPreference = prefs.clockAppPreference,
                            batteryAppPreference = prefs.batteryAppPreference,
                            calendarAppPreference = prefs.calendarAppPreference,
                            screenTimeAppPreference = prefs.screenTimeAppPreference,
                            swipeLeftAppPreference = prefs.swipeLeftAppPreference,
                            swipeRightAppPreference = prefs.swipeRightAppPreference,
                            keyboardOpenDelay = prefs.keyboardOpenDelay,
                            enableFastScroller = prefs.enableFastScroller,
                            backOpensAppDrawer = prefs.backOpensAppDrawer,
                            allApps = newAllApps,
                            filteredAllApps = newFilteredApps,
                            searchText = clearSearchText
                        )
                    }
                }
        }
    }

    suspend fun loadAppIcon(app: AppInfo, sizePx: Int) = appIconRepository.loadIcon(
        packageName = app.packageName,
        className = app.className,
        userHandle = app.userHandle,
        sizePx = sizePx
    )

    private fun getCombinedAllApps(
        dbApps: List<AppInfo>,
        hideAppDrawerSearch: Boolean,
        minimoSettingsPosition: MinimoSettingsPosition
    ): List<AppInfo> {
        return if (hideAppDrawerSearch) {
            val settingsAppInfo = AppInfo(
                packageName = Constants.MINIMO_SETTINGS_PACKAGE,
                className = "",
                userHandle = 0,
                appName = applicationContext.getString(R.string.minimo_settings),
                alternateAppName = "",
                isFavourite = false,
                isHidden = false,
                isWorkProfile = false,
                showNotificationDot = false,
                orderIndex = 0
            )
            when (minimoSettingsPosition) {
                MinimoSettingsPosition.Top -> listOf(settingsAppInfo) + dbApps
                MinimoSettingsPosition.Bottom -> dbApps + listOf(settingsAppInfo)
                MinimoSettingsPosition.Auto -> (dbApps + settingsAppInfo).sortedWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            }
        } else {
            dbApps
        }
    }

    fun onAppDrawerClosed() {
        if (_state.value.searchText.isNotBlank()) {
            onSearchTextChange("")
        }
    }

    fun onToggleFavouriteAppClick(app: AppInfo) {
        viewModelScope.launch {
            if (app.isFavourite) {
                appInfoDao.removeAppFromFavouriteTransaction(
                    app.className,
                    app.packageName,
                    app.userHandle,
                    app.orderIndex
                )
            } else {
                val newOrderIndex =
                    (_state.value.favouriteApps.maxOfOrNull { it.orderIndex } ?: 0) + 1
                appInfoDao.addAppToFavourite(
                    app.className,
                    app.packageName,
                    app.userHandle,
                    newOrderIndex
                )
            }
        }
    }

    fun onToggleHideClick(app: AppInfo) {
        viewModelScope.launch {
            if (app.isHidden) {
                appInfoDao.removeAppFromHidden(app.className, app.packageName, app.userHandle)
            } else {
                appInfoDao.addAppToHiddenTransaction(
                    app.className,
                    app.packageName,
                    app.userHandle,
                    app.orderIndex
                )
            }
        }
    }

    fun onRenameAppClick(app: AppInfo) {
        _state.update {
            it.copy(
                renameAppDialog = app
            )
        }
    }

    fun onRenameApp(newName: String) {
        val app = _state.value.renameAppDialog ?: return
        onDismissRenameAppDialog()
        viewModelScope.launch {
            val name = newName.ifBlank {
                app.appName
            }
            appInfoDao.renameApp(app.className, app.packageName, name)
        }
    }

    fun onDismissRenameAppDialog() {
        _state.update {
            it.copy(
                renameAppDialog = null
            )
        }
    }

    fun onSearchTextChange(searchText: String) {
        val filteredAllApps = getAppsWithSearch(
            searchText = searchText,
            apps = _state.value.allApps,
            includeHiddenApps = _state.value.showHiddenAppsInSearch,
            ignoreSpecialCharacters = _state.value.ignoreSpecialCharacters
        )
        _state.update {
            it.copy(
                searchText = searchText,
                filteredAllApps = filteredAllApps,
            )
        }
        if (searchText.isNotBlank() && _state.value.autoOpenApp && filteredAllApps.size == 1) {
            _launchApp.trySend(filteredAllApps[0])
        }
    }

    /**
     * If searchText is blank, then it should always exclude the favourite and hidden apps from the list.
     *
     * If searchText is not blank, then it should use the "showHiddenApps" flag to decide whether
     * to include the hidden apps in the result.
     * */
    private fun getAppsWithSearch(
        searchText: String,
        apps: List<AppInfo>,
        includeHiddenApps: Boolean,
        ignoreSpecialCharacters: String
    ): List<AppInfo> {
        if (searchText.isBlank()) {
            return apps.filterNot { appInfo ->
                appInfo.isFavourite || appInfo.isHidden
            }
        }

        if (includeHiddenApps) {
            return apps.filter { appInfo ->
                // Filter out the special characters from the app name before searching
                val cleanedAppName = appInfo.name.filterNot { ignoreSpecialCharacters.contains(it) }
                cleanedAppName.contains(searchText, ignoreCase = true)
            }
        }

        return apps.filter { appInfo ->
            // Filter out the special characters from the app name before searching
            val cleanedAppName = appInfo.name.filterNot { ignoreSpecialCharacters.contains(it) }
            !appInfo.isHidden && cleanedAppName.contains(searchText, ignoreCase = true)
        }
    }

    fun refreshScreenTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && applicationContext.isAppUsagePermissionGranted()) {
            // Only continue if 1 minute has been passed since last update
            if (System.currentTimeMillis() - lastScreenTimeUpdateTime < 60_000) return

            viewModelScope.launch(Dispatchers.IO) {
                val totalMillis = screenTimeHelper.getTodayScreenTimeMillis()

                val hours = TimeUnit.MILLISECONDS.toHours(totalMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis) % 60
                val formattedTime = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                _state.update { it.copy(screenTime = formattedTime) }

                lastScreenTimeUpdateTime = System.currentTimeMillis()
            }
        } else {
            viewModelScope.launch {
                preferenceHelper.showScreenTimeWidget(false)
            }
        }
    }
}
