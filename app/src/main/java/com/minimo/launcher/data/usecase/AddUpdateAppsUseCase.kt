package com.minimo.launcher.data.usecase

import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.utils.AppIconRepository
import com.minimo.launcher.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddUpdateAppsUseCase @Inject constructor(
    private val appUtils: AppUtils,
    private val appInfoDao: AppInfoDao,
    private val preferenceHelper: PreferenceHelper,
    private val appIconRepository: AppIconRepository,
    private val appSyncMutex: AppSyncMutex
) {
    suspend operator fun invoke(
        packageName: String,
        userHandle: Int,
        removeMissing: Boolean
    ) = withContext(Dispatchers.IO) {
        appSyncMutex.withLock {
            val installedApps = appUtils.getInstalledApps(packageName, userHandle)
            val dbApps = appInfoDao.getAppsByPackageName(packageName, userHandle)
            val syncResult = syncInstalledApps(
                installedApps = installedApps,
                dbApps = dbApps,
                removeMissing = removeMissing
            )
            applyAppSync(syncResult, appInfoDao, preferenceHelper) {
                appIconRepository.removeIcon(packageName, userHandle)
            }
        }
    }
}
