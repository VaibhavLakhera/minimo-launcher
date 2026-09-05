package com.minimo.launcher.ui.entities

import android.net.Uri
import com.minimo.launcher.data.entities.AppItemType

private const val PREFERENCE_VERSION = "2"
private const val LEGACY_PREFERENCE_PART_COUNT = 3
private const val VERSIONED_PREFERENCE_PART_COUNT = 5

data class AppInfo(
    val packageName: String,
    val itemType: AppItemType,
    val targetId: String,
    val userHandle: Int,
    val appName: String,
    val alternateAppName: String,
    val isFavourite: Boolean,
    val isHidden: Boolean,
    val isWorkProfile: Boolean,
    val showNotificationDot: Boolean,
    val orderIndex: Int,
    val launchDelaySeconds: Int = 0,
) {
    val name: String
        get() = alternateAppName.ifEmpty { appName }

    val isShortcut: Boolean
        get() = itemType == AppItemType.SHORTCUT

    val id: String
        get() = "${itemType.persistedValue}|$packageName|$targetId|$userHandle"

    val preferenceValue: String
        get() = AppPreferenceTarget(
            itemType = itemType,
            packageName = packageName,
            targetId = targetId,
            userHandle = userHandle
        ).preferenceValue
}

data class AppPreferenceTarget(
    val itemType: AppItemType,
    val packageName: String,
    val targetId: String,
    val userHandle: Int
) {
    val preferenceValue: String
        get() = listOf(
            PREFERENCE_VERSION,
            itemType.persistedValue,
            // Encoding prevents a delimiter in any future package format or shortcut ID from
            // being interpreted as an additional field when the preference is parsed.
            Uri.encode(packageName),
            Uri.encode(targetId),
            userHandle.toString()
        ).joinToString("|")

    fun matches(app: AppInfo): Boolean {
        return itemType == app.itemType &&
                packageName == app.packageName &&
                targetId == app.targetId &&
                userHandle == app.userHandle
    }
}

fun String.toAppPreferenceTarget(): AppPreferenceTarget? {
    val parts = split("|")
    return when (parts.size) {
        LEGACY_PREFERENCE_PART_COUNT -> {
            // Preferences saved before shortcuts were unified contain package, activity class,
            // and user only. Treat them as app targets so existing selections keep working.
            AppPreferenceTarget(
                itemType = AppItemType.APP,
                packageName = parts[0],
                targetId = parts[1],
                userHandle = parts[2].toIntOrNull() ?: return null
            )
        }

        VERSIONED_PREFERENCE_PART_COUNT -> {
            // Version 2 stores the item type and safely encoded identity fields, allowing both
            // apps and shortcuts to be selected without colliding with the legacy format.
            if (parts[0] != PREFERENCE_VERSION) return null
            AppPreferenceTarget(
                itemType = runCatching {
                    AppItemType.fromPersistedValue(parts[1])
                }.getOrNull() ?: return null,
                packageName = Uri.decode(parts[2]),
                targetId = Uri.decode(parts[3]),
                userHandle = parts[4].toIntOrNull() ?: return null
            )
        }

        // Reject malformed preferences and unknown formats instead of launching the wrong item.
        else -> null
    }
}
