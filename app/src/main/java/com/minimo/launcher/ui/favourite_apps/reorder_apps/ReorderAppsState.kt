package com.minimo.launcher.ui.favourite_apps.reorder_apps

import com.minimo.launcher.ui.entities.AppInfo

data class ReorderAppsState(
    val apps: List<AppInfo> = emptyList()
)