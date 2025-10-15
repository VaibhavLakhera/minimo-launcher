package com.minimo.launcher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserManager
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.ui.entities.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppUtils @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    fun getInstalledApps(): List<InstalledApp> {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        val selfPackageName = context.packageName

        val installedApps = mutableListOf<InstalledApp>()

        for (profile in userManager.userProfiles) {
            val activities = launcherApps.getActivityList(null, profile)
            for (activity in activities) {
                val appName = activity.label.toString()
                val appPackageName = activity.componentName.packageName
                val appClassName = activity.componentName.className

                /*
                * Ignore the self package name.
                * Add all user apps to the list.
                * */
                if (appPackageName.contains(selfPackageName, true).not()) {
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

        return installedApps
    }

    fun getInstalledApps(packageName: String, userHandle: Int): List<InstalledApp> {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val profile = userManager.userProfiles.find { it.hashCode() == userHandle }
        if (profile == null) return emptyList()

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
            if (appPackageName.contains(selfPackageName, true).not()) {
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

    fun mapToAppInfo(
        entities: List<AppInfoEntity>,
        notificationDots: List<NotificationDot> = emptyList()
    ): List<AppInfo> {
        val myUserHandle = getMyUserHandle()
        return entities.map {
            it.toAppInfo(
                myUserHandle = myUserHandle,
                showNotificationDot = notificationDots.any { notificationDot ->
                    notificationDot.packageName == it.packageName && notificationDot.userHandle == it.userHandle
                }
            )
        }
    }

    fun getAppsWithSearch(searchText: String, apps: List<AppInfo>): List<AppInfo> {
        if (searchText.isBlank()) return apps

        return apps.filter { appInfo ->
            appInfo.name.contains(searchText, ignoreCase = true)
        }
    }

    private fun AppInfoEntity.toAppInfo(myUserHandle: Int, showNotificationDot: Boolean): AppInfo {
        return AppInfo(
            packageName = packageName,
            className = className,
            userHandle = userHandle,
            appName = appName,
            alternateAppName = alternateAppName,
            isFavourite = isFavourite,
            isHidden = isHidden,
            isWorkProfile = userHandle != myUserHandle,
            showNotificationDot = showNotificationDot,
            orderIndex = orderIndex
        )
    }

    private fun getMyUserHandle() = Process.myUserHandle().hashCode()
}

fun List<AppInfo>.updateNotificationDots(notificationDots: List<NotificationDot>): List<AppInfo> {
    return map { appInfo ->
        appInfo.copy(
            showNotificationDot = notificationDots.any { notificationDot ->
                notificationDot.packageName == appInfo.packageName && notificationDot.userHandle == appInfo.userHandle
            }
        )
    }
}

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val className: String,
    val userHandle: Int
) {
    val id: String
        get() = packageName + className + userHandle
}