package com.minimo.launcher.ui.hidden_apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HiddenAppsViewModel @Inject constructor(
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils
) : ViewModel() {
    private val _state = MutableStateFlow(HiddenAppsState())
    val state: StateFlow<HiddenAppsState> = _state

    init {
        viewModelScope.launch {
            appInfoDao.getAllAppsFlow()
                .collect { appInfoList ->
                    val allApps = appUtils.mapToAppInfo(appInfoList)
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
                                apps = currentAllApps
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
                    appInfo.className,
                    appInfo.packageName,
                    appInfo.userHandle
                )
            } else {
                appInfoDao.addAppToHiddenTransaction(
                    appInfo.className,
                    appInfo.packageName,
                    appInfo.userHandle,
                    appInfo.orderIndex
                )
            }
        }
    }

    fun onSearchTextChange(searchText: String) {
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
                    apps = currentAllApps
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
                    }
                )
            )
        }
    }
}