package com.minimo.launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

@Entity(tableName = "appInfoEntity", primaryKeys = ["package_name", "class_name", "user_handle"])
data class AppInfoEntity(
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "class_name")
    val className: String,

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
    val orderIndex: Int
) {
    @get:Ignore
    val id: String
        get() = packageName + className + userHandle
}