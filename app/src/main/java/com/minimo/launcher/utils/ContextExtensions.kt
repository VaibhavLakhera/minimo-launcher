package com.minimo.launcher.utils

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.minimo.launcher.R
import com.minimo.launcher.ui.entities.AppInfo
import timber.log.Timber

fun Context.launchApp(packageName: String, className: String, userHandleHashCode: Int) {
    val userManager = getSystemService(Context.USER_SERVICE) as UserManager
    val userHandle = userManager.userProfiles.find { it.hashCode() == userHandleHashCode }

    if (packageName.isBlank() || className.isBlank() || userHandle == null) return

    val launcher = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val component = ComponentName(packageName, className)

    try {
        launcher.startMainActivity(component, userHandle, null, null)
    } catch (exception: SecurityException) {
        Timber.e(exception)
        try {
            launcher.startMainActivity(component, Process.myUserHandle(), null, null)
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.launchAppFromPreference(pref: String, fallback: () -> Unit = {}) {
    if (pref.isBlank()) {
        fallback()
        return
    }
    val parts = pref.split("|")
    if (parts.size == 3) {
        val packageName = parts[0]
        val className = parts[1]
        val userHandle = parts[2].toIntOrNull()
        if (userHandle != null) {
            launchApp(packageName, className, userHandle)
            return
        }
    }
    fallback()
}

fun Context.uninstallApp(appInfo: AppInfo) {
    try {
        val userManager = getSystemService(Context.USER_SERVICE) as UserManager
        val targetUserHandle =
            userManager.userProfiles.find { it.hashCode() == appInfo.userHandle } ?: return

        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.fromParts("package", appInfo.packageName, appInfo.className)
        intent.putExtra(Intent.EXTRA_USER, targetUserHandle)
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.launchAppInfo(appInfo: AppInfo) {
    try {
        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val userManager = getSystemService(Context.USER_SERVICE) as UserManager

        val targetUserHandle =
            userManager.userProfiles.find { it.hashCode() == appInfo.userHandle } ?: return

        val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName) ?: return

        launcherApps.startAppDetailsActivity(intent.component, targetUserHandle, null, null)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openHomeSettings() {
    try {
        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openPlayStorePage(id: String = packageName) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://play.google.com/store/apps/details?id=${id}".toUri()
        }
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.isInstalledFromPlayStore(): Boolean {
    val playStorePackageName = "com.android.vending"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val info = packageManager.getInstallSourceInfo(packageName)
            info.installingPackageName == playStorePackageName ||
                    info.initiatingPackageName == playStorePackageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName) == playStorePackageName
        }
    } catch (exception: Exception) {
        Timber.e(exception)
        false
    }
}

fun Context.openDeveloperPlayStorePage() {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://play.google.com/store/apps/developer?id=Vaibhav+Lakhera".toUri()
        }
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openDiscordLink() {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://discord.gg/f4wpPppCDk".toUri()
        }
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openGithubLink() {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://github.com/VaibhavLakhera/minimo-launcher".toUri()
        }
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openKoFiPage() {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://ko-fi.com/vaibhavlakhera".toUri()
        }
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.sendFeedback() {
    try {
        val recipient = "vaibhav.lakhera.dev@gmail.com"
        val subject = "Minimo Launcher Feedback"
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Timber.e("No email app found to handle the intent.")
        }
    } catch (e: Exception) {
        Timber.e("Error sending email: ${e.message}")
    }
}

fun Context.lockScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        lockScreenWithAccessibility()
    } else {
        lockScreenWithReceiver()
    }
}

private fun Context.lockScreenWithAccessibility() {
    if (isAccessibilityEnabled()) {
        MinimoAccessibilityService.lockScreen()
    }
}

private fun Context.lockScreenWithReceiver() {
    val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val componentName = ComponentName(this, ScreenOffAdminReceiver::class.java)

    if (devicePolicyManager.isAdminActive(componentName)) {
        devicePolicyManager.lockNow()
    }
}

fun Context.hasLockScreenPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        isAccessibilityEnabled()
    } else {
        isAdminActive()
    }
}

