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
import com.minimo.launcher.data.usecase.UpdateAllAppsUseCase
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.utils.AppUtils
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomePressedNotifier
import com.minimo.launcher.utils.MinimoSettingsPosition
import com.minimo.launcher.utils.NotificationDotsNotifier
import com.minimo.launcher.utils.ScreenTimeHelper
import com.minimo.launcher.utils.isAppUsagePermissionGranted
import com.minimo.launcher.utils.updateNotificationDots
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val homePressedNotifier: HomePressedNotifier,
    private val notificationDotsNotifier: NotificationDotsNotifier,
    @ApplicationContext
    private val applicationContext: Context,
    private val screenTimeHelper: ScreenTimeHelper
) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state

    private val _triggerHomePressed = MutableStateFlow(false)
    val triggerHomePressed: StateFlow<Boolean> = _triggerHomePressed.asStateFlow()

    private val _launchApp = Channel<AppInfo>(Channel.BUFFERED)
    val launchApp: Flow<AppInfo> = _launchApp.receiveAsFlow()

    private var lastScreenTimeUpdateTime = 0L

    init {
        viewModelScope.launch {
            updateAllAppsUseCase.invoke()
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
            preferenceHelper.getHomeAppsAlignmentHorizontal()
                .distinctUntilChanged()
                .collect { alignment ->
                    val arrangement = when (alignment) {
                        HomeAppsAlignmentHorizontal.Start -> Arrangement.Start
                        HomeAppsAlignmentHorizontal.Center -> Arrangement.Center
                        HomeAppsAlignmentHorizontal.End -> Arrangement.End
                    }
                    _state.update {
                        it.copy(appsArrangementHorizontal = arrangement)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeAppsAlignmentVertical()
                .distinctUntilChanged()
                .collect { alignment ->
                    val arrangement = when (alignment) {
                        HomeAppsAlignmentVertical.Top -> Arrangement.Top
                        HomeAppsAlignmentVertical.Center -> Arrangement.Center
                        HomeAppsAlignmentVertical.Bottom -> Arrangement.Bottom
                    }
                    _state.update {
                        it.copy(appsArrangementVertical = arrangement)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getHomeClockAlignment()
                .distinctUntilChanged()
                .collect { alignment ->
                    val horizontalAlignment = when (alignment) {
                        HomeClockAlignment.Start -> Alignment.Start
                        HomeClockAlignment.Center -> Alignment.CenterHorizontally
                        HomeClockAlignment.End -> Alignment.End
                    }
                    _state.update {
                        it.copy(homeClockAlignment = horizontalAlignment)
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
            preferenceHelper.getHomeTextSizeFlow()
                .distinctUntilChanged()
                .collect { size ->
                    _state.update {
                        it.copy(homeTextSize = size)
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
            preferenceHelper.getHomeAppVerticalPadding()
                .distinctUntilChanged()
                .collect { padding ->
                    _state.update {
                        it.copy(homeAppVerticalPadding = padding)
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
                    _state.update { state ->
                        val dbApps =
                            state.allApps.filterNot { it.packageName == Constants.MINIMO_SETTINGS_PACKAGE }
                        val allApps = getCombinedAllApps(
                            dbApps = dbApps,
                            hideAppDrawerSearch = enable,
                            minimoSettingsPosition = state.minimoSettingsPosition
                        )

                        state.copy(
                            hideAppDrawerSearch = enable,
                            allApps = allApps,
                            searchText = "",    // To clear any search result
                            filteredAllApps = getAppsWithSearch(
                                searchText = "",
                                apps = allApps,
                                includeHiddenApps = state.showHiddenAppsInSearch,
                                ignoreSpecialCharacters = state.ignoreSpecialCharacters
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getMinimoSettingsPosition()
                .distinctUntilChanged()
                .collect { position ->
                    _state.update { state ->
                        val dbApps =
                            state.allApps.filterNot { it.packageName == Constants.MINIMO_SETTINGS_PACKAGE }
                        val allApps = getCombinedAllApps(
                            dbApps = dbApps,
                            hideAppDrawerSearch = state.hideAppDrawerSearch,
                            minimoSettingsPosition = position
                        )

                        state.copy(
                            minimoSettingsPosition = position,
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
            preferenceHelper.getEnableWallpaper()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(enableWallpaper = enable)
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
                    if (enable) {
                        refreshScreenTime()
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
            preferenceHelper.getClockAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    _state.update {
                        it.copy(clockAppPreference = pref)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getCalendarAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    _state.update {
                        it.copy(calendarAppPreference = pref)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getScreenTimeAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    _state.update {
                        it.copy(screenTimeAppPreference = pref)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getSwipeLeftAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    _state.update {
                        it.copy(swipeLeftAppPreference = pref)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getSwipeRightAppPreference()
                .distinctUntilChanged()
                .collect { pref ->
                    _state.update {
                        it.copy(swipeRightAppPreference = pref)
                    }
                }
        }

        listenForHomePressedEvent()
    }

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

    private fun listenForHomePressedEvent() {
        homePressedNotifier.homePressedEvent
            .onEach {
                _triggerHomePressed.update { true }

                // Reset the flag after a delay to accept future triggers.
                // Delay is added so that bottom sheet will properly animate back to the closed position.
                viewModelScope.launch {
                    delay(1500)
                    _triggerHomePressed.update { false }
                }
            }
            .launchIn(viewModelScope)
    }

    fun setBottomSheetExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(
                isBottomSheetExpanded = isExpanded
            )
        }

        // Clear out the search text when bottom sheet is collapsed
        if (!isExpanded && _state.value.searchText.isNotBlank()) {
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

    fun onLaunchAppClick(app: AppInfo) {
        viewModelScope.launch {
            _launchApp.send(app)
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
        if (_state.value.autoOpenApp && filteredAllApps.size == 1) {
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