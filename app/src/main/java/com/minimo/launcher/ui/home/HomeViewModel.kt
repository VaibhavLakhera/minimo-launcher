package com.minimo.launcher.ui.home

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.BuildConfig
import com.minimo.launcher.R
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.FolderError
import com.minimo.launcher.data.FolderOperationException
import com.minimo.launcher.data.FolderRepository
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.data.entities.FolderEntity
import com.minimo.launcher.data.usecase.UpdateAllAppsUseCase
import com.minimo.launcher.data.usecase.UpdateAllShortcutsUseCase
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.entities.toAppPreferenceTarget
import com.minimo.launcher.utils.AppIconRepository
import com.minimo.launcher.utils.AppUtils
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.MinimoSettingsPosition
import com.minimo.launcher.utils.NotificationDotsNotifier
import com.minimo.launcher.utils.ScreenTimeHelper
import com.minimo.launcher.utils.SearchMode
import com.minimo.launcher.utils.ShortcutsUtils
import com.minimo.launcher.utils.StringUtils
import com.minimo.launcher.utils.isAppUsagePermissionGranted
import com.minimo.launcher.utils.launchApp
import com.minimo.launcher.utils.startShortcut
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val updateAllAppsUseCase: UpdateAllAppsUseCase,
    private val appInfoDao: AppInfoDao,
    private val folderRepository: FolderRepository,
    private val appUtils: AppUtils,
    private val preferenceHelper: PreferenceHelper,
    private val notificationDotsNotifier: NotificationDotsNotifier,
    @ApplicationContext
    private val applicationContext: Context,
    private val screenTimeHelper: ScreenTimeHelper,
    private val updateAllShortcutsUseCase: UpdateAllShortcutsUseCase,
    private val shortcutsUtils: ShortcutsUtils,
    private val appIconRepository: AppIconRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state
    val searchState = TextFieldState()
    val iconCacheRevision = appIconRepository.cacheRevision

    private var lastScreenTimeUpdateTime = 0L

    init {
        viewModelScope.launch {
            snapshotFlow { searchState.text.toString().trim() }.collect(::onSearchTextChange)
        }

        viewModelScope.launch {
            val description = applicationContext.getString(R.string.whats_new_description)
            if (preferenceHelper.claimWhatsNew(BuildConfig.VERSION_CODE, description)) {
                _state.update { it.copy(whatsNewDescription = description) }
            }
        }

        viewModelScope.launch {
            updateAllAppsUseCase.invoke()
        }

        viewModelScope.launch {
            updateAllShortcutsUseCase.invoke()
        }

        viewModelScope.launch {
            combine(
                folderRepository.observeCatalog(),
                notificationDotsNotifier.notificationDots
            ) { catalog, dots ->
                catalog to appUtils.mapToAppInfo(catalog.apps, dots)
            }.collect { (catalog, dbApps) ->
                _state.update { state ->
                    val allApps = getCombinedAllApps(
                        dbApps = dbApps,
                        hideAppDrawerSearch = state.hideAppDrawerSearch,
                        minimoSettingsPosition = state.minimoSettingsPosition
                    )
                    state.copy(
                        initialLoaded = true,
                        allApps = allApps,
                        favouriteApps = dbApps.filter { it.isFavourite }.sortedBy { it.orderIndex },
                        folders = projectFolders(catalog.folders, dbApps, state.folders),
                        filteredAllApps = filterDrawerApps(
                            state.searchText, allApps, state.showHiddenAppsInSearch,
                            state.ignoreSpecialCharacters, state.searchMode
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            preferenceHelper.getHomePreferencesFlow()
                .distinctUntilChanged()
                .collect { prefs ->
                    if (prefs.hideAppDrawerSearch) {
                        searchState.clearText()
                    }

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
                            newFilteredApps = filterDrawerApps(
                                searchText = clearSearchText,
                                apps = newAllApps,
                                includeHiddenApps = prefs.showHiddenAppsInSearch,
                                ignoreSpecialCharacters = prefs.ignoreSpecialCharacters,
                                searchMode = prefs.searchMode
                            )
                        } else if (
                            state.showHiddenAppsInSearch != prefs.showHiddenAppsInSearch ||
                            state.ignoreSpecialCharacters != prefs.ignoreSpecialCharacters ||
                            state.searchMode != prefs.searchMode
                        ) {
                            newFilteredApps = filterDrawerApps(
                                searchText = clearSearchText,
                                apps = newAllApps,
                                includeHiddenApps = prefs.showHiddenAppsInSearch,
                                ignoreSpecialCharacters = prefs.ignoreSpecialCharacters,
                                searchMode = prefs.searchMode
                            )
                        }

                        // Refresh screen time when the preference flag is enabled
                        if (prefs.showScreenTimeWidget && !state.showScreenTimeWidget) {
                            refreshScreenTime()
                        }

                        state.copy(
                            searchPreferencesLoaded = true,
                            appsArrangementHorizontal = homeAppsArrangementHorizontal,
                            drawerAppsArrangementHorizontal = drawerAppsArrangementHorizontal,
                            appsArrangementVertical = homeAppsArrangementVertical,
                            homeClockAlignment = homeClockAlignment,
                            showHomeClock = prefs.showHomeClock,
                            homeTimeTextSize = prefs.homeTimeTextSize,
                            homeDateTextSize = prefs.homeDateTextSize,
                            homeTimeFont = prefs.homeTimeFont,
                            homeDateFormat = prefs.homeDateFormat,
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
                            searchMode = prefs.searchMode,
                            searchBarBackground = prefs.searchBarBackground,
                            searchBarBorderPercent = prefs.searchBarBorderPercent,
                            hideAppDrawerSearch = prefs.hideAppDrawerSearch,
                            hideSettingsIcon = prefs.hideSettingsIcon,
                            minimoSettingsPosition = prefs.minimoSettingsPosition,
                            enableWallpaper = prefs.enableWallpaper,
                            enableWallpaperOnDrawer = prefs.enableWallpaperOnDrawer,
                            showScreenTimeWidget = prefs.showScreenTimeWidget,
                            lightTextOnWallpaper = prefs.lightTextOnWallpaper,
                            clockAppPreference = prefs.clockAppPreference,
                            batteryAppPreference = prefs.batteryAppPreference,
                            calendarAppPreference = prefs.calendarAppPreference,
                            screenTimeAppPreference = prefs.screenTimeAppPreference,
                            swipeLeftAppPreference = prefs.swipeLeftAppPreference,
                            swipeRightAppPreference = prefs.swipeRightAppPreference,
                            keyboardOpenDelay = prefs.keyboardOpenDelay,
                            enableFastScroller = prefs.enableFastScroller,
                            fastScrollerAlignment = prefs.fastScrollerAlignment,
                            backOpensAppDrawer = prefs.backOpensAppDrawer,
                            compactAppTouchArea = prefs.compactAppTouchArea,
                            keyboardDoneOpensFirstApp = prefs.keyboardDoneOpensFirstApp,
                            allApps = newAllApps,
                            filteredAllApps = newFilteredApps,
                            searchText = clearSearchText
                        )
                    }
                }
        }
    }

    fun toggleFolderExpanded(id: String) {
        _state.update { state ->
            state.copy(folders = state.folders.map { info ->
                if (info.folder.id == id) info.copy(isExpanded = !info.isExpanded) else info
            })
        }
    }

    fun showFolderPicker(app: AppInfo, fromHome: Boolean) =
        setFolderDialog(FolderDialog.Picker(app, fromHome))

    fun showCreateFolder(app: AppInfo, fromHome: Boolean) =
        setFolderDialog(FolderDialog.Create(app, fromHome))

    fun showRenameFolder(folder: FolderEntity) = setFolderDialog(FolderDialog.Rename(folder))
    fun showDeleteFolder(folder: FolderEntity) = setFolderDialog(FolderDialog.Delete(folder))

    private fun setFolderDialog(dialog: FolderDialog?) {
        if (!_state.value.folderSaving) {
            _state.update { it.copy(folderDialog = dialog, folderError = null) }
        }
    }

    fun dismissFolderDialog() = setFolderDialog(null)
    fun clearFolderError() {
        _state.update { it.copy(folderError = null) }
    }

    fun assignToFolder(app: AppInfo, id: String) =
        folderOperation { folderRepository.assign(app, id) }

    fun removeFromFolder(app: AppInfo) = folderOperation { folderRepository.remove(app) }
    fun toggleFolderFavourite(id: String) = folderOperation { folderRepository.toggleFavourite(id) }
    fun deleteFolder(id: String) = folderOperation { folderRepository.delete(id) }
    fun saveFolderName(name: String) {
        when (val dialog = _state.value.folderDialog) {
            is FolderDialog.Create -> folderOperation {
                folderRepository.createWithApp(
                    name,
                    dialog.app,
                    isFavourite = dialog.fromHome
                )
            }

            is FolderDialog.Rename -> folderOperation {
                folderRepository.rename(
                    dialog.folder.id,
                    name
                )
            }

            else -> Unit
        }
    }

    private fun folderOperation(operation: suspend () -> Unit) {
        if (_state.value.folderSaving) return
        _state.update { it.copy(folderSaving = true, folderError = null) }
        viewModelScope.launch {
            try {
                operation()
                _state.update {
                    it.copy(
                        folderSaving = false,
                        folderDialog = null,
                        folderError = null
                    )
                }
            } catch (cancelled: CancellationException) {
                _state.update { it.copy(folderSaving = false) }
                throw cancelled
            } catch (error: Exception) {
                Timber.e(error, "Folder operation failed")
                val reason = (error as? FolderOperationException)?.error ?: FolderError.SAVE_FAILED
                _state.update { it.copy(folderSaving = false, folderError = reason) }
                if (_state.value.folderDialog == null) {
                    Toast.makeText(applicationContext, reason.messageRes, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun loadAppIcon(app: AppInfo, sizePx: Int) = appIconRepository.loadIcon(
        packageName = app.packageName,
        itemType = app.itemType,
        targetId = app.targetId,
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
                itemType = AppItemType.APP,
                targetId = "",
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
        searchState.clearText()
    }

    fun onToggleFavouriteAppClick(app: AppInfo) {
        viewModelScope.launch {
            appInfoDao.setIndividualFavourite(
                app.itemType,
                app.targetId,
                app.packageName,
                app.userHandle,
                isFavourite = !app.isFavourite
            )
        }
    }

    fun onToggleHideClick(app: AppInfo) {
        viewModelScope.launch {
            if (app.isHidden) {
                appInfoDao.removeAppFromHidden(
                    app.itemType,
                    app.targetId,
                    app.packageName,
                    app.userHandle
                )
            } else {
                appInfoDao.addAppToHiddenTransaction(
                    app.itemType,
                    app.targetId,
                    app.packageName,
                    app.userHandle
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
            val name = newName.trim().takeUnless { it.isBlank() || it == app.appName }.orEmpty()
            appInfoDao.renameApp(
                app.itemType,
                app.targetId,
                app.packageName,
                app.userHandle,
                name
            )
        }
    }

    fun onDismissRenameAppDialog() {
        _state.update {
            it.copy(
                renameAppDialog = null
            )
        }
    }

    fun onLaunchDelayClick(app: AppInfo) {
        _state.update {
            it.copy(launchDelayDialog = app)
        }
    }

    fun onUpdateLaunchDelay(delaySeconds: Int) {
        val app = _state.value.launchDelayDialog ?: return
        onDismissLaunchDelayDialog()
        viewModelScope.launch {
            appInfoDao.updateLaunchDelay(
                itemType = app.itemType,
                targetId = app.targetId,
                packageName = app.packageName,
                userHandle = app.userHandle,
                delaySeconds = delaySeconds
            )
        }
    }

    fun onDismissLaunchDelayDialog() {
        _state.update {
            it.copy(launchDelayDialog = null)
        }
    }

    fun onAppLaunchRequest(app: AppInfo) {
        if (app.launchDelaySeconds > 0) {
            _state.update {
                it.copy(
                    launchConfirmDialog = PendingAppLaunch(
                        app = app,
                        deadlineElapsedRealtimeMillis = SystemClock.elapsedRealtime() +
                                app.launchDelaySeconds.toLong() * 1_000L
                    )
                )
            }
        } else {
            launchApp(app)
        }
    }

    fun onPreferenceAppLaunchRequest(preference: String): Boolean {
        val target = preference.toAppPreferenceTarget() ?: return false
        val app = _state.value.allApps.find(target::matches) ?: return false

        onAppLaunchRequest(app)
        return true
    }

    fun onConfirmAppLaunch() {
        val app = _state.value.launchConfirmDialog?.app ?: return
        onDismissAppLaunch()
        launchApp(app)
    }

    fun onDismissAppLaunch() {
        _state.update {
            it.copy(launchConfirmDialog = null)
        }
    }

    private fun launchApp(app: AppInfo) {
        when (app.itemType) {
            AppItemType.APP -> applicationContext.launchApp(
                app.packageName,
                app.targetId,
                app.userHandle
            )

            AppItemType.SHORTCUT -> applicationContext.startShortcut(
                app.packageName,
                app.targetId,
                app.userHandle
            )
        }
    }

    fun onConfirmDeleteShortcut(shortcut: AppInfo) {
        if (!shortcut.isShortcut) return
        viewModelScope.launch {
            val removed = withContext(Dispatchers.IO) {
                shortcutsUtils.deleteShortcut(
                    shortcut.packageName,
                    shortcut.targetId,
                    shortcut.userHandle
                )
            }
            if (removed) {
                appInfoDao.deleteAppTransaction(
                    shortcut.itemType,
                    shortcut.targetId,
                    shortcut.packageName,
                    shortcut.userHandle
                )
                appIconRepository.removeIcon(shortcut.packageName, shortcut.userHandle)
            } else {
                Toast.makeText(
                    applicationContext,
                    applicationContext.getString(R.string.failed_to_delete_shortcut),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun onSearchTextChange(searchText: String) {
        val filteredAllApps = filterDrawerApps(
            searchText = searchText,
            apps = _state.value.allApps,
            includeHiddenApps = _state.value.showHiddenAppsInSearch,
            ignoreSpecialCharacters = _state.value.ignoreSpecialCharacters,
            searchMode = _state.value.searchMode
        )
        _state.update {
            it.copy(
                searchText = searchText,
                filteredAllApps = filteredAllApps,
            )
        }
        if (searchText.isNotBlank() && _state.value.autoOpenApp && filteredAllApps.size == 1) {
            onAppLaunchRequest(filteredAllApps[0])
        }
    }

    fun onKeyboardDone() {
        val state = _state.value
        // The search flow may not have updated screen state before the keyboard action arrives.
        val searchText = searchState.text.toString().trim()
        if (searchText.isBlank()) return

        filterDrawerApps(
            searchText, state.allApps, state.showHiddenAppsInSearch,
            state.ignoreSpecialCharacters, state.searchMode
        ).firstOrNull()?.let(::onAppLaunchRequest)
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

    fun onDismissWhatsNew() {
        _state.update { it.copy(whatsNewDescription = null) }
    }
}

private fun filterDrawerApps(
    searchText: String,
    apps: List<AppInfo>,
    includeHiddenApps: Boolean,
    ignoreSpecialCharacters: String,
    searchMode: SearchMode
): List<AppInfo> {
    // Outside search, folder members and favourites stay in their own sections.
    if (searchText.isBlank()) {
        return apps.filterNot { it.isFavourite || it.isHidden || it.folderId != null }
    }
    return apps.filter {
        val name = it.name.filterNot(ignoreSpecialCharacters::contains)
        (includeHiddenApps || !it.isHidden) && StringUtils.matchesAppSearch(
            name,
            searchText,
            searchMode
        )
    }
}
