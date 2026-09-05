package com.minimo.launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

/**
 * A launcher item backed by either an installed launcher activity or a pinned shortcut.
 *
 * [targetId] contains the launcher activity class name for [AppItemType.APP] rows and the
 * Android shortcut ID for [AppItemType.SHORTCUT] rows. Together with [packageName], [itemType],
 * and [userHandle], it forms the persisted identity of a launcher item. App sync can move saved
 * state to a new identity when an app replaces one launcher component with another.
 */
@Entity(
    tableName = "appInfoEntity",
    primaryKeys = ["package_name", "item_type", "target_id", "user_handle"]
)
data class AppInfoEntity(
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "item_type")
    val itemType: AppItemType,

    @ColumnInfo(name = "target_id")
    val targetId: String,

    @ColumnInfo(name = "user_handle")
    val userHandle: Int,

    @ColumnInfo(name = "app_name")
    val appName: String,

    @ColumnInfo(name = "alternate_app_name", defaultValue = "")
    val alternateAppName: String,

    @ColumnInfo(name = "is_favourite", defaultValue = "0")
    val isFavourite: Boolean,

    @ColumnInfo(name = "is_hidden", defaultValue = "0")
    val isHidden: Boolean,

    @ColumnInfo(name = "order_index", defaultValue = "0")
    val orderIndex: Int,

    @ColumnInfo(name = "launch_delay_seconds", defaultValue = "0")
    val launchDelaySeconds: Int = 0
) {
    @get:Ignore
    val id: String
        get() = "${itemType.persistedValue}|$packageName|$targetId|$userHandle"
}
