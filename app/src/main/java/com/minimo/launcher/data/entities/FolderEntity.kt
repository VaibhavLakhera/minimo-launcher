package com.minimo.launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "folderEntity", indices = [Index(value = ["normalized_name"], unique = true)])
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String,
    @ColumnInfo(name = "is_favourite", defaultValue = "0") val isFavourite: Boolean = false
)
