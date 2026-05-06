package com.minimo.launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "shortcutInfoEntity",
    primaryKeys = ["package_name", "shortcut_id", "user_handle"]
)
data class ShortcutInfoEntity(
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "shortcut_id")
    val shortcutId: String,

    @ColumnInfo(name = "user_handle")
    val userHandle: Int,

    @ColumnInfo(name = "shortcut_name")
    val shortcutName: String,

    @ColumnInfo(name = "alternate_shortcut_name", defaultValue = "")
    val alternateShortcutName: String = "",

    @ColumnInfo(name = "is_favourite", defaultValue = "0")
    val isFavourite: Boolean
) {
    @get:Ignore
    val id: String
        get() = "$packageName-$shortcutId-$userHandle"
}