package com.minimo.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.minimo.launcher.data.entities.ShortcutInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutInfoDao {
    @Query("SELECT * FROM shortcutInfoEntity ORDER BY COALESCE(NULLIF(alternate_shortcut_name, ''), shortcut_name) COLLATE NOCASE")
    fun getAllShortcutsFlow(): Flow<List<ShortcutInfoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM shortcutInfoEntity)")
    suspend fun hasShortcuts(): Boolean

    @Query("SELECT * FROM shortcutInfoEntity WHERE is_favourite = 1 ORDER BY COALESCE(NULLIF(alternate_shortcut_name, ''), shortcut_name) COLLATE NOCASE")
    fun getFavouriteShortcutsFlow(): Flow<List<ShortcutInfoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addShortcuts(apps: List<ShortcutInfoEntity>)

    @Query("DELETE FROM shortcutInfoEntity WHERE package_name = :packageName AND shortcut_id = :shortcutId AND user_handle = :userHandle")
    suspend fun deleteShortcut(packageName: String, shortcutId: String, userHandle: Int)

    @Transaction
    suspend fun deleteShortcutsTransaction(apps: List<ShortcutInfoEntity>) {
        for (app in apps) {
            deleteShortcut(app.packageName, app.shortcutId, app.userHandle)
        }
    }

    @Query("UPDATE shortcutInfoEntity SET is_favourite = 1 WHERE package_name = :packageName AND shortcut_id = :shortcutId AND user_handle = :userHandle")
    suspend fun addShortcutToFavourite(packageName: String, shortcutId: String, userHandle: Int)

    @Query("UPDATE shortcutInfoEntity SET is_favourite = 0 WHERE package_name = :packageName AND shortcut_id = :shortcutId AND user_handle = :userHandle")
    suspend fun removeShortcutFromFavourite(
        packageName: String,
        shortcutId: String,
        userHandle: Int
    )

    @Query("UPDATE shortcutInfoEntity SET alternate_shortcut_name = :alternateName WHERE package_name = :packageName AND shortcut_id = :shortcutId AND user_handle = :userHandle")
    suspend fun renameShortcut(
        packageName: String,
        shortcutId: String,
        userHandle: Int,
        alternateName: String
    )
}