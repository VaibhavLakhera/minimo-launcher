package com.minimo.launcher.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.AppItemTypeConverter
import com.minimo.launcher.data.entities.FolderEntity

@Database(
    entities = [AppInfoEntity::class, FolderEntity::class],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 5, to = 6)
    ]
)
@TypeConverters(AppItemTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
    abstract fun folderDao(): FolderDao
}
