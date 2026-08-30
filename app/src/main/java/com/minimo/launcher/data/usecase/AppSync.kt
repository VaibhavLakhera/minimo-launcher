package com.minimo.launcher.data.usecase

import com.minimo.launcher.data.AppInfoDao
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.ui.entities.AppPreferenceTarget
import com.minimo.launcher.utils.InstalledApp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal data class AppIdentityReplacement(
    val previous: AppInfoEntity,
    val replacement: AppInfoEntity
)

internal data class AppSyncResult(
    val updates: List<AppInfoEntity>,
    val additions: List<AppInfoEntity>,
    val replacements: List<AppIdentityReplacement>,
    val deletions: List<AppInfoEntity>
) {
    val hasChanges: Boolean
        get() = updates.isNotEmpty() || additions.isNotEmpty() ||
                replacements.isNotEmpty() || deletions.isNotEmpty()
}

/**
 * Syncs Android's current launcher activities with Minimo's saved app rows.
 *
 * Launcher icon switching is commonly implemented by disabling one activity alias and enabling
 * another. Transfer state only when the package/profile has one launcher entry before and after
 * the change. Multi-entry packages keep exact matches, but never transfer state between components.
 */
internal fun syncInstalledApps(
    installedApps: List<InstalledApp>,
    dbApps: List<AppInfoEntity>,
    removeMissing: Boolean
): AppSyncResult {
    val installedById = installedApps.associateBy { it.id }
    val dbIds = dbApps.mapTo(mutableSetOf()) { it.id }

    val updates = dbApps.mapNotNull { dbApp ->
        installedById[dbApp.id]?.let { installedApp ->
            dbApp.withInstalledIdentity(installedApp).takeUnless { it == dbApp }
        }
    }

    val unmatchedInstalled = installedApps.filterNot { it.id in dbIds }
    val unmatchedDb = if (removeMissing) {
        dbApps.filterNot { it.id in installedById }
    } else {
        emptyList()
    }

    val replacements = mutableListOf<AppIdentityReplacement>()
    val replacedInstalledIds = mutableSetOf<String>()
    val replacedDbIds = mutableSetOf<String>()
    if (removeMissing) {
        val installedByPackageAndProfile = installedApps.groupBy(InstalledApp::packageProfile)
        val dbByPackageAndProfile = dbApps.groupBy(AppInfoEntity::packageProfile)

        (installedByPackageAndProfile.keys intersect dbByPackageAndProfile.keys).forEach { key ->
            val installedApp = installedByPackageAndProfile.getValue(key).singleOrNull()
            val dbApp = dbByPackageAndProfile.getValue(key).singleOrNull()
            if (installedApp != null && dbApp != null && installedApp.id != dbApp.id) {
                replacements += AppIdentityReplacement(
                    previous = dbApp,
                    replacement = dbApp.withInstalledIdentity(installedApp)
                )
                replacedInstalledIds += installedApp.id
                replacedDbIds += dbApp.id
            }
        }
    }

    return AppSyncResult(
        updates = updates,
        additions = unmatchedInstalled
            .filterNot { it.id in replacedInstalledIds }
            .map(::createAppEntity),
        replacements = replacements,
        deletions = unmatchedDb.filterNot { it.id in replacedDbIds }
    )
}

internal suspend fun applyAppSync(
    syncResult: AppSyncResult,
    appInfoDao: AppInfoDao,
    preferenceHelper: PreferenceHelper,
    onSynced: () -> Unit = {}
) {
    currentCoroutineContext().ensureActive()
    // Once committing starts, cancellation must not separate preference remapping, Room, and icon
    // refresh. This does not make DataStore and Room atomic across process death or storage failure.
    withContext(NonCancellable) {
        if (syncResult.hasChanges) {
            preferenceHelper.remapAppPreferences(
                syncResult.replacements.associate { replacement ->
                    replacement.previous.toPreferenceTarget() to
                            replacement.replacement.toPreferenceTarget()
                }
            )

            appInfoDao.syncAppsTransaction(
                updates = syncResult.updates,
                additions = syncResult.additions,
                replacements = syncResult.replacements.associate { it.previous to it.replacement },
                deletedApps = syncResult.deletions
            )
        }
        // Icons can change even when every component and label still matches.
        onSynced()
    }
}

private fun AppInfoEntity.withInstalledIdentity(installedApp: InstalledApp): AppInfoEntity {
    return copy(
        targetId = installedApp.className,
        appName = installedApp.appName,
        alternateAppName = if (alternateAppName == appName) "" else alternateAppName
    )
}

private fun createAppEntity(installedApp: InstalledApp): AppInfoEntity {
    return AppInfoEntity(
        packageName = installedApp.packageName,
        itemType = AppItemType.APP,
        targetId = installedApp.className,
        userHandle = installedApp.userHandle,
        appName = installedApp.appName,
        alternateAppName = "",
        isFavourite = false,
        isHidden = false,
        orderIndex = 0,
        launchDelaySeconds = 0
    )
}

private fun AppInfoEntity.toPreferenceTarget(): AppPreferenceTarget {
    return AppPreferenceTarget(
        itemType = itemType,
        packageName = packageName,
        targetId = targetId,
        userHandle = userHandle
    )
}

private val InstalledApp.packageProfile: Pair<String, Int>
    get() = packageName to userHandle

private val AppInfoEntity.packageProfile: Pair<String, Int>
    get() = packageName to userHandle
