package com.minimo.launcher.ui.entities

data class AppInfo(
    val packageName: String,
    val className: String,
    val userHandle: Int,
    val appName: String,
    val alternateAppName: String,
    val isFavourite: Boolean,
    val isHidden: Boolean,
    val isWorkProfile: Boolean,
    val showNotificationDot: Boolean,
    val orderIndex: Int,
) {
    val name: String
        get() = alternateAppName.ifEmpty { appName }

    val id: String
        get() = packageName + className + userHandle
}