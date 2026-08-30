package com.minimo.launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import com.minimo.launcher.data.usecase.AddUpdateAppsUseCase
import com.minimo.launcher.data.usecase.RemoveAppsUseCase
import com.minimo.launcher.data.usecase.UpdateAllAppsUseCase
import com.minimo.launcher.data.usecase.UpdateAllShortcutsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class AppsManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val addUpdateAppsUseCase: AddUpdateAppsUseCase,
    private val removeAppsUseCase: RemoveAppsUseCase,
    private val updateAllAppsUseCase: UpdateAllAppsUseCase,
    private val updateAllShortcutsUseCase: UpdateAllShortcutsUseCase,
    private val appIconRepository: AppIconRepository
) : LauncherApps.Callback() {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Component changes are debounced independently for each profile so unrelated packages do not
    // cancel one another. The lock only protects the job map from callback/coroutine races.
    private val packageChangeJobs = mutableMapOf<PackageProfile, Job>()
    private val packageChangeJobsLock = Any()

    private val managedProfileReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_MANAGED_PROFILE_ADDED,
                Intent.ACTION_MANAGED_PROFILE_REMOVED -> {
                    appIconRepository.clear()
                    coroutineScope.launch {
                        updateAllAppsUseCase.invoke()
                        updateAllShortcutsUseCase.invoke()
                    }
                }
            }
        }
    }

    fun registerCallback() {
        launcherApps.registerCallback(this, Handler(Looper.getMainLooper()))

        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
            addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
        }
        context.registerReceiver(managedProfileReceiver, intentFilter)
    }

    override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
        if (packageName == null || user == null) return
        val userHandle = user.hashCode()
        // A true removal must win over any delayed component sync for this package/profile.
        cancelPendingPackageChange(packageName, userHandle)
        appIconRepository.removeIcon(packageName, userHandle)
        coroutineScope.launch {
            removeAppsUseCase.invoke(packageName, userHandle)
        }
    }

    override fun onPackageAdded(packageName: String?, user: UserHandle?) {
        if (packageName == null || user == null) return
        val userHandle = user.hashCode()
        cancelPendingPackageChange(packageName, userHandle)
        coroutineScope.launch {
            addUpdateAppsUseCase.invoke(
                packageName = packageName,
                userHandle = userHandle,
                removeMissing = false
            )
        }
    }

    override fun onPackageChanged(packageName: String?, user: UserHandle?) {
        if (packageName == null || user == null) return
        val userHandle = user.hashCode()
        // Icon switching can enable and disable launcher aliases in consecutive callbacks. Waiting
        // briefly lets one authoritative sync observe the final component set.
        schedulePackageChange(packageName, userHandle)
    }

    override fun onPackagesAvailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean
    ) {
        if (packageNames == null || user == null) return
        val userHandle = user.hashCode()
        packageNames.forEach { packageName ->
            cancelPendingPackageChange(packageName, userHandle)
        }
        coroutineScope.launch {
            packageNames.forEach { packageName ->
                addUpdateAppsUseCase.invoke(
                    packageName = packageName,
                    userHandle = userHandle,
                    removeMissing = replacing
                )
            }
        }
    }

    override fun onPackagesUnavailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean
    ) {
        if (packageNames == null || user == null) return
        val userHandle = user.hashCode()
        packageNames.forEach { packageName ->
            cancelPendingPackageChange(packageName, userHandle)
            appIconRepository.removeIcon(packageName, userHandle)
        }
        // Package replacement is temporary; keep saved state until the available callback can sync
        // against the new version. Non-replacement unavailability keeps the existing behavior.
        if (replacing) return

        coroutineScope.launch {
            packageNames.forEach { packageName ->
                removeAppsUseCase.invoke(
                    packageName = packageName,
                    userHandle = userHandle
                )
            }
        }
    }

    override fun onPackagesUnsuspended(packageNames: Array<out String>?, user: UserHandle?) {
        if (packageNames == null || user == null) return
        coroutineScope.launch {
            packageNames.forEach { packageName ->
                addUpdateAppsUseCase.invoke(
                    packageName = packageName,
                    userHandle = user.hashCode(),
                    removeMissing = false
                )
            }
        }
    }

    override fun onShortcutsChanged(
        packageName: String,
        shortcuts: MutableList<ShortcutInfo>,
        user: UserHandle
    ) {
        appIconRepository.removeIcon(packageName, user.hashCode())
        coroutineScope.launch {
            updateAllShortcutsUseCase.invoke()
        }
    }

    fun unregisterCallback() {
        launcherApps.unregisterCallback(this)
        context.unregisterReceiver(managedProfileReceiver)
        coroutineScope.cancel()
    }

    private fun schedulePackageChange(packageName: String, userHandle: Int) {
        val key = PackageProfile(packageName, userHandle)
        val job = coroutineScope.launch(start = CoroutineStart.LAZY) {
            try {
                delay(PACKAGE_CHANGE_DEBOUNCE_MILLIS.milliseconds)
                // The use case invalidates icons once after saving the final identity, even if
                // only the icon changed. A newer callback cannot cancel that commit halfway.
                addUpdateAppsUseCase.invoke(
                    packageName = packageName,
                    userHandle = userHandle,
                    removeMissing = true
                )
            } finally {
                val currentJob = coroutineContext[Job]
                synchronized(packageChangeJobsLock) {
                    if (packageChangeJobs[key] === currentJob) {
                        packageChangeJobs.remove(key)
                    }
                }
            }
        }

        synchronized(packageChangeJobsLock) {
            packageChangeJobs.put(key, job)?.cancel()
        }
        job.start()
    }

    private fun cancelPendingPackageChange(packageName: String, userHandle: Int) {
        val job = synchronized(packageChangeJobsLock) {
            packageChangeJobs.remove(PackageProfile(packageName, userHandle))
        }
        job?.cancel()
    }

    private data class PackageProfile(
        val packageName: String,
        val userHandle: Int
    )

    companion object {
        private const val PACKAGE_CHANGE_DEBOUNCE_MILLIS = 500L
    }
}
