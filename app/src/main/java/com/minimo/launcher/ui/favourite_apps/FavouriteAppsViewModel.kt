package com.minimo.launcher.ui.favourite_apps

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
class FavouriteAppsViewModel @Inject constructor(
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils
) : ViewModel() {
    private val _state = MutableStateFlow(FavouriteAppsState())
    val state: StateFlow<FavouriteAppsState> = _state

    init {
        viewModelScope.launch {
            appInfoDao.getAllNonHiddenAppsFlow()
                .collect { appInfoList ->
                    val allApps = appUtils.mapToAppInfo(appInfoList)
                    val favouriteApps = allApps.filter { it.isFavourite }
                    val showReorderButton = favouriteApps.size > 1
                    val currentAllApps = if (_state.value.showFavouritesOnly) {
                        favouriteApps
                    } else {
                        allApps
                    }

                    _state.update {
                        it.copy(
                            allApps = allApps,
                            favouriteApps = favouriteApps,
                            filteredAllApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = currentAllApps
                            ),
                            showReorderButton = showReorderButton
                        )
                    }
                }
        }
    }

    fun onToggleFavouriteAppClick(appInfo: AppInfo) {
        viewModelScope.launch {
            if (appInfo.isFavourite) {
                appInfoDao.removeAppFromFavouriteTransaction(
                    appInfo.className,
                    appInfo.packageName,
                    appInfo.userHandle,
                    appInfo.orderIndex
                )
            } else {
                val newOrderIndex =
                    (_state.value.favouriteApps.maxOfOrNull { it.orderIndex } ?: 0) + 1
                appInfoDao.addAppToFavourite(
                    appInfo.className,
                    appInfo.packageName,
                    appInfo.userHandle,
                    newOrderIndex
                )
            }
        }
    }

    fun onSearchTextChange(searchText: String) {
        val currentAllApps = if (_state.value.showFavouritesOnly) {
            _state.value.favouriteApps
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

    fun onToggleShowFavouritesOnly() {
        val showFavouritesOnly = !_state.value.showFavouritesOnly
        _state.update {
            it.copy(
                showAppBarMorePopup = false,
                showFavouritesOnly = showFavouritesOnly,
                filteredAllApps = appUtils.getAppsWithSearch(
                    searchText = it.searchText,
                    apps = if (showFavouritesOnly) {
                        it.favouriteApps
                    } else {
                        it.allApps
                    }
                )
            )
        }
    }
}