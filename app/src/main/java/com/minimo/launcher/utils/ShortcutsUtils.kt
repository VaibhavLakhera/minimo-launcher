package com.minimo.launcher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import com.minimo.launcher.data.entities.ShortcutInfoEntity
import com.minimo.launcher.ui.entities.ShortcutInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun getInstalledShortcuts(): List<InstalledShortcut> = withContext(Dispatchers.IO) {
        if (!hasShortcutHostPermission()) {
            return@withContext emptyList()
        }

        val profiles = launcherApps.profiles
        val allShortcuts = mutableListOf<InstalledShortcut>()

        for (userHandle in profiles) {
            val query = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            }

            val shortcuts = try {
                launcherApps.getShortcuts(query, userHandle)
            } catch (e: Exception) {
                Timber.e(e)
                emptyList()
            }

            val profileShortcuts = shortcuts?.map { shortcut ->
                async {
                    val appName = shortcut.shortLabel?.toString() ?: shortcut.id
                    val packageName = shortcut.`package`
                    val shortcutId = shortcut.id
                    val userHandleHash = userHandle.hashCode()

                    InstalledShortcut(
                        appName = appName,
                        packageName = packageName,
                        shortcutId = shortcutId,
                        userHandle = userHandleHash
                    )
                }
            }?.awaitAll() ?: emptyList()

            allShortcuts.addAll(profileShortcuts)
        }

        allShortcuts
    }

    fun deleteShortcut(packageName: String, shortcutId: String, userHandle: Int) {
        try {
            if (hasShortcutHostPermission()) {
                val targetUser = launcherApps.profiles.find { it.hashCode() == userHandle }
                    ?: Process.myUserHandle()

                val query = LauncherApps.ShortcutQuery().apply {
                    setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                    setPackage(packageName)
                }
                val shortcuts = launcherApps.getShortcuts(query, targetUser)
                if (shortcuts != null) {
                    val remainingIds = shortcuts.map { it.id }.filter { it != shortcutId }
                    launcherApps.pinShortcuts(packageName, remainingIds, targetUser)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun mapToShortcutInfo(
        entities: List<ShortcutInfoEntity>
    ): List<ShortcutInfo> {
        val myUserHandle = getMyUserHandle()
        return entities.map {
            it.toShortcutInfo(myUserHandle)
        }
    }

    fun getShortcutsWithSearch(searchText: String, apps: List<ShortcutInfo>): List<ShortcutInfo> {
        if (searchText.isBlank()) return apps

        return apps.filter { appInfo ->
            appInfo.displayName.contains(searchText, ignoreCase = true)
        }
    }

    private fun ShortcutInfoEntity.toShortcutInfo(myUserHandle: Int): ShortcutInfo {
        return ShortcutInfo(
            packageName = packageName,
            shortcutId = shortcutId,
            userHandle = userHandle,
            shortcutName = shortcutName,
            alternateShortcutName = alternateShortcutName,
            isFavourite = isFavourite,
            isWorkProfile = userHandle != myUserHandle
        )
    }

    private fun getMyUserHandle() = Process.myUserHandle().hashCode()
}

data class InstalledShortcut(
    val appName: String,
    val packageName: String,
    val shortcutId: String,
    val userHandle: Int
) {
    val id: String
        get() = "$packageName-$shortcutId-$userHandle"
}