private fun Context.isAccessibilityEnabled(): Boolean {
    var enabled = 0
    try {
        enabled = Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
    } catch (e: Settings.SettingNotFoundException) {
        Timber.e(e)
    }
    if (enabled == 1) {
        val name = ComponentName(applicationContext, MinimoAccessibilityService::class.java)
        val services = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return services?.contains(name.flattenToString()) ?: false
    }
    return false
}

private fun Context.isAdminActive(): Boolean {
    val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val componentName = ComponentName(this, ScreenOffAdminReceiver::class.java)
    return devicePolicyManager.isAdminActive(componentName)
}

fun Context.requestLockScreenPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        launchAccessibilitySettings(getString(R.string.enable_accessibility_permission_for_minimo))
    } else {
        requestEnableAdmin()
    }
}

private fun Context.launchAccessibilitySettings(toastMessage: String) {
    Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

fun Context.requestEnableAdmin() {
    val componentName = ComponentName(this, ScreenOffAdminReceiver::class.java)

    Toast.makeText(this, getString(R.string.enable_device_admin_first), Toast.LENGTH_SHORT).show()
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
    intent.putExtra(
        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
        getString(R.string.you_need_to_enable_admin_access_to_lock_the_screen)
    )
    startActivity(intent)
}

fun Context.removeLockScreenPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // Ignore
    } else {
        removeDeviceAdmin()
    }
}

private fun Context.removeDeviceAdmin() {
    try {
        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, ScreenOffAdminReceiver::class.java)
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.removeActiveAdmin(componentName)
        }
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.showNotificationDrawer() {
    try {
        Class.forName("android.app.StatusBarManager")
            .getMethod("expandNotificationsPanel")
            .apply { isAccessible = true }
            .invoke(getSystemService("statusbar"))
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openDefaultClockApp() {
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openDefaultCalendarApp() {
    if (!openDefaultCalendarAppOption1()) {
        openDefaultCalendarOption2()
    }
}

private fun Context.openDefaultCalendarAppOption1(): Boolean {
    try {
        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = builder.build()
        startActivity(intent)
        return true
    } catch (exception: Exception) {
        Timber.e(exception)
        return false
    }
}

private fun Context.openDefaultCalendarOption2() {
    try {
        val intent = Intent(
            Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_CALENDAR
            )
        )
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.openPowerUsageSummary() {
    try {
        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

fun Context.isNotificationPermissionGranted(): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
}

private const val EXTRA_FRAGMENT_HIGHLIGHT_KEY = ":settings:fragment_args_key"
private const val EXTRA_FRAGMENT_ARGS = ":settings:fragment_args"
fun Context.openNotificationSettings() {
    try {
        val componentName = ComponentName(this, LauncherNotificationListenerService::class.java)

        val showFragmentArgs = Bundle()
        showFragmentArgs.putString(
            EXTRA_FRAGMENT_HIGHLIGHT_KEY,
            componentName.flattenToString(),
        )

        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_FRAGMENT_HIGHLIGHT_KEY, componentName.flattenToString())
            .putExtra(EXTRA_FRAGMENT_ARGS, showFragmentArgs)

        startActivity(intent)
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.isAppUsagePermissionGranted(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

// Thanks to Olauncher -> https://github.com/tanujnotes/Olauncher
fun Context.openDigitalWellbeing() {
    val intent = Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        intent.setClassName(
            "com.google.android.apps.wellbeing",
            "com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity"
        )
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
        try {
            intent.setClassName(
                "com.samsung.android.forest",
                "com.samsung.android.forest.launcher.LauncherActivity"
            )
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

fun Context.openUsageAccessSettings() {
    try {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    } catch (exception: Exception) {
        Timber.e(exception)
    }
}