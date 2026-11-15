package com.minimo.launcher.utils

import android.app.Activity
import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.LauncherApps
import android.content.pm.LauncherUserInfo
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.minimo.launcher.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class ProfileType { MAIN, WORK, PRIVATE }

data class PrivateSpaceState(
    /** False only for the placeholder published before the first read finishes. */
    val isLoaded: Boolean = false,
    /** Hash code of the private profile's UserHandle, or null when Minimo cannot access one. */
    val userHandle: Int? = null,
    val isLocked: Boolean = true,
    /** The user chose "Hide private space when locked" in system settings (Android 16+). */
    val isEntryHidden: Boolean = false,
    val profileTypes: Map<Int, ProfileType> = emptyMap(),
    /**
     * Profiles whose apps cannot be listed right now (a locked private space, or a hidden profile
     * Minimo cannot access). App sync must leave their saved rows untouched.
     */
    val unlistableProfiles: Set<Int> = emptySet(),
    /** The unlocked private space has Android's "Add files" shortcut (Android 16 QPR2+). */
    val canAddFiles: Boolean = false,
    /**
     * Minimo can see every profile. Without this (Android 15+ while another launcher is the
     * default), a hidden profile is missing from every list, so a missing profile is not proof
     * that it was removed.
     */
    val hasHiddenProfileAccess: Boolean = true
) {
    val isAvailable: Boolean get() = userHandle != null
    val isUnlocked: Boolean get() = isAvailable && !isLocked
}

/**
 * Tracks the Android 15+ private space profile. Android only exposes it to the default launcher
 * holding ACCESS_HIDDEN_PROFILES, so every lookup here can fail and must fall back to hiding it.
 */
