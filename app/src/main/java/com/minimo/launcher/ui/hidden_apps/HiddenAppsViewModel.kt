package com.minimo.launcher.ui.hidden_apps

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.R
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.data.usecase.AddUpdateAppsUseCase
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.utils.AppUtils
import com.minimo.launcher.utils.PrivateSpaceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HiddenAppsViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils,
    private val preferenceHelper: PreferenceHelper,
    private val privateSpaceManager: PrivateSpaceManager,
    private val addUpdateAppsUseCase: AddUpdateAppsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(HiddenAppsState())
    val state: StateFlow<HiddenAppsState> = _state
    val searchState = TextFieldState()

    init {
        viewModelScope.launch {
            snapshotFlow { searchState.text.toString().trim() }.collect(::onSearchTextChange)
        }

        viewModelScope.launch {
            preferenceHelper.getSearchPreferencesFlow()
                .collect { prefs ->
                    _state.update {
                        it.copy(
                            searchPreferencesLoaded = true,
                            searchMode = prefs.searchMode,
                            searchBarBackground = prefs.searchBarBackground,
                            searchBarBorderPercent = prefs.searchBarBorderPercent,
                            filteredAllApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = if (it.showHiddenOnly) it.hiddenApps else it.allApps,
                                searchMode = prefs.searchMode
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            combine(
                appInfoDao.getAllNonFavouriteAppsFlow(),
                privateSpaceManager.loadedState
            ) { appInfoList, privateSpace -> appInfoList to privateSpace }
                .collect { (appInfoList, privateSpace) ->
                    // Private space apps can only be hidden or unhidden while it is unlocked.
                    val allApps =
                        appUtils.mapToAppInfo(appInfoList, privateSpaceState = privateSpace)
                            .filter { !it.isPrivateSpace || privateSpace.isUnlocked }
                    val hiddenApps = allApps.filter { it.isHidden }
                    val currentAllApps = if (_state.value.showHiddenOnly) {
                        hiddenApps
                    } else {
                        allApps
                    }

                    _state.update {
                        it.copy(
                            allApps = allApps,
                            hiddenApps = hiddenApps,
                            filteredAllApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = currentAllApps,
                                searchMode = it.searchMode
                            ),
                        )
                    }
                }
        }
    }

    fun onToggleHiddenAppClick(appInfo: AppInfo) {
        viewModelScope.launch {
            if (appInfo.isHidden) {
                appInfoDao.removeAppFromHidden(
                    appInfo.itemType,
                    appInfo.targetId,
                    appInfo.packageName,
                    appInfo.userHandle
                )
            } else {
                appInfoDao.addAppToHiddenTransaction(
                    appInfo.itemType,
                    appInfo.targetId,
                    appInfo.packageName,
                    appInfo.userHandle
                )
            }
        }
    }

    private fun onSearchTextChange(searchText: String) {
        val currentAllApps = if (_state.value.showHiddenOnly) {
            _state.value.hiddenApps
        } else {
            _state.value.allApps
        }

        _state.update {
            it.copy(
                searchText = searchText,
                filteredAllApps = appUtils.getAppsWithSearch(
                    searchText = searchText,
                    apps = currentAllApps,
                    searchMode = it.searchMode
                )
            )
        }
    }

    fun onToggleAppBarMorePopup() {
        _state.update {
            it.copy(
                showAppBarMorePopup = !it.showAppBarMorePopup
            )
        }
    }

    fun onToggleShowHiddenOnly() {
        val showHiddenOnly = !_state.value.showHiddenOnly
        _state.update {
            it.copy(
                showAppBarMorePopup = false,
                showHiddenOnly = showHiddenOnly,
                filteredAllApps = appUtils.getAppsWithSearch(
                    searchText = it.searchText,
                    apps = if (showHiddenOnly) {
                        it.hiddenApps
                    } else {
                        it.allApps
                    },
                    searchMode = it.searchMode
                )
            )
        }
    }

    fun onAppLaunchFailed(app: AppInfo) {
        Toast.makeText(applicationContext, R.string.app_launch_failed, Toast.LENGTH_SHORT).show()
        viewModelScope.launch {
            addUpdateAppsUseCase.resyncAfterFailedLaunch(app.packageName, app.userHandle)
        }
    }
}
