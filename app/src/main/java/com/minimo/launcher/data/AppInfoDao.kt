package com.minimo.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.ui.entities.AppOrderUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM appInfoEntity ORDER BY COALESCE( NULLIF(alternate_app_name, ''), app_name ) COLLATE NOCASE")
    fun getAllAppsFlow(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM appInfoEntity ORDER BY COALESCE( NULLIF(alternate_app_name, ''), app_name ) COLLATE NOCASE")
    suspend fun getAllApps(): List<AppInfoEntity>

    @Query("SELECT * FROM appInfoEntity WHERE is_hidden = 0 ORDER BY COALESCE( NULLIF(alternate_app_name, ''), app_name ) COLLATE NOCASE")
    fun getAllNonHiddenAppsFlow(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM appInfoEntity WHERE package_name = :packageName AND user_handle = :userHandle")
    suspend fun getAppsByPackageName(packageName: String, userHandle: Int): List<AppInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addApps(apps: List<AppInfoEntity>)

    @Query("SELECT * FROM appInfoEntity WHERE is_favourite = 1 ORDER BY order_index")
    fun getFavouriteAppsFlow(): Flow<List<AppInfoEntity>>

    @Query("UPDATE appInfoEntity SET is_favourite = 1, order_index = :orderIndex WHERE class_name = :className AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun addAppToFavourite(
        className: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    )

    @Transaction
    suspend fun removeAppFromFavouriteTransaction(
        className: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    ) {
        removeAppFromFavourite(className, packageName, userHandle)
        if (orderIndex > 0) {
            decreaseAllOrderIndex(orderIndex)
        }
    }

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET is_favourite = 0, order_index = 0 WHERE class_name = :className AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun removeAppFromFavourite(className: String, packageName: String, userHandle: Int)

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET order_index = order_index - 1 WHERE is_favourite = 1 AND order_index > :orderIndex")
    suspend fun decreaseAllOrderIndex(orderIndex: Int)

    @Transaction
    suspend fun addAppToHiddenTransaction(
        className: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    ) {
        addAppToHidden(className, packageName, userHandle)
        if (orderIndex > 0) {
            decreaseAllOrderIndex(orderIndex)
        }
    }

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET is_hidden = 1, is_favourite = 0, order_index = 0 WHERE class_name = :className AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun addAppToHidden(className: String, packageName: String, userHandle: Int)

    @Query("UPDATE appInfoEntity SET is_hidden = 0 WHERE class_name = :className AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun removeAppFromHidden(className: String, packageName: String, userHandle: Int)

    @Query("UPDATE appInfoEntity SET alternate_app_name = :newName WHERE class_name = :className AND package_name = :packageName")
    suspend fun renameApp(className: String, packageName: String, newName: String)

    @Transaction
    suspend fun deleteAppsTransaction(apps: List<AppInfoEntity>) {
        for (app in apps) {
            // From the input apps, get the favourite app which needs to be deleted
            val deletedFavourite = getFavouriteApp(app.className, app.packageName, app.userHandle)

            if (deletedFavourite != null) {
                // For every favourite deleted app, decrease the order index of favourite apps
                decreaseAllOrderIndex(deletedFavourite.orderIndex)
            }

            // Once index is updated, delete the app by input packageName and className
            deleteAppByClassAndPackage(app.className, app.packageName, app.userHandle)
        }
    }

    // Not called from outside this file
    @Query("DELETE FROM appInfoEntity WHERE class_name = :className AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun deleteAppByClassAndPackage(className: String, packageName: String, userHandle: Int)

    // Not called from outside this file
    @Query("SELECT * FROM appInfoEntity WHERE class_name = :className AND package_name = :packageName AND user_handle = :userHandle AND is_favourite = 1")
    suspend fun getFavouriteApp(
        className: String,
        packageName: String,
        userHandle: Int
    ): AppInfoEntity?

    @Query(
        """
        UPDATE appInfoEntity
        SET order_index = CASE
            WHEN class_name = :app1ClassName AND package_name = :app1PackageName AND user_handle = :app1UserHandle THEN :app2OrderIndex
            WHEN class_name = :app2ClassName AND package_name = :app2PackageName AND user_handle = :app2UserHandle THEN :app1OrderIndex
            ELSE order_index
        END
        WHERE (class_name = :app1ClassName AND package_name = :app1PackageName AND user_handle = :app1UserHandle) OR (class_name = :app2ClassName AND package_name = :app2PackageName AND user_handle = :app2UserHandle)
    """
    )
    suspend fun swapOrderIndex(
        app1ClassName: String,
        app1PackageName: String,
        app1UserHandle: Int,
        app2ClassName: String,
        app2PackageName: String,
        app2UserHandle: Int,
        app1OrderIndex: Int,
        app2OrderIndex: Int
    )

    @Transaction
    suspend fun updateAppOrder(updates: List<AppOrderUpdate>) {
        for (update in updates) {
            updateOrderIndex(
                update.className,
                update.packageName,
                update.userHandle,
                update.orderIndex
            )
        }
    }

    // Not called from outside this file
    @Query("UPDATE appInfoEntity SET order_index = :orderIndex WHERE class_name = :className AND package_name = :packageName AND user_handle = :userHandle")
    suspend fun updateOrderIndex(
        className: String,
        packageName: String,
        userHandle: Int,
        orderIndex: Int
    )
}