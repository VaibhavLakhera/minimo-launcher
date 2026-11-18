package com.minimo.launcher.data

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.minimo.launcher.utils.HomeTimeFormat

/**
 * Moves the old "24 Hour Format" toggle to the Time Format setting and deletes the toggle's key,
 * so it runs once. On becomes 24-Hour; off writes nothing, so the 12-Hour default applies.
 */
object TimeFormatMigration : DataMigration<Preferences> {
    private val KEY_TWENTY_FOUR_HOUR_FORMAT = booleanPreferencesKey("KEY_TWENTY_FOUR_HOUR_FORMAT")

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData.contains(KEY_TWENTY_FOUR_HOUR_FORMAT)

    override suspend fun migrate(currentData: Preferences): Preferences {
        val preferences = currentData.toMutablePreferences()
        if (preferences[KEY_TWENTY_FOUR_HOUR_FORMAT] == true) {
            preferences[PreferenceHelper.KEY_TIME_FORMAT] = HomeTimeFormat.TwentyFourHour.name
        }
        preferences.remove(KEY_TWENTY_FOUR_HOUR_FORMAT)
        return preferences.toPreferences()
    }

    override suspend fun cleanUp() = Unit
}
