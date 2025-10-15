package com.minimo.launcher.data

import android.content.ContentValues
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.database.sqlite.SQLiteDatabase
import android.os.Process
import android.os.UserManager
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

object DatabaseMigrations {
    fun MIGRATION_1_2(context: Context) = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create new table with updated schema
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `appInfoEntity_new` (
                    `package_name` TEXT NOT NULL,
                    `user_handle` INTEGER NOT NULL,
                    `app_name` TEXT NOT NULL,
                    `class_name` TEXT NOT NULL,
                    `alternate_app_name` TEXT NOT NULL DEFAULT '',
                    `is_favourite` INTEGER NOT NULL DEFAULT 0,
                    `is_hidden` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`package_name`, `user_handle`, `class_name`)
                )
            """.trimIndent()
            )

            // Using LauncherApps get all activities. This will be used to get the className of existing apps in DB
            val launcherApps =
                context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
            val mainUserHandle = Process.myUserHandle()

            // Build a map of package names to their launcher activities
            val packageToActivities = mutableMapOf<String, LauncherActivityInfo>()

            try {
                for (profile in userManager.userProfiles) {
                    val activities = launcherApps.getActivityList(null, profile)
                    for (activity in activities) {
                        val packageName = activity.componentName.packageName
                        packageToActivities[packageName] = activity
                    }
                }
            } catch (exception: Exception) {
                Timber.e(exception)
            }

            // Get existing data from old table
            val cursor = db.query("SELECT * FROM appInfoEntity")

            while (cursor.moveToNext()) {
                val packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name"))
                val appName = cursor.getString(cursor.getColumnIndexOrThrow("app_name"))
                val alternateAppName =
                    cursor.getString(cursor.getColumnIndexOrThrow("alternate_app_name"))
                val isFavourite = cursor.getInt(cursor.getColumnIndexOrThrow("is_favourite"))
                val isHidden = cursor.getInt(cursor.getColumnIndexOrThrow("is_hidden"))

                // Find the actual launcher activity for this package
                val activity = packageToActivities[packageName]

                // Insert an entry for each launcher activity found
                if (activity != null) {
                    // Only add main profile activities during migration
                    if (activity.user == mainUserHandle) {
                        val className = activity.componentName.className
                        val userHandle = activity.user.hashCode()

                        db.execSQL(
                            """
                                INSERT OR REPLACE INTO `appInfoEntity_new` 
                                (`package_name`, `user_handle`, `app_name`, `class_name`, `alternate_app_name`, `is_favourite`, `is_hidden`)
                                VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                            arrayOf(
                                packageName,
                                userHandle,
                                appName,
                                className,
                                alternateAppName,
                                isFavourite,
                                isHidden
                            )
                        )
                    }
                }
            }
            cursor.close()

            // Drop old table
            db.execSQL("DROP TABLE `appInfoEntity`")

            // Rename new table to original name
            db.execSQL("ALTER TABLE `appInfoEntity_new` RENAME TO `appInfoEntity`")
        }
    }

    // Add a new column 'order_index' to the 'appInfoEntity' table.
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new 'order_index' column to the table.
            // It's an INTEGER NOT NULL, and defaults to 0.
            db.execSQL("ALTER TABLE appInfoEntity ADD COLUMN order_index INTEGER NOT NULL DEFAULT 0")

            // Fetch existing favourite apps in their current sorted order.
            val favouritesCursor =
                db.query("SELECT * FROM appInfoEntity WHERE is_favourite = 1 ORDER BY COALESCE(NULLIF(alternate_app_name, ''), app_name) COLLATE NOCASE")

            if (favouritesCursor.moveToFirst()) {
                // Get column indices once to use in the loop.
                val packageNameIndex = favouritesCursor.getColumnIndex("package_name")
                val classNameIndex = favouritesCursor.getColumnIndex("class_name")
                val userHandleIndex = favouritesCursor.getColumnIndex("user_handle")

                // Start the current order index at 1.
                var currentOrderIndex = 1

                do {
                    // For each favourite app, update its 'order_index'.
                    val packageName = favouritesCursor.getString(packageNameIndex)
                    val className = favouritesCursor.getString(classNameIndex)
                    val userHandle = favouritesCursor.getInt(userHandleIndex)

                    val contentValues = ContentValues().apply {
                        put("order_index", currentOrderIndex)
                    }

                    db.update(
                        "appInfoEntity",
                        SQLiteDatabase.CONFLICT_NONE,
                        contentValues,
                        "package_name = ? AND class_name = ? AND user_handle = ?",
                        arrayOf(packageName, className, userHandle.toString())
                    )

                    currentOrderIndex++
                } while (favouritesCursor.moveToNext())
            }

            favouritesCursor.close()
        }
    }
}