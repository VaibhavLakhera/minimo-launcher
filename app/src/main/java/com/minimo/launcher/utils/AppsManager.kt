package com.minimo.launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import com.minimo.launcher.data.usecase.AddUpdateAppsUseCase
import com.minimo.launcher.data.usecase.RemoveAppsUseCase
import com.minimo.launcher.data.usecase.UpdateAllAppsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class AppsManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val addUpdateAppsUseCase: AddUpdateAppsUseCase,
    private val removeAppsUseCase: RemoveAppsUseCase,
    private val updateAllAppsUseCase: UpdateAllAppsUseCase
) : LauncherApps.Callback() {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    private val managedProfileReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_MANAGED_PROFILE_ADDED,
                Intent.ACTION_MANAGED_PROFILE_REMOVED -> {
                    coroutineScope.launch {
                        mutex.withLock {
                            updateAllAppsUseCase.invoke()
                        }
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
        coroutineScope.launch {
            mutex.withLock {
                removeAppsUseCase.invoke(packageName, user.hashCode())
            }
        }
    }

    override fun onPackageAdded(packageName: String?, user: UserHandle?) {
        if (packageName == null || user == null) return
        coroutineScope.launch {
            mutex.withLock {
                addUpdateAppsUseCase.invoke(
                    packageName = packageName,
                    userHandle = user.hashCode(),
                    checkAppRemoval = false
                )
            }
        }
    }

    override fun onPackageChanged(packageName: String?, user: UserHandle?) {
        if (packageName == null || user == null) return
        coroutineScope.launch {
            mutex.withLock {
                addUpdateAppsUseCase.invoke(
                    packageName = packageName,
                    userHandle = user.hashCode(),
                    checkAppRemoval = true
                )
            }
        }
    }

    override fun onPackagesAvailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean
    ) {
        if (packageNames == null || user == null) return
        coroutineScope.launch {
            mutex.withLock {
                packageNames.forEach { packageName ->
                    addUpdateAppsUseCase.invoke(
                        packageName = packageName,
                        userHandle = user.hashCode(),
                        checkAppRemoval = false
                    )
                }
            }
        }
    }

    override fun onPackagesUnavailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean
    ) {
        if (packageNames == null || user == null) return
        coroutineScope.launch {
            mutex.withLock {
                packageNames.forEach { packageName ->
                    removeAppsUseCase.invoke(
                        packageName = packageName,
                        userHandle = user.hashCode()
                    )
                }
            }
        }
    }

    override fun onPackagesUnsuspended(packageNames: Array<out String>?, user: UserHandle?) {
        if (packageNames == null || user == null) return
        coroutineScope.launch {
            mutex.withLock {
                packageNames.forEach { packageName ->
                    addUpdateAppsUseCase.invoke(
                        packageName = packageName,
                        userHandle = user.hashCode(),
                        checkAppRemoval = false
                    )
                }
            }
        }
    }

    fun unregisterCallback() {
        launcherApps.unregisterCallback(this)
        context.unregisterReceiver(managedProfileReceiver)
        coroutineScope.cancel()
    }
}