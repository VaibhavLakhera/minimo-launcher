package com.minimo.launcher.ui.favourite_apps

import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.SearchMode

data class FavouriteAppsState(
    val allApps: List<AppInfo> = emptyList(),
    val favouriteApps: List<AppInfo> = emptyList(),
    val filteredAllApps: List<AppInfo> = emptyList(),
    val searchText: String = "",
    val searchMode: SearchMode = SearchMode.Contains,
    val searchBarBackground: Boolean = false,
    val searchPreferencesLoaded: Boolean = false,
    val searchBarBorderPercent: Int = Constants.DEFAULT_SEARCH_BAR_BORDER_PERCENT,
    val showAppBarMorePopup: Boolean = false,
    val showFavouritesOnly: Boolean = false,
    val showReorderButton: Boolean = false
)
