package com.minimo.launcher.data

import androidx.room.withTransaction
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.FolderEntity
import com.minimo.launcher.ui.entities.AppInfo
import kotlinx.coroutines.flow.map
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class LauncherCatalog(val apps: List<AppInfoEntity>, val folders: List<FolderEntity>)
enum class FolderError { INVALID_NAME, DUPLICATE_NAME, UNAVAILABLE, SAVE_FAILED }
class FolderOperationException(val error: FolderError) : Exception(error.name)

@Singleton
class FolderRepository @Inject constructor(private val database: AppDatabase) {
    private val folders = database.folderDao()
    private val apps = database.appInfoDao()

    // Read both tables in one transaction so creation/deletion cannot briefly orphan UI rows.
    fun observeCatalog() = database.invalidationTracker
        .createFlow("appInfoEntity", "folderEntity")
        .map {
            database.withTransaction { LauncherCatalog(apps.getAllApps(), folders.getFolders()) }
        }

    suspend fun createWithApp(name: String, app: AppInfo, isFavourite: Boolean = false) =
        database.withTransaction {
            val current = requireUngroupedApp(app)
            val (trimmed, normalized) = validateName(name)
            val id = UUID.randomUUID().toString()
            folders.insert(FolderEntity(id, trimmed, normalized, isFavourite = isFavourite))
            setMembership(app, id)
            matchFolderFavourite(current, isFavourite)
        }

    suspend fun rename(id: String, name: String) = database.withTransaction {
        requireFolder(id)
        val (trimmed, normalized) = validateName(name, id)
        folders.rename(id, trimmed, normalized)
    }

    suspend fun assign(app: AppInfo, folderId: String) = database.withTransaction {
        val current = requireUngroupedApp(app)
        val folder = requireFolder(folderId)
        setMembership(app, folderId)
        matchFolderFavourite(current, folder.isFavourite)
    }

    suspend fun remove(app: AppInfo) = database.withTransaction {
        requireVisibleApp(app)
        setMembership(app, null)
    }

    suspend fun toggleFavourite(id: String) = database.withTransaction {
        val folder = requireFolder(id)
        val members = apps.getFolderMembers(id)
        if (folder.isFavourite) {
            // The DAO reads each current position, including shifts from earlier removals.
            members.filter { it.isFavourite }.forEach {
                apps.removeAppFromFavouriteTransaction(
                    it.itemType, it.targetId, it.packageName, it.userHandle
                )
            }
        } else {
            var nextIndex = apps.getMaxFavouriteOrder() + 1
            members.filter { !it.isFavourite && !it.isHidden }.forEach {
                apps.addAppToFavourite(
                    it.itemType,
                    it.targetId,
                    it.packageName,
                    it.userHandle,
                    nextIndex++
                )
            }
        }
        folders.toggleFavourite(id)
    }

    suspend fun delete(id: String) = database.withTransaction {
        requireFolder(id)
        // ON DELETE SET NULL releases the members without changing their favourite settings.
        folders.delete(id)
    }

    private suspend fun setMembership(app: AppInfo, folderId: String?) = folders.setMembership(
        app.packageName, app.itemType, app.targetId, app.userHandle, folderId
    )

    // A folder and its members need the same favourite status to appear together.
    private suspend fun matchFolderFavourite(app: AppInfoEntity, isFavourite: Boolean) {
        if (app.isFavourite == isFavourite) return
        if (isFavourite) {
            apps.addAppToFavourite(
                app.itemType, app.targetId, app.packageName, app.userHandle,
                apps.getMaxFavouriteOrder() + 1
            )
        } else {
            apps.removeAppFromFavouriteTransaction(
                app.itemType,
                app.targetId,
                app.packageName,
                app.userHandle
            )
        }
    }

    private suspend fun requireVisibleApp(app: AppInfo): AppInfoEntity {
        val current = apps.getApp(app.itemType, app.targetId, app.packageName, app.userHandle)
        if (current == null || current.isHidden) throw FolderOperationException(FolderError.UNAVAILABLE)
        return current
    }

    private suspend fun requireUngroupedApp(app: AppInfo): AppInfoEntity {
        val current = requireVisibleApp(app)
        if (current.folderId != null) throw FolderOperationException(FolderError.UNAVAILABLE)
        return current
    }

    private suspend fun requireFolder(id: String): FolderEntity =
        folders.getFolder(id) ?: throw FolderOperationException(FolderError.UNAVAILABLE)

    private suspend fun validateName(
        name: String,
        currentId: String? = null
    ): Pair<String, String> {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > 150) {
            throw FolderOperationException(FolderError.INVALID_NAME)
        }
        val normalized = trimmed.lowercase(Locale.ROOT)
        val existing = folders.getByNormalizedName(normalized)
        if (existing != null && existing.id != currentId) {
            throw FolderOperationException(FolderError.DUPLICATE_NAME)
        }
        return trimmed to normalized
    }
}
