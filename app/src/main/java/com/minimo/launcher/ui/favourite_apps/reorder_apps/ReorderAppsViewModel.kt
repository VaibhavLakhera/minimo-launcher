package com.minimo.launcher.ui.favourite_apps.reorder_apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.entities.AppOrderUpdate
import com.minimo.launcher.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReorderAppsViewModel @Inject constructor(
    private val appUtils: AppUtils,
    private val appInfoDao: AppInfoDao
) : ViewModel() {
    private val _state = MutableStateFlow(ReorderAppsState())
    val state: StateFlow<ReorderAppsState> = _state

    private var firstResultReceived = false

    init {
        viewModelScope.launch {
            appInfoDao.getFavouriteAppsFlow()
                .collect { appList ->
                    val apps = appUtils.mapToAppInfo(appList)
                    _state.update {
                        _state.value.copy(
                            apps = apps
                        )
                    }

                    // In rare scenario, due to package change notifier, order index changes to some incorrect value and needs to be fixed.
                    if (!firstResultReceived) {
                        firstResultReceived = true
                        if (isCorrectOrderIndex(apps).not()) {
                            fixOrderIndex(apps)
                        }
                    }
                }
        }
    }

    private fun isCorrectOrderIndex(apps: List<AppInfo>): Boolean {
        for (index in apps.indices) {
            if (apps[index].orderIndex != index + 1) {
                return false
            }
        }
        return true
    }

    private fun fixOrderIndex(apps: List<AppInfo>) {
        val updates = mutableListOf<AppOrderUpdate>()
        for (index in apps.indices) {
            updates.add(
                AppOrderUpdate(
                    packageName = apps[index].packageName,
                    className = apps[index].className,
                    userHandle = apps[index].userHandle,
                    orderIndex = index + 1
                )
            )
        }
        viewModelScope.launch {
            appInfoDao.updateAppOrder(updates)
        }
    }

    fun onAppMoveUpClick(app: AppInfo) {
        viewModelScope.launch {
            val currentIndex = _state.value.apps.indexOfFirst { it.id == app.id }
            if (currentIndex != -1 && currentIndex > 0) {
                val newIndex = currentIndex - 1
                val appAtNewIndex = _state.value.apps[newIndex]
                swapAppOrderIndex(app, appAtNewIndex)
            }
        }
    }

    fun onAppMoveDownClick(app: AppInfo) {
        viewModelScope.launch {
            val currentIndex = _state.value.apps.indexOfFirst { it.id == app.id }
            val lastIndex = _state.value.apps.lastIndex
            if (currentIndex != -1 && currentIndex < lastIndex) {
                val newIndex = currentIndex + 1
                val appAtNewIndex = _state.value.apps[newIndex]
                swapAppOrderIndex(app, appAtNewIndex)
            }
        }
    }

    private fun swapAppOrderIndex(app1: AppInfo, app2: AppInfo) {
        viewModelScope.launch {
            appInfoDao.swapOrderIndex(
                app1.className,
                app1.packageName,
                app1.userHandle,
                app2.className,
                app2.packageName,
                app2.userHandle,
                app1.orderIndex,
                app2.orderIndex
            )
        }
    }
}