@Singleton
class PrivateSpaceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)
    private val myUserHandle = Process.myUserHandle().hashCode()

    private val _state = MutableStateFlow(PrivateSpaceState())
    val state: StateFlow<PrivateSpaceState> = _state.asStateFlow()

    /** [state] without the placeholder, for screens that would otherwise briefly misplace apps. */
    val loadedState: Flow<PrivateSpaceState> = state.filter { it.isLoaded }

    init {
        // The first read makes several system calls. It is created on the main thread, so read in
        // the background; anything that needs the result before then waits in [currentState].
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { currentState() }
    }

    /** The latest state, reading it first if the startup read has not finished yet. */
    fun currentState(): PrivateSpaceState {
        val state = _state.value
        if (state.isLoaded) return state
        return synchronized(this) { _state.value.takeIf { it.isLoaded } ?: refresh() }
    }

    /**
     * Re-reads the private space from the system, publishes it, and returns the new state.
     *
     * Callers arrive on the main thread and on sync threads. Reading and publishing under one lock
     * stops an older read from replacing a newer one, which could show a locked space as unlocked.
     */
    @Synchronized
    fun refresh(): PrivateSpaceState {
        val newState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            runCatching { readState() }
                .onFailure { Timber.e(it, "Unable to read private space state") }
                .getOrElse { failedReadState() }
        } else {
            PrivateSpaceState(isLoaded = true)
        }
        _state.value = newState
        return newState
    }

    /**
     * The state to check before listing [userHandle]'s apps. The main profile is always listable,
     * so its frequent package events skip the system calls that [refresh] makes.
     */
    fun stateFor(userHandle: Int): PrivateSpaceState =
        if (userHandle == myUserHandle) currentState() else refresh()

    fun profileTypeOf(userHandle: Int, state: PrivateSpaceState = currentState()): ProfileType {
        if (userHandle == myUserHandle) return ProfileType.MAIN
        state.profileTypes[userHandle]?.let { return it }
        // Before Android 15 every other profile is a work profile, as Minimo always assumed. Later,
        // an unknown profile could be a hidden one, so it stays out of sight until a sync removes it.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ProfileType.PRIVATE
        } else {
            ProfileType.WORK
        }
    }

    /**
     * Returns true when no credential is needed; otherwise Android shows its unlock screen.
     * Runs off the main thread, as the system starts the private profile before returning.
     */
    suspend fun unlock(): Boolean = requestQuietMode(enabled = false)

    /** Runs off the main thread, as the system stops the private profile before returning. */
    suspend fun lock(): Boolean = requestQuietMode(enabled = true)

    fun openInstallApps(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val user = privateUser() ?: return
        runCatching {
            val sender = launcherApps.getAppMarketActivityIntent(null, user)
                ?: throw ActivityNotFoundException()
            startIntentSender(activity, sender)
        }.onFailure {
            // Android finds no store when none is installed or set up in the private space.
            Timber.e(it, "Unable to open the app store for private space")
            Toast.makeText(activity, R.string.private_space_no_app_store, Toast.LENGTH_SHORT).show()
        }
    }

    /** Opens Android's "Add files" screen, which copies or moves files into the private space. */
    fun openAddFiles() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val user = privateUser() ?: return
        runCatching {
            launcherApps.startShortcut(
                PRIVATE_SPACE_SYSTEM_PACKAGE, ADD_FILES_SHORTCUT_ID, null, null, user
            )
        }.onFailure { Timber.e(it, "Unable to open add files for private space") }
    }

    fun openSettings(activity: Activity) {
        runCatching {
            val sender = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                launcherApps.privateSpaceSettingsIntent
            } else {
                null
            }
            if (sender != null) {
                startIntentSender(activity, sender)
            } else {
                // Android 15 has no public entry point for the private space page itself. It lives
                // under Security & privacy, so open that instead.
                activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            }
        }.onFailure { Timber.e(it, "Unable to open private space settings") }
    }

    /**
     * Used when the system could not be read. Any profile other than the main one could be a
     * locked private space, so sync must keep all of their saved rows until a read succeeds.
     */
    private fun failedReadState(): PrivateSpaceState {
        val previous = _state.value
        val otherProfiles = runCatching { userManager.userProfiles }
            .getOrDefault(emptyList())
            .map { it.hashCode() }
            .filter { it != myUserHandle }
        return previous.copy(
            isLoaded = true,
            unlistableProfiles = previous.unlistableProfiles + otherProfiles,
            hasHiddenProfileAccess = false
        )
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun readState(): PrivateSpaceState {
        // LauncherApps hides inaccessible hidden profiles, which UserManager still lists.
        val accessibleProfiles = launcherApps.profiles.toSet()
        val knownTypes = _state.value.profileTypes
        val profileTypes = mutableMapOf<Int, ProfileType>()
        val unlistable = mutableSetOf<Int>()
        var privateProfile: UserHandle? = null
        var privateInfo: LauncherUserInfo? = null

        for (profile in userManager.userProfiles) {
            val handle = profile.hashCode()
            if (handle == myUserHandle) {
                profileTypes[handle] = ProfileType.MAIN
                continue
            }
            if (profile !in accessibleProfiles) {
                profileTypes[handle] = ProfileType.PRIVATE
                unlistable += handle
                continue
            }
            val info = runCatching { launcherApps.getLauncherUserInfo(profile) }.getOrNull()
            when {
                info?.userType == UserManager.USER_TYPE_PROFILE_PRIVATE -> {
                    profileTypes[handle] = ProfileType.PRIVATE
                    privateProfile = profile
                    privateInfo = info
                }

                info != null || knownTypes[handle] == ProfileType.WORK -> {
                    profileTypes[handle] = ProfileType.WORK
                }

                // Android would not describe this profile, so it could be the private space. Hide
                // it and keep its saved rows, unless an earlier read already saw a work profile.
                else -> {
                    profileTypes[handle] = ProfileType.PRIVATE
                    unlistable += handle
                }
            }
        }

        val isLocked = privateProfile?.let { userManager.isQuietModeEnabled(it) } ?: true
        privateProfile?.let { if (isLocked) unlistable += it.hashCode() }
        val isEntryHidden = privateInfo != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
                privateInfo.userConfig.getBoolean(
                    LauncherUserInfo.PRIVATE_SPACE_ENTRYPOINT_HIDDEN,
                    false
                )

        val canAddFiles = privateProfile?.takeIf { !isLocked }?.let { profile ->
            runCatching { hasAddFilesShortcut(profile) }.getOrDefault(false)
        } ?: false

        return PrivateSpaceState(
            isLoaded = true,
            userHandle = privateProfile?.hashCode(),
            isLocked = isLocked,
            isEntryHidden = isEntryHidden,
            profileTypes = profileTypes,
            unlistableProfiles = unlistable,
            canAddFiles = canAddFiles,
            hasHiddenProfileAccess = isDefaultLauncher()
        )
    }

    // Android grants ACCESS_HIDDEN_PROFILES only to the launcher that holds the home role.
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isDefaultLauncher(): Boolean = runCatching {
        context.getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_HOME)
    }.getOrDefault(false)

    // The app's launcher activity alone only opens private space settings; the shortcut adds files.
    private fun hasAddFilesShortcut(user: UserHandle): Boolean {
        if (!launcherApps.hasShortcutHostPermission()) return false
        val query = LauncherApps.ShortcutQuery()
            .setPackage(PRIVATE_SPACE_SYSTEM_PACKAGE)
            .setShortcutIds(listOf(ADD_FILES_SHORTCUT_ID))
            .setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST)
        return launcherApps.getShortcuts(query, user).orEmpty().any { it.isEnabled }
    }

    private fun privateUser(): UserHandle? {
        val handle = currentState().userHandle ?: return null
        return runCatching { userManager.userProfiles.firstOrNull { it.hashCode() == handle } }
            .getOrNull()
    }

    private suspend fun requestQuietMode(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return@withContext false
        val user = privateUser() ?: return@withContext false
        runCatching { userManager.requestQuietModeEnabled(enabled, user) }
            .onFailure { Timber.e(it, "Unable to change private space lock state") }
            .getOrDefault(false)
    }

    private fun startIntentSender(activity: Activity, sender: IntentSender) {
        activity.startIntentSender(sender, null, 0, 0, 0, backgroundStartOptions())
    }

    // The system creates these senders; opting in lets Android start them from Minimo's foreground.
    @Suppress("DEPRECATION")
    private fun backgroundStartOptions(): Bundle? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
        return ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            )
            .toBundle()
    }

    companion object {
        /**
         * Android's own private space app. Its launcher activity only forwards to private space
         * settings, so Minimo leaves it out of the app list and offers its shortcuts from the
         * private space menu instead.
         */
        const val PRIVATE_SPACE_SYSTEM_PACKAGE = "com.android.privatespace"

        /** The app's manifest shortcut that copies or moves files in, as Pixel Launcher's Add does. */
        private const val ADD_FILES_SHORTCUT_ID = "shortcut-add-files"
    }
}
