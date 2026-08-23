package com.minimo.launcher.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.minimo.launcher.data.entities.AppInfoEntity
import com.minimo.launcher.data.entities.AppItemType
import com.minimo.launcher.data.usecase.reconcilePinnedShortcuts
import com.minimo.launcher.utils.InstalledShortcut
import com.minimo.launcher.utils.ShortcutInventory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortcutReconciliationTest {
    @Test
    fun unavailableInventoryDoesNotProduceDatabaseChanges() {
        assertNull(reconcilePinnedShortcuts(null, listOf(entity("saved", 10))))
    }

    @Test
    fun reconciliationUpdatesOnlyAuthoritativeProfilesAndPreservesUserState() {
        val renamed = entity(
            id = "renamed",
            user = 10,
            name = "Old source name",
            alternateName = "My custom name",
            favourite = true,
            hidden = false,
            order = 3,
            delay = 7
        )
        val resetName = entity(
            id = "reset-name",
            user = 10,
            name = "Old label",
            alternateName = "Old label"
        )
        val removedFromSuccessfulProfile = entity("gone", 10)
        val retainedFromFailedProfile = entity("unavailable", 20)
        val removedProfile = entity("removed-profile", 99)
        val inventory = ShortcutInventory(
            shortcuts = listOf(
                shortcut("renamed", 10, "New source name"),
                shortcut("reset-name", 10, "New label"),
                shortcut("new-shortcut", 30, "New shortcut")
            ),
            currentProfiles = setOf(10, 20, 30),
            successfulProfiles = setOf(10, 30)
        )

        val result = requireNotNull(
            reconcilePinnedShortcuts(
                inventory,
                listOf(
                    renamed,
                    resetName,
                    removedFromSuccessfulProfile,
                    retainedFromFailedProfile,
                    removedProfile
                )
            )
        )

        assertEquals(setOf("gone", "removed-profile"), result.deletions.map { it.targetId }.toSet())
        assertFalse(result.deletions.any { it.targetId == "unavailable" })

        val renamedUpdate = result.updates.single { it.targetId == "renamed" }
        assertEquals("New source name", renamedUpdate.appName)
        assertEquals("My custom name", renamedUpdate.alternateAppName)
        assertTrue(renamedUpdate.isFavourite)
        assertEquals(3, renamedUpdate.orderIndex)
        assertEquals(7, renamedUpdate.launchDelaySeconds)
        assertEquals("", result.updates.single { it.targetId == "reset-name" }.alternateAppName)

        val addition = result.additions.single()
        assertEquals("new-shortcut", addition.targetId)
        assertFalse(addition.isFavourite)
        assertFalse(addition.isHidden)
        assertEquals(0, addition.orderIndex)
        assertEquals(0, addition.launchDelaySeconds)
    }

    private fun shortcut(id: String, user: Int, name: String): InstalledShortcut {
        return InstalledShortcut(name, PACKAGE, id, user)
    }

    private fun entity(
        id: String,
        user: Int,
        name: String = id,
        alternateName: String = "",
        favourite: Boolean = false,
        hidden: Boolean = false,
        order: Int = 0,
        delay: Int = 0
    ): AppInfoEntity {
        return AppInfoEntity(
            packageName = PACKAGE,
            itemType = AppItemType.SHORTCUT,
            targetId = id,
            userHandle = user,
            appName = name,
            alternateAppName = alternateName,
            isFavourite = favourite,
            isHidden = hidden,
            orderIndex = order,
            launchDelaySeconds = delay
        )
    }

    private companion object {
        const val PACKAGE = "com.browser"
    }
}
