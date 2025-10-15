package com.minimo.launcher.ui.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.data.usecase.UpdateAllAppsUseCase
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.utils.AppUtils
import com.minimo.launcher.utils.Constants.INTRO_MINIMUM_FAVOURITE_COUNT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val updateAllAppsUseCase: UpdateAllAppsUseCase,
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private val _state = MutableStateFlow(IntroState())
    val state: StateFlow<IntroState> = _state

    init {
        viewModelScope.launch {
            updateAllAppsUseCase.invoke()
        }

        viewModelScope.launch {
            appInfoDao.getAllNonHiddenAppsFlow()
                .collect { appInfoList ->
                    val allApps = appUtils.mapToAppInfo(appInfoList)
                    val minimumFavouriteAdded =
                        allApps.count { it.isFavourite } >= INTRO_MINIMUM_FAVOURITE_COUNT

                    _state.update {
                        it.copy(
                            allApps = allApps,
                            filteredAllApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = allApps
                            ),
                            minimumFavouriteAdded = minimumFavouriteAdded
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
                val favouriteApps = _state.value.allApps.filter { it.isFavourite }
                val newOrderIndex = (favouriteApps.maxOfOrNull { it.orderIndex } ?: 0) + 1
                appInfoDao.addAppToFavourite(
                    appInfo.className,
                    appInfo.packageName,
                    appInfo.userHandle,
                    newOrderIndex
                )
            }
        }
    }

    fun setIntroCompleted() {
        viewModelScope.launch {
            preferenceHelper.setIsIntroCompleted(true)
        }
    }

    fun onSearchTextChange(searchText: String) {
        _state.update {
            it.copy(
                searchText = searchText,
                filteredAllApps = appUtils.getAppsWithSearch(
                    searchText = searchText,
                    apps = it.allApps
                )
            )
        }
    }
}