package com.minimo.launcher.data.usecase

import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.utils.AppUtils
import com.minimo.launcher.utils.InstalledApp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddUpdateAppsUseCase @Inject constructor(
    private val appUtils: AppUtils,
    private val appInfoDao: AppInfoDao
) {
    suspend operator fun invoke(packageName: String, userHandle: Int, checkAppRemoval: Boolean) {
        val installedApps = appUtils.getInstalledApps(packageName, userHandle)
        val dbApps = appInfoDao.getAppsByPackageName(packageName, userHandle)
        addUpdateAppsInDb(installedApps, dbApps)

        if (!checkAppRemoval) return

        // Remove the apps by className + packageName which exists in DB but not in installed apps
        // This could happen if any component was disabled or removed
        val installedAppIds = installedApps.map { it.id }
        val removedApps = dbApps.filterNot { installedAppIds.contains(it.id) }
        if (removedApps.isNotEmpty()) {
            appInfoDao.deleteAppsTransaction(removedApps)
        }
    }

    private suspend fun addUpdateAppsInDb(
        installedApps: List<InstalledApp>,
        dbApps: List<AppInfoEntity>
    ) {
        val dbAppsMap = dbApps.associateBy { it.id }
        val addApps = mutableListOf<AppInfoEntity>()

        for (installedApp in installedApps) {
            if (dbAppsMap.containsKey(installedApp.id)) {
                // Update the app if it exists in the database
                val dbApp = dbAppsMap[installedApp.id]
                if (dbApp != null) {
                    addApps.add(dbApp.copy(appName = installedApp.appName))
                }
            } else {
                // Add the app if it does not exists in the database
                addApps.add(
                    AppInfoEntity(
                        packageName = installedApp.packageName,
                        className = installedApp.className,
                        userHandle = installedApp.userHandle,
                        appName = installedApp.appName,
                        alternateAppName = "",
                        isFavourite = false,
                        isHidden = false,
                        orderIndex = 0
                    )
                )
            }
        }

        if (addApps.isNotEmpty()) {
            appInfoDao.addApps(addApps)
        }
    }
}