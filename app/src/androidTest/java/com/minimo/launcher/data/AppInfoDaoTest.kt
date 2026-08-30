package com.minimo.launcher.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.AppItemType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppInfoDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: AppInfoDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.appInfoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fullItemKeyKeepsAppsShortcutsAndProfilesIndependent() = runBlocking {
        val app = entity(AppItemType.APP, "same-target", 10, "Browser")
        val shortcut = entity(AppItemType.SHORTCUT, "same-target", 10, "Website")
        val workShortcut = entity(AppItemType.SHORTCUT, "same-target", 20, "Work Website")
        dao.addApps(listOf(app, shortcut, workShortcut))

        dao.addAppToFavourite(app.itemType, app.targetId, app.packageName, app.userHandle, 1)
        dao.addAppToFavourite(
            shortcut.itemType,
            shortcut.targetId,
            shortcut.packageName,
            shortcut.userHandle,
            2
        )
        dao.addAppToFavourite(
            workShortcut.itemType,
            workShortcut.targetId,
            workShortcut.packageName,
            workShortcut.userHandle,
            3
        )
        dao.renameApp(
            shortcut.itemType,
            shortcut.targetId,
            shortcut.packageName,
            shortcut.userHandle,
            "Renamed Website"
        )

        assertEquals(
            "",
            dao.getApp(AppItemType.APP, "same-target", PACKAGE, 10)?.alternateAppName
        )
        assertEquals(
            "Renamed Website",
            dao.getApp(AppItemType.SHORTCUT, "same-target", PACKAGE, 10)?.alternateAppName
        )
        assertEquals(
            "",
            dao.getApp(AppItemType.SHORTCUT, "same-target", PACKAGE, 20)?.alternateAppName
        )

        dao.addAppToHiddenTransaction(
            shortcut.itemType,
            shortcut.targetId,
            shortcut.packageName,
            shortcut.userHandle,
            2
        )
        val hiddenShortcut = dao.getApp(AppItemType.SHORTCUT, "same-target", PACKAGE, 10)
        assertTrue(hiddenShortcut?.isHidden == true)
        assertFalse(hiddenShortcut?.isFavourite == true)
        assertEquals(0, hiddenShortcut?.orderIndex)
        assertEquals(2, dao.getApp(AppItemType.SHORTCUT, "same-target", PACKAGE, 20)?.orderIndex)

        dao.deleteAppTransaction(AppItemType.APP, "same-target", PACKAGE, 10)
        assertNull(dao.getApp(AppItemType.APP, "same-target", PACKAGE, 10))
        assertNotNull(dao.getApp(AppItemType.SHORTCUT, "same-target", PACKAGE, 10))
        assertNotNull(dao.getApp(AppItemType.SHORTCUT, "same-target", PACKAGE, 20))
        assertEquals(1, dao.getApp(AppItemType.SHORTCUT, "same-target", PACKAGE, 20)?.orderIndex)
    }

    @Test
    fun aliasReplacementKeepsOrderWhileRealRemovalCompactsIt() = runBlocking {
        val removedFavourite = entity(AppItemType.APP, "removed", 10, "Removed")
            .copy(isFavourite = true, orderIndex = 1)
        val oldAlias = entity(AppItemType.APP, "old-alias", 10, "Old")
            .copy(
                alternateAppName = "Custom",
                isFavourite = true,
                orderIndex = 2,
                launchDelaySeconds = 5
            )
        val lastFavourite = entity(AppItemType.APP, "last", 10, "Last")
            .copy(isFavourite = true, orderIndex = 3)
        val newAlias = oldAlias.copy(targetId = "new-alias", appName = "New")
        dao.addApps(listOf(removedFavourite, oldAlias, lastFavourite))

        dao.syncAppsTransaction(
            updates = emptyList(),
            additions = emptyList(),
            replacements = mapOf(oldAlias to newAlias),
            deletedApps = listOf(removedFavourite)
        )

        assertNull(dao.getApp(AppItemType.APP, "old-alias", PACKAGE, 10))
        assertNull(dao.getApp(AppItemType.APP, "removed", PACKAGE, 10))
        val replacement = dao.getApp(AppItemType.APP, "new-alias", PACKAGE, 10)
        assertNotNull(replacement)
        assertTrue(replacement?.isFavourite == true)
        assertEquals(1, replacement?.orderIndex)
        assertEquals("Custom", replacement?.alternateAppName)
        assertEquals(5, replacement?.launchDelaySeconds)
        assertEquals(2, dao.getApp(AppItemType.APP, "last", PACKAGE, 10)?.orderIndex)
    }

    @Test
    fun replacementReadsLatestFavouriteAndHiddenSettings() = runBlocking {
        for (hidden in listOf(false, true)) {
            val user = if (hidden) 20 else 10
            val snapshot = entity(AppItemType.APP, "old", user, "Old Label")
            dao.addApps(listOf(snapshot))
            val latest = snapshot.copy(
                alternateAppName = "Edited after snapshot",
                isFavourite = !hidden,
                isHidden = hidden,
                orderIndex = if (hidden) 0 else 1,
                launchDelaySeconds = 7
            )
            dao.addApps(listOf(latest))

            dao.syncAppsTransaction(
                updates = emptyList(),
                additions = emptyList(),
                replacements = mapOf(
                    snapshot to snapshot.copy(
                        targetId = "new",
                        appName = "New Label"
                    )
                ),
                deletedApps = emptyList()
            )

            assertNull(dao.getApp(AppItemType.APP, "old", PACKAGE, user))
            assertEquals(
                latest.copy(targetId = "new", appName = "New Label"),
                dao.getApp(AppItemType.APP, "new", PACKAGE, user)
            )
        }
    }

    @Test
    fun labelRefreshDoesNotOverwriteSettingsEditedAfterSnapshot() = runBlocking {
        val snapshot = entity(AppItemType.APP, "main", 10, "Old Label")
        dao.addApps(listOf(snapshot))
        val latest = snapshot.copy(
            alternateAppName = "My App",
            isFavourite = true,
            orderIndex = 3,
            launchDelaySeconds = 6
        )
        dao.addApps(listOf(latest))

        dao.syncAppsTransaction(
            updates = listOf(snapshot.copy(appName = "New Label")),
            additions = emptyList(),
            replacements = emptyMap(),
            deletedApps = emptyList()
        )

        assertEquals(
            latest.copy(appName = "New Label"),
            dao.getApp(AppItemType.APP, "main", PACKAGE, 10)
        )
    }

    @Test
    fun labelRefreshClearsOnlyAnAlternateNameEqualToThePreviousSourceLabel() = runBlocking {
        val app = entity(AppItemType.APP, "main", 10, "Old Label")
            .copy(alternateAppName = "Old Label", isHidden = true)
        dao.addApps(listOf(app))

        dao.syncAppsTransaction(
            updates = listOf(app.copy(appName = "New Label")),
            additions = emptyList(),
            replacements = emptyMap(),
            deletedApps = emptyList()
        )

        assertEquals(
            app.copy(appName = "New Label", alternateAppName = ""),
            dao.getApp(AppItemType.APP, "main", PACKAGE, 10)
        )
    }

    @Test
    fun additionsDoNotOverwriteAnExistingRow() = runBlocking {
        val addition = entity(AppItemType.APP, "main", 10, "App")
        val existing = addition.copy(isHidden = true, launchDelaySeconds = 5)
        dao.addApps(listOf(existing))

        dao.syncAppsTransaction(
            updates = emptyList(),
            additions = listOf(addition),
            replacements = emptyMap(),
            deletedApps = emptyList()
        )

        assertEquals(existing, dao.getApp(AppItemType.APP, "main", PACKAGE, 10))
    }

    private fun entity(
        type: AppItemType,
        targetId: String,
        user: Int,
        name: String
    ): AppInfoEntity {
        return AppInfoEntity(
            packageName = PACKAGE,
            itemType = type,
            targetId = targetId,
            userHandle = user,
            appName = name,
            alternateAppName = "",
            isFavourite = false,
            isHidden = false,
            orderIndex = 0,
            launchDelaySeconds = 0
        )
    }

    private companion object {
        const val PACKAGE = "com.browser"
    }
}
