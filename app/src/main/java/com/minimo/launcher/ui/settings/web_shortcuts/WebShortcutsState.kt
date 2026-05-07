package com.minimo.launcher.ui.settings.web_shortcuts

import com.minimo.launcher.ui.entities.ShortcutInfo

data class WebShortcutsState(
    val initialLoaded: Boolean = false,
    val hasShortcutHostPermission: Boolean = true,
    val searchText: String = "",
    val allShortcuts: List<ShortcutInfo> = emptyList(),
    val filteredAllShortcuts: List<ShortcutInfo> = emptyList(),
    val shortcutToRename: ShortcutInfo? = null,
    val shortcutToDelete: ShortcutInfo? = null
)