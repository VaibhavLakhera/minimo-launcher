package com.minimo.launcher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.ui.entities.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppUtils @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val privateSpaceManager: PrivateSpaceManager
) {
    fun getInstalledApps(): InstalledApps {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        val selfPackageName = context.packageName

        val installedApps = mutableListOf<InstalledApp>()
        // A locked private space cannot be listed reliably, so its saved apps are left as they are.
        val privateSpace = privateSpaceManager.refresh()
        val skippedProfiles = privateSpace.unlistableProfiles
        val listedProfiles = mutableSetOf<Int>()

        for (profile in userManager.userProfiles) {
            if (profile.hashCode() in skippedProfiles) continue
            listedProfiles += profile.hashCode()
            val activities = launcherApps.getActivityList(null, profile)
            for (activity in activities) {
                val appName = activity.label.toString()
                val appPackageName = activity.componentName.packageName
                val appClassName = activity.componentName.className

                /*
                * Ignore the self package name.
                * Add all user apps to the list.
                * */
                if (appPackageName.contains(selfPackageName, true).not() &&
                    !isPrivateSpaceSystemApp(appPackageName, profile.hashCode(), privateSpace)
                ) {
                    installedApps.add(
                        InstalledApp(
                            appName = appName,
                            packageName = appPackageName,
                            className = appClassName,
                            userHandle = profile.hashCode()
                        )
                    )
                }
            }
        }

        return InstalledApps(
            apps = installedApps,
            listedProfiles = listedProfiles,
            skippedProfiles = skippedProfiles,
            keepUnlistedProfiles = !privateSpace.hasHiddenProfileAccess
        )
    }

    /** Returns null when the profile cannot be listed right now, such as a locked private space. */
    fun getInstalledApps(packageName: String, userHandle: Int): List<InstalledApp>? {
        val privateSpace = privateSpaceManager.stateFor(userHandle)
        if (userHandle in privateSpace.unlistableProfiles) return null
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        // Without hidden profile access a missing profile may still exist, so its rows are kept.
        val profile = userManager.userProfiles.find { it.hashCode() == userHandle }
            ?: return if (privateSpace.hasHiddenProfileAccess) emptyList() else null

        val selfPackageName = context.packageName

        val installedApps = mutableListOf<InstalledApp>()

        val activities = launcherApps.getActivityList(packageName, profile)
        for (activity in activities) {
            val appName = activity.label.toString()
            val appPackageName = activity.componentName.packageName
            val appClassName = activity.componentName.className

            /*
            * Ignore the self package name.
            * Add all user apps to the list.
            * */
            if (appPackageName.contains(selfPackageName, true).not() &&
                !isPrivateSpaceSystemApp(appPackageName, userHandle, privateSpace)
            ) {
                installedApps.add(
                    InstalledApp(
                        appName = appName,
                        packageName = appPackageName,
                        className = appClassName,
                        userHandle = profile.hashCode()
                    )
                )
            }
        }

        return installedApps
    }

    /**
     * Apps for screens outside the drawer's private space section, such as Home, favourites and
     * gesture pickers. They stay visible while the private space is locked, so its apps are left out.
     */
    fun mapToMainAppInfo(entities: List<AppInfoEntity>): List<AppInfo> =
        mapToAppInfo(entities).filterNot { it.isPrivateSpace }

    fun mapToAppInfo(
        entities: List<AppInfoEntity>,
        notificationDots: List<NotificationDot> = emptyList(),
        privateSpaceState: PrivateSpaceState = privateSpaceManager.currentState()
    ): List<AppInfo> {
        return entities.map {
            it.toAppInfo(
                profileType = privateSpaceManager.profileTypeOf(it.userHandle, privateSpaceState),
                showNotificationDot = it.itemType == AppItemType.APP &&
                        notificationDots.any { notificationDot ->
                            notificationDot.packageName == it.packageName &&
                                    notificationDot.userHandle == it.userHandle
                        }
            )
        }
    }

    fun getAppsWithSearch(
        searchText: String,
        apps: List<AppInfo>,
        searchMode: SearchMode
    ): List<AppInfo> {
        if (searchText.isBlank()) return apps

        val matchesSearch = StringUtils.appSearchMatcher(searchText, searchMode)
        return apps.filter { appInfo -> matchesSearch(appInfo.name) }
    }

    // Android's own private space app is reached from the private space menu, not listed as an app.
    private fun isPrivateSpaceSystemApp(
        packageName: String,
        userHandle: Int,
        privateSpace: PrivateSpaceState
    ): Boolean = userHandle == privateSpace.userHandle &&
            packageName == PrivateSpaceManager.PRIVATE_SPACE_SYSTEM_PACKAGE

    private fun AppInfoEntity.toAppInfo(
        profileType: ProfileType,
        showNotificationDot: Boolean
    ): AppInfo {
        return AppInfo(
            packageName = packageName,
            itemType = itemType,
            targetId = targetId,
            userHandle = userHandle,
            appName = appName,
            alternateAppName = alternateAppName,
            isFavourite = isFavourite,
            isHidden = isHidden,
            profileType = profileType,
            showNotificationDot = showNotificationDot,
            orderIndex = orderIndex,
            launchDelaySeconds = launchDelaySeconds,
            folderId = folderId
        )
    }
}

data class InstalledApps(
    val apps: List<InstalledApp>,
    /** Profiles whose apps were listed, so their saved rows can be compared with [apps]. */
    val listedProfiles: Set<Int>,
    /** Profiles that exist but could not be listed, such as a locked private space. */
    val skippedProfiles: Set<Int>,
    /** Minimo could not see hidden profiles, so a profile missing from the list may still exist. */
    val keepUnlistedProfiles: Boolean
) {
    /**
     * Whether saved rows of [userHandle] can be synced against [apps]. Rows of a profile that was
     * not listed would otherwise look uninstalled.
     */
    fun canSyncProfile(userHandle: Int): Boolean =
        userHandle in listedProfiles || (!keepUnlistedProfiles && userHandle !in skippedProfiles)
}

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val className: String,
    val userHandle: Int
) {
    val id: String
        get() = "${AppItemType.APP.persistedValue}|$packageName|$className|$userHandle"
}
