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
