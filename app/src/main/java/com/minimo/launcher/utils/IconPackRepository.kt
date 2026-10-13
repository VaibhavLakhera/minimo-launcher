package com.minimo.launcher.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.LocaleList
import android.util.Xml
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.text.Collator
import javax.inject.Inject
import javax.inject.Singleton

/** Installed ADW-compatible packs. Only the last used pack's mappings are kept in memory. */
// Resource names come from another installed APK and cannot be referenced through our R class.
@SuppressLint("DiscouragedApi")
@Singleton
class IconPackRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val packageManager = context.packageManager

    // Serialises pack parsing so concurrent icon loads share a single parse.
    private val loadMutex = Mutex()

    // Guards cached and loadingPackage. Clearing loadingPackage tells an in-flight parse that its
    // package changed, so it must not cache resources from the old APK.
    private val lock = Any()
    @Volatile
    private var cached: Pack? = null
    private var loadingPackage: String? = null

    // Lets settings show the selected pack immediately while installedPacks() lists them again.
    @Volatile
    var lastInstalledPacks: List<IconPackOption>? = null
        private set

    // The language lastInstalledPacks' names were loaded in.
    @Volatile
    private var lastInstalledPacksLocales: LocaleList? = null

    /** Returns whether one of [packageNames] was the pack in memory or the one being read. */
    fun invalidate(packageNames: Set<String>): Boolean = synchronized(lock) {
        val wasCached = cached?.packageName in packageNames
        val wasLoading = loadingPackage in packageNames
        if (wasCached) cached = null
        if (wasLoading) loadingPackage = null
        wasCached || wasLoading
    }

    /** Frees the pack in memory unless it is [packageName]. */
    fun retainOnly(packageName: String) = synchronized(lock) {
        if (cached?.packageName != packageName) cached = null
        if (loadingPackage != packageName) loadingPackage = null
    }

    fun clear() = synchronized(lock) {
        cached = null
        loadingPackage = null
    }

    suspend fun installedPacks(): List<IconPackOption> = withContext(Dispatchers.IO) {
        val iconSize = (IconPackOption.ICON_SIZE_DP * context.resources.displayMetrics.density)
            .toInt()
            .coerceAtLeast(1)
        val packages = PACK_ACTIONS.flatMap { action ->
            ensureActive()
            packageManager.queryIntentActivities(Intent(action), 0)
        }.map { it.activityInfo.applicationInfo }.distinctBy { it.packageName }
        // A pack's name and icon only change when it is updated or the language changes, so the
        // options listed last time are reused instead of loading and drawing them again.
        val locales = context.resources.configuration.locales
        val reusable = lastInstalledPacks
            ?.takeIf { locales == lastInstalledPacksLocales }
            .orEmpty()
            .associateBy { it.packageName }
        val collator = Collator.getInstance()
        val packs = packages.mapNotNull { app ->
            ensureActive()
            try {
                if (!app.enabled) return@mapNotNull null
                val updatedAt = packageManager.getPackageInfo(app.packageName, 0).lastUpdateTime
                reusable[app.packageName]?.takeIf { it.updatedAt == updatedAt } ?: IconPackOption(
                    app.packageName,
                    app.loadLabel(packageManager).toString(),
                    app.loadIcon(packageManager).toBitmap(iconSize, iconSize).asImageBitmap(),
                    updatedAt
                )
            } catch (error: Exception) {
                // A package can disappear between discovery and loading its label/icon.
                Timber.w(error, "Unable to inspect icon pack %s", app.packageName)
                null
            }
        }
        ensureActive()
        lastInstalledPacksLocales = locales
        packs.sortedWith { a, b -> collator.compare(a.name, b.name) }
            .also { lastInstalledPacks = it }
    }

    /**
     * Returns [iconPack]'s drawable for [component]. When the pack has no entry for that exact
     * activity but draws the whole app with one icon, [canUseAppEntry] decides whether to use it.
     */
    suspend fun drawable(
        iconPack: String,
        component: ComponentName,
        density: Int,
        canUseAppEntry: () -> Boolean
    ): Drawable? {
        if (iconPack.isEmpty()) return null
        val pack = loadMutex.withLock {
            cached?.takeIf { it.packageName == iconPack } ?: load(iconPack)
        }
        val name = pack.items[component]
            ?: pack.appItems[component.packageName]?.takeIf { canUseAppEntry() }
            ?: return null
        return try {
            val resources = pack.resources ?: return null
            val id = resources.getIdentifier(name, "drawable", iconPack)
            if (id == 0) null else resources.getDrawableForDensity(id, density, null)
        } catch (error: Exception) {
            Timber.w(error, "Unable to load icon pack drawable %s/%s", iconPack, name)
            null
        }
    }

    private fun load(packageName: String): Pack {
        synchronized(lock) { loadingPackage = packageName }
        val pack = readPack(packageName)
        synchronized(lock) {
            if (loadingPackage == packageName) cached = pack
            loadingPackage = null
        }
        return pack
    }

    private fun readPack(packageName: String): Pack {
        return try {
            val app = packageManager.getApplicationInfo(packageName, 0)
            if (!app.enabled) return Pack(packageName)
            val resources = packageManager.getResourcesForApplication(app)
            val items = HashMap<ComponentName, String>()
            val appItems = HashMap<String, String>()
            try {
                val xmlId = resources.getIdentifier(APPFILTER, "xml", packageName)
                if (xmlId != 0) {
                    resources.getXml(xmlId).use { parse(it, items, appItems) }
                } else {
                    val rawId = resources.getIdentifier(APPFILTER, "raw", packageName)
                    val stream = if (rawId != 0) {
                        resources.openRawResource(rawId)
                    } else {
                        resources.assets.open("$APPFILTER.xml")
                    }
                    stream.use {
                        val parser = Xml.newPullParser()
                        parser.setInput(it, null)
                        parse(parser, items, appItems)
                    }
                }
            } catch (error: Exception) {
                // Raw and asset appfilters are not checked when the pack is built. Keep the
                // entries read before a damaged line instead of losing the whole pack.
                Timber.w(error, "Icon pack %s has a damaged appfilter", packageName)
            }
            Pack(packageName, resources, items, appItems)
        } catch (error: Exception) {
            // Cache failed/empty packs too; do not retry parsing for every visible app.
            Timber.w(error, "Unable to read icon pack %s", packageName)
            Pack(packageName)
        }
    }

    private fun parse(
        parser: XmlPullParser,
        items: MutableMap<ComponentName, String>,
        appItems: MutableMap<String, String>
    ) {
        // Apps the pack draws in more than one way, e.g. one icon per activity of a search app.
        val appsWithSeveralIcons = HashSet<String>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "item") {
                val raw = parser.getAttributeValue(null, "component")
                val drawable = parser.getAttributeValue(null, "drawable")
                val component = raw?.removePrefix("ComponentInfo{")?.removeSuffix("}")
                    ?.let(ComponentName::unflattenFromString)
                if (component != null && !drawable.isNullOrBlank()) {
                    items[component] = drawable
                    val app = component.packageName
                    val appDrawable = appItems[app]
                    when {
                        app in appsWithSeveralIcons -> Unit
                        appDrawable == null -> appItems[app] = drawable
                        appDrawable != drawable -> {
                            appItems.remove(app)
                            appsWithSeveralIcons += app
                        }
                    }
                }
            }
            event = parser.next()
        }
    }

    private data class Pack(
        val packageName: String,
        val resources: Resources? = null,
        val items: Map<ComponentName, String> = emptyMap(),
        // The one drawable a pack uses for an app, for activities it does not list by name. Apps
        // it draws in more than one way are left out, as any of those icons could be the wrong one.
        val appItems: Map<String, String> = emptyMap()
    )

    private companion object {
        // The file in which a pack lists its icons. Packs ship it as an XML resource, a raw
        // resource or an asset, as lines of the form
        // <item component="ComponentInfo{package/activity}" drawable="drawable_name" />
        const val APPFILTER = "appfilter"

        // An icon pack has no API of its own. It is found by an activity it declares for one of
        // the launchers that started the format: ADW (two actions), GO Launcher and Nova.
        val PACK_ACTIONS = listOf(
            "org.adw.launcher.THEMES",
            "org.adw.ActivityStarter.THEMES",
            "com.gau.go.launcherex.theme",
            "com.novalauncher.THEME"
        )
    }
}

/** [updatedAt] is the pack's last update time, which tells whether [name] and [icon] are current. */
data class IconPackOption(
    val packageName: String,
    val name: String,
    val icon: ImageBitmap,
    val updatedAt: Long
) {
    companion object {
        /** The size [icon] is drawn at, and so the size to show it at. */
        const val ICON_SIZE_DP = 24
    }
}
