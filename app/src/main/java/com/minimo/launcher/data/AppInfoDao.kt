package com.minimo.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.ui.entities.AppOrderUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM appInfoEntity ORDER BY COALESCE(NULLIF(alternate_app_name, ''), app_name) COLLATE NOCASE")
    fun getAllAppsFlow(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM appInfoEntity ORDER BY COALESCE(NULLIF(alternate_app_name, ''), app_name) COLLATE NOCASE")
    suspend fun getAllApps(): List<AppInfoEntity>

    @Query("SELECT * FROM appInfoEntity WHERE item_type = :itemType ORDER BY COALESCE(NULLIF(alternate_app_name, ''), app_name) COLLATE NOCASE")
    suspend fun getItemsByType(itemType: AppItemType): List<AppInfoEntity>

    @Query("SELECT * FROM appInfoEntity WHERE is_hidden = 0 ORDER BY COALESCE(NULLIF(alternate_app_name, ''), app_name) COLLATE NOCASE")
    fun getAllNonHiddenAppsFlow(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM appInfoEntity WHERE is_favourite = 0 ORDER BY COALESCE(NULLIF(alternate_app_name, ''), app_name) COLLATE NOCASE")
    fun getAllNonFavouriteAppsFlow(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM appInfoEntity WHERE package_name = :packageName AND user_handle = :userHandle")
    suspend fun getItemsByPackageName(packageName: String, userHandle: Int): List<AppInfoEntity>

    @Query("SELECT * FROM appInfoEntity WHERE package_name = :packageName AND user_handle = :userHandle AND item_type = 'APP'")
    suspend fun getAppsByPackageName(packageName: String, userHandle: Int): List<AppInfoEntity>

    @Query("SELECT * FROM appInfoEntity WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun getApp(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int
    ): AppInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addApps(apps: List<AppInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAppIfMissing(app: AppInfoEntity): Long

    @Query("SELECT * FROM appInfoEntity WHERE is_favourite = 1 ORDER BY order_index")
    fun getFavouriteAppsFlow(): Flow<List<AppInfoEntity>>

    @Query("UPDATE appInfoEntity SET is_favourite = 1, order_index = :orderIndex WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun addAppToFavourite(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    )

    @Transaction
    suspend fun removeAppFromFavouriteTransaction(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    ) {
        removeAppFromFavourite(itemType, targetId, packageName, userHandle)
        if (orderIndex > 0) {
            decreaseAllOrderIndex(orderIndex)
        }
    }

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET is_favourite = 0, order_index = 0 WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun removeAppFromFavourite(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int
    )

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET order_index = order_index - 1 WHERE is_favourite = 1 AND order_index > :orderIndex")
    suspend fun decreaseAllOrderIndex(orderIndex: Int)

    @Transaction
    suspend fun addAppToHiddenTransaction(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    ) {
        addAppToHidden(itemType, targetId, packageName, userHandle)
        if (orderIndex > 0) {
            decreaseAllOrderIndex(orderIndex)
        }
    }

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET is_hidden = 1, is_favourite = 0, order_index = 0 WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun addAppToHidden(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int
    )

    @Query("UPDATE appInfoEntity SET is_hidden = 0 WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun removeAppFromHidden(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int
    )

    @Query("UPDATE appInfoEntity SET alternate_app_name = :newName WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun renameApp(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int,
        newName: String
    )

    @Query("UPDATE appInfoEntity SET launch_delay_seconds = :delaySeconds WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun updateLaunchDelay(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int,
        delaySeconds: Int
    )

    @Transaction
    suspend fun syncAppsTransaction(
        updates: List<AppInfoEntity>,
        additions: List<AppInfoEntity>,
        replacements: Map<AppInfoEntity, AppInfoEntity>,
        deletedApps: List<AppInfoEntity>
    ) {
        for (app in updates) {
            updateAppLabel(app.targetId, app.packageName, app.userHandle, app.appName)
        }
        for ((previous, replacement) in replacements) {
            // User settings may have changed since the inventory was read. Copy the current row,
            // and do not compact favourite order when replacing only its component identity.
            val current = getApp(
                previous.itemType, previous.targetId, previous.packageName, previous.userHandle
            ) ?: continue
            deleteApp(current.itemType, current.targetId, current.packageName, current.userHandle)
            addApps(
                listOf(
                    current.copy(
                        targetId = replacement.targetId,
                        appName = replacement.appName,
                        alternateAppName = if (current.alternateAppName == current.appName) {
                            ""
                        } else {
                            current.alternateAppName
                        }
                    )
                )
            )
        }
        for (app in additions) {
            addAppIfMissing(app)
        }
        if (deletedApps.isNotEmpty()) {
            deleteAppsTransaction(deletedApps)
        }
    }

    // Refresh source labels without overwriting user settings with an earlier inventory snapshot.
    @Query(
        """
        UPDATE appInfoEntity
        SET app_name = :appName,
            alternate_app_name = CASE WHEN alternate_app_name = app_name THEN '' ELSE alternate_app_name END
        WHERE item_type = 'APP' AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle
    """
    )
    suspend fun updateAppLabel(
        targetId: String,
        packageName: String,
        userHandle: Int,
        appName: String
    )

    @Transaction
    suspend fun deleteAppsTransaction(apps: List<AppInfoEntity>) {
        for (app in apps) {
            // From the input items, get the favourite item which needs to be deleted.
            val deletedFavourite = getFavouriteApp(
                app.itemType,
                app.targetId,
                app.packageName,
                app.userHandle
            )

            // Delete only the exact app or shortcut identity, including its profile.
            deleteApp(app.itemType, app.targetId, app.packageName, app.userHandle)

            if (deletedFavourite != null) {
                // Keep the remaining favourite order contiguous after each deleted item.
                decreaseAllOrderIndex(deletedFavourite.orderIndex)
            }
        }
    }

    @Transaction
    suspend fun deleteAppTransaction(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int
    ) {
        val deletedFavourite = getFavouriteApp(itemType, targetId, packageName, userHandle)
        deleteApp(itemType, targetId, packageName, userHandle)
        if (deletedFavourite != null) {
            decreaseAllOrderIndex(deletedFavourite.orderIndex)
        }
    }

    // Not called from outside this file
    @Query("DELETE FROM appInfoEntity WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun deleteApp(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int
    )

    // Not called from outside this file
    @Query("SELECT * FROM appInfoEntity WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle AND is_favourite = 1")
    suspend fun getFavouriteApp(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int
    ): AppInfoEntity?

    @Query(
        """
        UPDATE appInfoEntity
        SET order_index = CASE
            WHEN item_type = :app1ItemType AND target_id = :app1TargetId AND package_name = :app1PackageName AND user_handle = :app1UserHandle THEN :app2OrderIndex
            WHEN item_type = :app2ItemType AND target_id = :app2TargetId AND package_name = :app2PackageName AND user_handle = :app2UserHandle THEN :app1OrderIndex
            ELSE order_index
        END
        WHERE (item_type = :app1ItemType AND target_id = :app1TargetId AND package_name = :app1PackageName AND user_handle = :app1UserHandle)
           OR (item_type = :app2ItemType AND target_id = :app2TargetId AND package_name = :app2PackageName AND user_handle = :app2UserHandle)
        """
    )
    suspend fun swapOrderIndex(
        app1ItemType: AppItemType,
        app1TargetId: String,
        app1PackageName: String,
        app1UserHandle: Int,
        app2ItemType: AppItemType,
        app2TargetId: String,
        app2PackageName: String,
        app2UserHandle: Int,
        app1OrderIndex: Int,
        app2OrderIndex: Int
    )

    @Transaction
    suspend fun updateAppOrder(updates: List<AppOrderUpdate>) {
        for (update in updates) {
            updateOrderIndex(
                update.itemType,
                update.targetId,
                update.packageName,
                update.userHandle,
                update.orderIndex
            )
        }
    }

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET order_index = :orderIndex WHERE item_type = :itemType AND target_id = :targetId AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun updateOrderIndex(
        itemType: AppItemType,
        targetId: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    )
}
