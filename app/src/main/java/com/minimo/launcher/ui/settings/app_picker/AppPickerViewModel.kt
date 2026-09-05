package com.minimo.launcher.ui.settings.app_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private val _state = MutableStateFlow(AppPickerState())
    val state: StateFlow<AppPickerState> = _state

    init {
        viewModelScope.launch {
            preferenceHelper.getSearchPreferencesFlow()
                .collect { prefs ->
                    _state.update {
                        it.copy(
                            searchPreferencesLoaded = true,
                            searchMode = prefs.searchMode,
                            searchBarBackground = prefs.searchBarBackground,
                            searchBarBorderPercent = prefs.searchBarBorderPercent,
                            filteredApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = it.allApps,
                                searchMode = prefs.searchMode
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            appInfoDao.getAllAppsFlow()
                .collect { appInfoList ->
                    val allApps = appUtils.mapToAppInfo(appInfoList)

                    _state.update {
                        it.copy(
                            allApps = allApps,
                            filteredApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = allApps,
                                searchMode = it.searchMode
                            )
                        )
                    }
                }
        }
    }

    fun onSearchTextChange(searchText: String) {
        _state.update {
            it.copy(
                searchText = searchText,
                filteredApps = appUtils.getAppsWithSearch(
                    searchText = searchText,
                    apps = it.allApps,
                    searchMode = it.searchMode
                )
            )
        }
    }
}
