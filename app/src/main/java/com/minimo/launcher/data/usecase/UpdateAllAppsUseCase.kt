package com.minimo.launcher.data.usecase

import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAllAppsUseCase @Inject constructor(
    private val appUtils: AppUtils,
    private val appInfoDao: AppInfoDao,
    private val preferenceHelper: PreferenceHelper,
    private val appSyncMutex: AppSyncMutex
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        appSyncMutex.withLock {
            val installedApps = appUtils.getInstalledApps()
            val dbApps = appInfoDao.getItemsByType(AppItemType.APP)
            val syncResult = syncInstalledApps(
                installedApps = installedApps,
                dbApps = dbApps,
                removeMissing = true
            )
            applyAppSync(syncResult, appInfoDao, preferenceHelper)
        }
    }
}
