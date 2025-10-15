package com.minimo.launcher.data.usecase

import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.utils.AppUtils
import com.minimo.launcher.utils.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAllAppsUseCase @Inject constructor(
    private val appUtils: AppUtils,
    private val appInfoDao: AppInfoDao
) {
    suspend operator fun invoke() {
        val installedApps = withContext(Dispatchers.IO) {
            appUtils.getInstalledApps()
        }
        val dbApps = appInfoDao.getAllApps()

        updateExistingAppsInDb(installedApps, dbApps)
        addNewAppsToDb(installedApps, dbApps)
    }

    private suspend fun updateExistingAppsInDb(
        installedApps: List<InstalledApp>,
        dbApps: List<AppInfoEntity>
    ) {
        val installedAppsMap = installedApps.associateBy { it.id }
        val addApps = mutableListOf<AppInfoEntity>()
        val deleteApps = mutableListOf<AppInfoEntity>()

        for (dbApp in dbApps) {
            if (installedAppsMap.containsKey(dbApp.id)) {
                // Update the app if it exists in the installed apps list
                val installedApp = installedAppsMap[dbApp.id]
                if (installedApp != null) {
                    addApps.add(
                        dbApp.copy(
                            appName = installedApp.appName,
                            alternateAppName = if (dbApp.appName == dbApp.alternateAppName) "" else dbApp.alternateAppName
                        )
                    )
                }
            } else {
                // Delete the app if it is not in the installed apps list
                deleteApps.add(dbApp)
            }
        }

        if (addApps.isNotEmpty()) {
            appInfoDao.addApps(addApps)
        }

        if (deleteApps.isNotEmpty()) {
            appInfoDao.deleteAppsTransaction(deleteApps)
        }
    }

    private suspend fun addNewAppsToDb(
        installedApps: List<InstalledApp>,
        dbApps: List<AppInfoEntity>
    ) {
        val dbAppsPackages = dbApps.map { it.id }
        val newApps = mutableListOf<AppInfoEntity>()

        for (installedApp in installedApps) {
            if (installedApp.id !in dbAppsPackages) {
                newApps.add(
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

        if (newApps.isNotEmpty()) {
            appInfoDao.addApps(newApps)
        }
    }
}