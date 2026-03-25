package com.minimo.launcher.ui.settings.app_picker

import com.minimo.launcher.ui.entities.AppInfo

data class AppPickerState(
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchText: String = ""
)
