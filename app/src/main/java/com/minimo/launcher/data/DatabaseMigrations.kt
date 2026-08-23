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

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shortcutInfoEntity` (
                    `package_name` TEXT NOT NULL,
                    `shortcut_id` TEXT NOT NULL,
                    `user_handle` INTEGER NOT NULL,
                    `shortcut_name` TEXT NOT NULL,
                    `alternate_shortcut_name` TEXT NOT NULL DEFAULT '',
                    `is_favourite` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`package_name`, `shortcut_id`, `user_handle`)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val cursor = db.query("PRAGMA table_info(shortcutInfoEntity)")
            val columns = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
            cursor.close()

            if (columns.contains("app_name")) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `shortcutInfoEntity_new` (
                        `package_name` TEXT NOT NULL,
                        `shortcut_id` TEXT NOT NULL,
                        `user_handle` INTEGER NOT NULL,
                        `shortcut_name` TEXT NOT NULL,
                        `alternate_shortcut_name` TEXT NOT NULL DEFAULT '',
                        `is_favourite` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`package_name`, `shortcut_id`, `user_handle`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO `shortcutInfoEntity_new` (`package_name`, `shortcut_id`, `user_handle`, `shortcut_name`, `alternate_shortcut_name`, `is_favourite`)
                    SELECT `package_name`, `shortcut_id`, `user_handle`, `app_name`, `alternate_app_name`, `is_favourite` FROM `shortcutInfoEntity`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE `shortcutInfoEntity`")
                db.execSQL("ALTER TABLE `shortcutInfoEntity_new` RENAME TO `shortcutInfoEntity`")
            }
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Version 7 replaces the separate app and shortcut tables with one launcher-item
            // table. Item type is part of the primary key so an activity and shortcut can safely
            // share the same package, target text, and profile.
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `appInfoEntity_new` (
                    `package_name` TEXT NOT NULL,
                    `item_type` TEXT NOT NULL,
                    `target_id` TEXT NOT NULL,
                    `user_handle` INTEGER NOT NULL,
                    `app_name` TEXT NOT NULL,
                    `alternate_app_name` TEXT NOT NULL DEFAULT '',
                    `is_favourite` INTEGER NOT NULL DEFAULT 0,
                    `is_hidden` INTEGER NOT NULL DEFAULT 0,
                    `order_index` INTEGER NOT NULL DEFAULT 0,
                    `launch_delay_seconds` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`package_name`, `item_type`, `target_id`, `user_handle`)
                )
                """.trimIndent()
            )

            // Existing app rows map directly to APP items. The former activity class_name is now
            // stored as target_id, while every user-controlled field is copied unchanged.
            db.execSQL(
                """
                INSERT INTO `appInfoEntity_new` (
                    `package_name`, `item_type`, `target_id`, `user_handle`, `app_name`,
                    `alternate_app_name`, `is_favourite`, `is_hidden`, `order_index`,
                    `launch_delay_seconds`
                )
                SELECT
                    `package_name`, 'APP', `class_name`, `user_handle`, `app_name`,
                    `alternate_app_name`, `is_favourite`, `is_hidden`, `order_index`,
                    `launch_delay_seconds`
                FROM `appInfoEntity`
                """.trimIndent()
            )

            // Preserve all existing app favourite positions. Favourite shortcuts are appended
            // after them in their previous case-insensitive display-name order, matching the old
            // Home layout where shortcuts were rendered below favourite apps.
            var nextFavouriteOrder = queryLong(
                db,
                "SELECT COALESCE(MAX(order_index), 0) FROM appInfoEntity WHERE is_favourite = 1"
            ).toInt()
            val shortcutCursor = db.query(
                """
                SELECT * FROM shortcutInfoEntity
                ORDER BY is_favourite DESC,
                    COALESCE(NULLIF(alternate_shortcut_name, ''), shortcut_name) COLLATE NOCASE,
                    package_name, shortcut_id, user_handle
                """.trimIndent()
            )
            while (shortcutCursor.moveToNext()) {
                val isFavourite = shortcutCursor.getInt(
                    shortcutCursor.getColumnIndexOrThrow("is_favourite")
                )
                val orderIndex = if (isFavourite == 1) ++nextFavouriteOrder else 0

                // Shortcut IDs become target_id values. Version 6 had no hidden or launch-delay
                // shortcut fields, so migrated shortcuts start visible with zero launch delay.
                db.execSQL(
                    """
                    INSERT INTO `appInfoEntity_new` (
                        `package_name`, `item_type`, `target_id`, `user_handle`, `app_name`,
                        `alternate_app_name`, `is_favourite`, `is_hidden`, `order_index`,
                        `launch_delay_seconds`
                    ) VALUES (?, 'SHORTCUT', ?, ?, ?, ?, ?, 0, ?, 0)
                    """.trimIndent(),
                    arrayOf(
                        shortcutCursor.getString(
                            shortcutCursor.getColumnIndexOrThrow("package_name")
                        ),
                        shortcutCursor.getString(
                            shortcutCursor.getColumnIndexOrThrow("shortcut_id")
                        ),
                        shortcutCursor.getInt(
                            shortcutCursor.getColumnIndexOrThrow("user_handle")
                        ),
                        shortcutCursor.getString(
                            shortcutCursor.getColumnIndexOrThrow("shortcut_name")
                        ),
                        shortcutCursor.getString(
                            shortcutCursor.getColumnIndexOrThrow("alternate_shortcut_name")
                        ),
                        isFavourite,
                        orderIndex
                    )
                )
            }
            shortcutCursor.close()

            // Verify that the unified table contains every source row before removing either old
            // table. A failed check aborts the Room migration transaction without losing data.
            val sourceCount = queryLong(db, "SELECT COUNT(*) FROM appInfoEntity") +
                    queryLong(db, "SELECT COUNT(*) FROM shortcutInfoEntity")
            val migratedCount = queryLong(db, "SELECT COUNT(*) FROM appInfoEntity_new")
            check(sourceCount == migratedCount) {
                "Launcher item migration lost data: expected $sourceCount rows, found $migratedCount"
            }

            // Source tables are only replaced after copying and validation have succeeded.
            db.execSQL("DROP TABLE `appInfoEntity`")
            db.execSQL("DROP TABLE `shortcutInfoEntity`")
            db.execSQL("ALTER TABLE `appInfoEntity_new` RENAME TO `appInfoEntity`")
        }
    }

    private fun queryLong(db: SupportSQLiteDatabase, query: String): Long {
        val cursor = db.query(query)
        val value = if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        cursor.close()
        return value
    }
}
