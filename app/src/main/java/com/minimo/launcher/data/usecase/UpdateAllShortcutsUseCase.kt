package com.minimo.launcher.data.usecase

import com.minimo.launcher.data.ShortcutInfoDao
import com.minimo.launcher.data.entities.ShortcutInfoEntity
import com.minimo.launcher.utils.InstalledShortcut
import com.minimo.launcher.utils.ShortcutsUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAllShortcutsUseCase @Inject constructor(
    private val shortcutsUtils: ShortcutsUtils,
    private val shortcutInfoDao: ShortcutInfoDao
) {
    suspend operator fun invoke() {
        val installedShortcuts = shortcutsUtils.getInstalledShortcuts()
        val dbApps = shortcutInfoDao.getAllShortcutsFlow().first()

        updateExistingAppsInDb(installedShortcuts, dbApps)
        addNewAppsToDb(installedShortcuts, dbApps)
    }

    private suspend fun updateExistingAppsInDb(
        installedApps: List<InstalledShortcut>,
        dbApps: List<ShortcutInfoEntity>
    ) {
        val installedAppsMap = installedApps.associateBy { it.id }
        val addApps = mutableListOf<ShortcutInfoEntity>()
        val deleteApps = mutableListOf<ShortcutInfoEntity>()

        for (dbApp in dbApps) {
            if (installedAppsMap.containsKey(dbApp.id)) {
                val installedApp = installedAppsMap[dbApp.id]
                if (installedApp != null) {
                    addApps.add(
                        dbApp.copy(
                            shortcutName = installedApp.appName,
                            alternateShortcutName = if (dbApp.shortcutName == dbApp.alternateShortcutName) "" else dbApp.alternateShortcutName
                        )
                    )
                }
            } else {
                deleteApps.add(dbApp)
            }
        }

        if (addApps.isNotEmpty()) {
            shortcutInfoDao.addShortcuts(addApps)
        }

        if (deleteApps.isNotEmpty()) {
            shortcutInfoDao.deleteShortcutsTransaction(deleteApps)
        }
    }

    private suspend fun addNewAppsToDb(
        installedApps: List<InstalledShortcut>,
        dbApps: List<ShortcutInfoEntity>
    ) {
        val dbAppsIds = dbApps.map { it.id }
        val newApps = mutableListOf<ShortcutInfoEntity>()

        for (installedApp in installedApps) {
            if (installedApp.id !in dbAppsIds) {
                newApps.add(
                    ShortcutInfoEntity(
                        packageName = installedApp.packageName,
                        shortcutId = installedApp.shortcutId,
                        userHandle = installedApp.userHandle,
                        shortcutName = installedApp.appName,
                        alternateShortcutName = "",
                        isFavourite = false
                    )
                )
            }
        }

        if (newApps.isNotEmpty()) {
            shortcutInfoDao.addShortcuts(newApps)
        }
    }
}