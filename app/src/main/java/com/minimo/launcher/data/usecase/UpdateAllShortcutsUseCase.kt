package com.minimo.launcher.data.usecase

import android.content.pm.ShortcutInfo
import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.utils.InstalledShortcut
import com.minimo.launcher.utils.ShortcutInventory
import com.minimo.launcher.utils.ShortcutsUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAllShortcutsUseCase @Inject constructor(
    private val shortcutsUtils: ShortcutsUtils,
    private val appInfoDao: AppInfoDao
) {
    suspend operator fun invoke() {
        val dbShortcuts = appInfoDao.getItemsByType(AppItemType.SHORTCUT)
        val reconciliation = reconcilePinnedShortcuts(
            shortcutsUtils.getInstalledShortcuts(),
            dbShortcuts
        ) ?: return

        if (reconciliation.updates.isNotEmpty()) appInfoDao.addApps(reconciliation.updates)
        if (reconciliation.deletions.isNotEmpty()) {
            appInfoDao.deleteAppsTransaction(reconciliation.deletions)
        }
        if (reconciliation.additions.isNotEmpty()) appInfoDao.addApps(reconciliation.additions)
    }

    suspend fun addAcceptedShortcut(shortcut: ShortcutInfo) {
        val installedShortcut = shortcutsUtils.mapInstalledShortcut(shortcut)
        val existing = appInfoDao.getApp(
            itemType = AppItemType.SHORTCUT,
            targetId = installedShortcut.shortcutId,
            packageName = installedShortcut.packageName,
            userHandle = installedShortcut.userHandle
        )
        if (existing == null) {
            appInfoDao.addAppIfMissing(createShortcutEntity(installedShortcut))
        } else {
            appInfoDao.addApps(
                listOf(
                    existing.copy(
                        appName = installedShortcut.appName,
                        alternateAppName = if (existing.alternateAppName == existing.appName) {
                            ""
                        } else {
                            existing.alternateAppName
                        }
                    )
                )
            )
        }
    }

}

internal data class ShortcutReconciliation(
    val additions: List<AppInfoEntity>,
    val updates: List<AppInfoEntity>,
    val deletions: List<AppInfoEntity>
)

internal fun reconcilePinnedShortcuts(
    inventory: ShortcutInventory?,
    dbShortcuts: List<AppInfoEntity>
): ShortcutReconciliation? {
    if (inventory == null) return null

    val installedMap = inventory.shortcuts.associateBy { it.id }
    val updates = mutableListOf<AppInfoEntity>()
    val deletions = mutableListOf<AppInfoEntity>()

    for (dbShortcut in dbShortcuts) {
        val profileRemoved = dbShortcut.userHandle !in inventory.currentProfiles
        val profileQueried = dbShortcut.userHandle in inventory.successfulProfiles
        if (!profileRemoved && !profileQueried) continue

        val installedShortcut = installedMap[dbShortcut.id]
        if (installedShortcut == null) {
            deletions.add(dbShortcut)
        } else {
            updates.add(
                dbShortcut.copy(
                    appName = installedShortcut.appName,
                    alternateAppName = if (dbShortcut.alternateAppName == dbShortcut.appName) {
                        ""
                    } else {
                        dbShortcut.alternateAppName
                    }
                )
            )
        }
    }

    val dbIds = dbShortcuts.mapTo(mutableSetOf()) { it.id }
    val additions = inventory.shortcuts
        .filterNot { it.id in dbIds }
        .map(::createShortcutEntity)

    return ShortcutReconciliation(additions, updates, deletions)
}

private fun createShortcutEntity(shortcut: InstalledShortcut): AppInfoEntity {
    return AppInfoEntity(
        packageName = shortcut.packageName,
        itemType = AppItemType.SHORTCUT,
        targetId = shortcut.shortcutId,
        userHandle = shortcut.userHandle,
        appName = shortcut.appName,
        alternateAppName = "",
        isFavourite = false,
        isHidden = false,
        orderIndex = 0,
        launchDelaySeconds = 0
    )
}
