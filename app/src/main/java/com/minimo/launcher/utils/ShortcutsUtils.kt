package com.minimo.launcher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import com.minimo.launcher.data.entities.AppItemType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ShortcutsUtils @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val launcherApps by lazy {
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    }

    fun hasShortcutHostPermission(): Boolean {
        return try {
            launcherApps.hasShortcutHostPermission()
        } catch (exception: Exception) {
            Timber.e(exception)
            false
        }
    }

    suspend fun getInstalledShortcuts(): ShortcutInventory? = withContext(Dispatchers.IO) {
        if (!hasShortcutHostPermission()) return@withContext null

        val profiles = try {
            launcherApps.profiles
        } catch (exception: Exception) {
            Timber.e(exception)
            return@withContext null
        }
        if (profiles.isEmpty()) return@withContext null
        val currentProfiles = profiles.mapTo(mutableSetOf()) { it.hashCode() }
        val successfulProfiles = mutableSetOf<Int>()
        val shortcuts = mutableListOf<InstalledShortcut>()

        for (profile in profiles) {
            val query = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            }
            val profileShortcuts = try {
                launcherApps.getShortcuts(query, profile)
            } catch (exception: Exception) {
                Timber.e(exception, "Unable to query shortcuts for profile %s", profile)
                continue
            }
            if (profileShortcuts == null) {
                // A null result is not an authoritative empty inventory. Preserve this profile's
                // database rows until Android can return a successful query result.
                Timber.w("Shortcut query returned null for profile %s", profile)
                continue
            }

            successfulProfiles.add(profile.hashCode())
            shortcuts.addAll(profileShortcuts.map(::mapInstalledShortcut))
        }

        ShortcutInventory(
            shortcuts = shortcuts,
            currentProfiles = currentProfiles,
            successfulProfiles = successfulProfiles
        )
    }

    fun mapInstalledShortcut(shortcut: ShortcutInfo): InstalledShortcut {
        return InstalledShortcut(
            appName = shortcut.shortLabel?.toString() ?: shortcut.id,
            packageName = shortcut.`package`,
            shortcutId = shortcut.id,
            userHandle = shortcut.userHandle.hashCode()
        )
    }

    fun deleteShortcut(packageName: String, shortcutId: String, userHandle: Int): Boolean {
        if (!hasShortcutHostPermission()) return false

        return try {
            val targetUser = launcherApps.profiles.find { it.hashCode() == userHandle }
                ?: return false
            val query = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                setPackage(packageName)
            }
            val shortcuts = launcherApps.getShortcuts(query, targetUser)
            if (shortcuts == null) {
                // Deletion must not be considered successful unless Android confirms that the
                // shortcut is already absent or accepts the updated pinned shortcut list.
                Timber.w("Shortcut deletion query returned null for profile %s", targetUser)
                return false
            }
            if (shortcuts.none { it.id == shortcutId }) return true

            launcherApps.pinShortcuts(
                packageName,
                shortcuts.map { it.id }.filterNot { it == shortcutId },
                targetUser
            )
            true
        } catch (exception: Exception) {
            Timber.e(exception)
            false
        }
    }

    fun getShortcut(
        packageName: String,
        shortcutId: String,
        userHandle: Int
    ): ShortcutInfo? {
        if (!hasShortcutHostPermission()) return null

        return try {
            val targetUser = launcherApps.profiles.find { it.hashCode() == userHandle }
                ?: return null
            val query = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                setPackage(packageName)
                setShortcutIds(listOf(shortcutId))
            }
            launcherApps.getShortcuts(query, targetUser)?.firstOrNull {
                it.id == shortcutId
            }
        } catch (exception: Exception) {
            Timber.e(exception)
            null
        }
    }
}

data class ShortcutInventory(
    val shortcuts: List<InstalledShortcut>,
    val currentProfiles: Set<Int>,
    val successfulProfiles: Set<Int>
)

data class InstalledShortcut(
    val appName: String,
    val packageName: String,
    val shortcutId: String,
    val userHandle: Int
) {
    val id: String
        get() = "${AppItemType.SHORTCUT.persistedValue}|$packageName|$shortcutId|$userHandle"
}
