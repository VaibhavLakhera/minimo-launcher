package com.minimo.launcher.data.entities

import androidx.room.TypeConverter

enum class AppItemType(val persistedValue: String) {
    APP("APP"),
    SHORTCUT("SHORTCUT");

    companion object {
        fun fromPersistedValue(value: String): AppItemType {
            return entries.firstOrNull { it.persistedValue == value }
                ?: throw IllegalArgumentException("Unknown app item type: $value")
        }
    }
}

class AppItemTypeConverter {
    @TypeConverter
    fun fromAppItemType(value: AppItemType): String = value.persistedValue

    @TypeConverter
    fun toAppItemType(value: String): AppItemType = AppItemType.fromPersistedValue(value)
}
