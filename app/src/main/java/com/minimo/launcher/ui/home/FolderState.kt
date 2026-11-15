package com.minimo.launcher.ui.home

import androidx.annotation.StringRes
import com.minimo.launcher.R
import com.minimo.launcher.data.FolderError
import com.minimo.launcher.data.entities.FolderEntity
import com.minimo.launcher.ui.entities.AppInfo

val FolderError.messageRes: Int
    @StringRes get() = when (this) {
        FolderError.INVALID_NAME -> R.string.folder_invalid_name
        FolderError.DUPLICATE_NAME -> R.string.folder_duplicate_name
        FolderError.UNAVAILABLE -> R.string.folder_unavailable
        FolderError.SAVE_FAILED -> R.string.folder_save_failed
    }

data class FolderInfo(
    val folder: FolderEntity,
    val apps: List<AppInfo>,
    // Session-only UI state: follows the folder between Home and the drawer, never saved to Room.
    val isExpanded: Boolean = false
) {
    val showNotificationDot: Boolean get() = apps.any { it.showNotificationDot }
}

sealed interface FolderDialog {
    data class Picker(val app: AppInfo, val fromHome: Boolean) : FolderDialog
    data class Create(val app: AppInfo, val fromHome: Boolean) : FolderDialog
    data class Rename(val folder: FolderEntity) : FolderDialog
    data class Delete(val folder: FolderEntity) : FolderDialog
}

/** The private space row's contents. It looks like a folder but is not saved as one. */
data class PrivateSpaceSection(
    val isLocked: Boolean,
    val isExpanded: Boolean,
    val apps: List<AppInfo>,
    val canAddFiles: Boolean = false
) {
    val showNotificationDot: Boolean get() = !isLocked && apps.any { it.showNotificationDot }
}

/** Parent ID of the private space's member rows. Folder IDs are UUIDs, so this cannot clash. */
const val PRIVATE_SPACE_ROW_ID = "private_space"

sealed interface LauncherRow {
    val key: String

    data class Folder(val info: FolderInfo) : LauncherRow {
        override val key = "folder:${info.folder.id}"
    }

    data class App(val app: AppInfo, val parentFolderId: String? = null) : LauncherRow {
        override val key =
            if (parentFolderId == null) "app:${app.id}" else "member:$parentFolderId:${app.id}"
    }

    data class Empty(val folderId: String) : LauncherRow {
        override val key = "empty:$folderId"
    }

    data class PrivateSpace(val section: PrivateSpaceSection) : LauncherRow {
        override val key = PRIVATE_SPACE_ROW_ID
    }
}

internal fun projectFolders(
    folders: List<FolderEntity>,
    apps: List<AppInfo>,
    previousFolders: List<FolderInfo>
): List<FolderInfo> {
    val members = apps.filter { !it.isHidden && it.folderId != null }.groupBy { it.folderId }
    // Database and notification updates rebuild the contents without collapsing existing folders.
    val previousById = previousFolders.associateBy { it.folder.id }
    return folders.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }).map { folder ->
        FolderInfo(
            folder = folder,
            apps = members[folder.id].orEmpty().sortedWith(
                compareBy<AppInfo, String>(String.CASE_INSENSITIVE_ORDER) { it.name }.thenBy { it.id }
            ),
            isExpanded = previousById[folder.id]?.isExpanded ?: false
        )
    }
}

internal fun launcherRows(
    folders: List<FolderInfo>,
    apps: List<AppInfo>,
    privateSpace: PrivateSpaceSection? = null
): List<LauncherRow> = buildList {
    // Keep children as separate lazy-list rows so expansion animates and scroll indices stay exact.
    folders.forEach { info ->
        add(LauncherRow.Folder(info))
        if (info.isExpanded) {
            if (info.apps.isEmpty()) add(LauncherRow.Empty(info.folder.id))
            info.apps.forEach { add(LauncherRow.App(it, info.folder.id)) }
        }
    }
    // Always after the user's folders and before the app list, so the fast scroller offset counts it.
    if (privateSpace != null) {
        add(LauncherRow.PrivateSpace(privateSpace))
        if (privateSpace.isExpanded && !privateSpace.isLocked) {
            if (privateSpace.apps.isEmpty()) add(LauncherRow.Empty(PRIVATE_SPACE_ROW_ID))
            privateSpace.apps.forEach { add(LauncherRow.App(it, PRIVATE_SPACE_ROW_ID)) }
        }
    }
    apps.forEach { add(LauncherRow.App(it)) }
}
