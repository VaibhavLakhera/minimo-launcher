package com.minimo.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.data.entities.FolderEntity

@Dao
interface FolderDao {
    @Query("SELECT * FROM folderEntity")
    suspend fun getFolders(): List<FolderEntity>

    @Query("SELECT * FROM folderEntity WHERE id = :id")
    suspend fun getFolder(id: String): FolderEntity?

    @Query("SELECT * FROM folderEntity WHERE normalized_name = :name")
    suspend fun getByNormalizedName(name: String): FolderEntity?

    @Insert
    suspend fun insert(folder: FolderEntity)

    @Query("UPDATE folderEntity SET name = :name, normalized_name = :normalizedName WHERE id = :id")
    suspend fun rename(id: String, name: String, normalizedName: String)

    @Query("UPDATE folderEntity SET is_favourite = NOT is_favourite WHERE id = :id")
    suspend fun toggleFavourite(id: String)

    @Query("DELETE FROM folderEntity WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE appInfoEntity SET folder_id = :folderId WHERE package_name = :packageName AND item_type = :itemType AND target_id = :targetId AND user_handle = :userHandle")
    suspend fun setMembership(
        packageName: String, itemType: AppItemType, targetId: String, userHandle: Int,
        folderId: String?
    )
}
