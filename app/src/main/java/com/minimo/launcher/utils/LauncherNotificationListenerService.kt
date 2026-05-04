package com.minimo.launcher.utils

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LauncherNotificationListenerService : NotificationListenerService() {
    @Inject
    lateinit var notificationDotsNotifier: NotificationDotsNotifier

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Cache to keep track of active notifications without making repeated IPC calls.
    // Maps (PackageName, UserHandle) -> Set of Notification Keys
    private val activeNotificationsCache = mutableMapOf<Pair<String, Int>, MutableSet<String>>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.d("onListenerConnected")
        syncNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.d("onListenerDisconnected")
        activeNotificationsCache.clear()
        notificationDotsNotifier.updateNotificationDots(emptyList())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val userHandle = sbn.user.hashCode()
        val key = sbn.key
        val cacheKey = Pair(packageName, userHandle)

        val isValid = sbn.isClearable && !sbn.isOngoing
        var changed = false

        if (isValid) {
            val keys = activeNotificationsCache[cacheKey]
            if (keys == null) {
                activeNotificationsCache[cacheKey] = mutableSetOf(key)
                changed = true
            } else {
                keys.add(key)
            }
        } else {
            val keys = activeNotificationsCache[cacheKey]
            if (keys != null && keys.remove(key)) {
                if (keys.isEmpty()) {
                    activeNotificationsCache.remove(cacheKey)
                    changed = true
                }
            }
        }

        if (changed) {
            notificationDotsNotifier.updateNotificationDots(
                activeNotificationsCache.keys.map { (pkg, user) -> NotificationDot(pkg, user) }
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val userHandle = sbn.user.hashCode()
        val key = sbn.key
        val cacheKey = Pair(packageName, userHandle)

        val keys = activeNotificationsCache[cacheKey]
        if (keys != null && keys.remove(key)) {
            if (keys.isEmpty()) {
                activeNotificationsCache.remove(cacheKey)
                notificationDotsNotifier.updateNotificationDots(
                    activeNotificationsCache.keys.map { (pkg, user) -> NotificationDot(pkg, user) }
                )
            }
        }
    }

    private fun syncNotifications() {
        serviceScope.launch {
            try {
                // activeNotifications makes a synchronous Binder IPC call to the System Server.
                // We fetch this result on the IO thread to prevent main thread blocking / UI jank.
                val notifications = withContext(Dispatchers.IO) {
                    activeNotifications
                } ?: return@launch

                // We switch back to the Main thread to update the cache.
                // This avoids race conditions with onNotificationPosted/Removed, which are invoked by the OS on the Main thread.
                activeNotificationsCache.clear()

                for (notification in notifications) {
                    val packageName = notification.packageName ?: continue
                    val userHandle = notification.user.hashCode()
                    if (notification.isClearable && !notification.isOngoing) {
                        val cacheKey = Pair(packageName, userHandle)
                        val keys = activeNotificationsCache[cacheKey]
                        if (keys == null) {
                            activeNotificationsCache[cacheKey] = mutableSetOf(notification.key)
                        } else {
                            keys.add(notification.key)
                        }
                    }
                }

                notificationDotsNotifier.updateNotificationDots(
                    activeNotificationsCache.keys.map { (pkg, user) -> NotificationDot(pkg, user) }
                )
            } catch (exception: Exception) {
                Timber.e(exception)
                activeNotificationsCache.clear()
                notificationDotsNotifier.updateNotificationDots(emptyList())
            }
        }
    }
}
