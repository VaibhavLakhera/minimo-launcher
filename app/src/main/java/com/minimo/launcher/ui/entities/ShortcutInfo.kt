package com.minimo.launcher.ui.entities

data class ShortcutInfo(
    val packageName: String,
    val shortcutId: String,
    val userHandle: Int,
    val shortcutName: String,
    val alternateShortcutName: String,
    val isFavourite: Boolean,
    val isWorkProfile: Boolean
) {
    val displayName: String
        get() = alternateShortcutName.ifEmpty { shortcutName }

    val id: String
        get() = "$packageName-$shortcutId-$userHandle"
}