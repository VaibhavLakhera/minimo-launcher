package com.minimo.launcher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minimo.launcher.data.entities.AppInfoEntity

@Database(
    entities = [AppInfoEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
}