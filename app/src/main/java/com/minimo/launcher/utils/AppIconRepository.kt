package com.minimo.launcher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.UserManager
import android.util.DisplayMetrics
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import com.minimo.launcher.data.entities.AppItemType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class AppIconRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val shortcutsUtils: ShortcutsUtils
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)

    // Prevents an in-flight load from restoring a stale icon after the cache was cleared or an
    // app's cached icons were removed. Loads only cache results from the generation they started in.
    private val cacheGeneration = AtomicInteger()

    // Notifies active Compose icon loaders after invalidated entries have been removed.
    private val _cacheRevision = MutableStateFlow(0)
    val cacheRevision = _cacheRevision.asStateFlow()
    private val loadSemaphore = Semaphore(permits = 2)

    // Reuses icons by item identity, user profile, and rendered size. Entries are measured in KB so
    // the cache stays within the memory-based limit returned by maxCacheSizeKb().
    private val iconCache = object : LruCache<AppIconKey, ImageBitmap>(maxCacheSizeKb()) {
        override fun sizeOf(key: AppIconKey, value: ImageBitmap): Int {
            return (value.width * value.height * BYTES_PER_PIXEL / 1024).coerceAtLeast(1)
        }
    }

    suspend fun loadIcon(
        packageName: String,
        itemType: AppItemType,
        targetId: String,
        userHandle: Int,
        sizePx: Int
    ): ImageBitmap? {
        if (sizePx <= 0) return null

        val key = AppIconKey(packageName, itemType, targetId, userHandle, sizePx)
        iconCache.get(key)?.let { return it }

        return loadSemaphore.withPermit {
            iconCache.get(key)?.let { return@withPermit it }
            val generation = cacheGeneration.get()

            withContext(Dispatchers.IO) {
                val loadedImage = runCatching {
                    val profile = userManager.userProfiles.firstOrNull {
                        it.hashCode() == userHandle
                    } ?: return@runCatching null

                    val requestedDensity = (sizePx * DisplayMetrics.DENSITY_DEFAULT /
                            DEFAULT_ICON_SIZE_DP.toFloat())
                        .toInt()
                        .coerceAtLeast(DisplayMetrics.DENSITY_LOW)
                    when (itemType) {
                        AppItemType.APP -> launcherApps.getActivityList(packageName, profile)
                            .firstOrNull { it.componentName.className == targetId }
                            ?.getIcon(requestedDensity)

                        AppItemType.SHORTCUT -> shortcutsUtils.getShortcut(
                            packageName = packageName,
                            shortcutId = targetId,
                            userHandle = userHandle
                        )?.let { shortcut ->
                            launcherApps.getShortcutIconDrawable(shortcut, requestedDensity)
                        }
                    }?.renderToImageBitmap(sizePx)
                }.onFailure {
                    Timber.w(it, "Unable to load icon for %s/%s", packageName, targetId)
                }.getOrNull()
                val image = loadedImage ?: if (itemType == AppItemType.SHORTCUT) {
                    createGenericShortcutIcon(sizePx)
                } else {
                    createWhiteIcon(sizePx)
                }

                ensureActive()
                if (generation == cacheGeneration.get()) {
                    iconCache.put(key, image)
                }
                image
            }
        }
    }

    fun clear() {
        val revision = cacheGeneration.incrementAndGet()
        iconCache.evictAll()
        _cacheRevision.value = revision
    }

    fun removeIcon(packageName: String, userHandle: Int) {
        val revision = cacheGeneration.incrementAndGet()
        iconCache.snapshot().keys
            .filter { it.packageName == packageName && it.userHandle == userHandle }
            .forEach(iconCache::remove)
        _cacheRevision.value = revision
    }

    private fun Drawable.renderToImageBitmap(sizePx: Int): ImageBitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)

        if (this is AdaptiveIconDrawable) {
            // Android extends both 108dp layers beyond the visible viewport so their inner
            // safe zone fills the mask. Keep masking in Compose to support configurable shapes.
            val extraInset = (sizePx * AdaptiveIconDrawable.getExtraInsetFraction()).roundToInt()
            if (background == null) canvas.drawColor(Color.WHITE)
            background?.setBounds(
                -extraInset,
                -extraInset,
                sizePx + extraInset,
                sizePx + extraInset
            )
            background?.draw(canvas)
            foreground?.setBounds(
                -extraInset,
                -extraInset,
                sizePx + extraInset,
                sizePx + extraInset
            )
            foreground?.draw(canvas)
        } else {
            renderLegacyIcon(canvas, sizePx)
        }

        return bitmap.asImageBitmap()
    }

    private fun Drawable.renderLegacyIcon(canvas: Canvas, sizePx: Int) {
        canvas.drawColor(Color.WHITE)
        val oldBounds = copyBounds()
        val intrinsicWidth = intrinsicWidth
        val intrinsicHeight = intrinsicHeight
        val maxIconSize = (sizePx * LEGACY_ICON_SCALE).roundToInt()
        val iconWidth: Int
        val iconHeight: Int

        if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            val scale = minOf(
                maxIconSize / intrinsicWidth.toFloat(),
                maxIconSize / intrinsicHeight.toFloat()
            )
            iconWidth = (intrinsicWidth * scale).roundToInt().coerceAtLeast(1)
            iconHeight = (intrinsicHeight * scale).roundToInt().coerceAtLeast(1)
        } else {
            iconWidth = maxIconSize
            iconHeight = maxIconSize
        }

        val left = (sizePx - iconWidth) / 2
        val top = (sizePx - iconHeight) / 2
        setBounds(left, top, left + iconWidth, top + iconHeight)
        draw(canvas)
        bounds = oldBounds
    }

    private fun createWhiteIcon(sizePx: Int): ImageBitmap {
        return createBitmap(sizePx, sizePx).apply {
            eraseColor(Color.WHITE)
        }.asImageBitmap()
    }

    private fun createGenericShortcutIcon(sizePx: Int): ImageBitmap {
        return createBitmap(sizePx, sizePx).apply {
            val canvas = Canvas(this)
            canvas.drawColor(Color.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                style = Paint.Style.STROKE
                strokeWidth = (sizePx * 0.055f).coerceAtLeast(1f)
            }
            val inset = sizePx * 0.22f
            val bounds = RectF(inset, inset, sizePx - inset, sizePx - inset)
            canvas.drawOval(bounds, paint)
            canvas.drawOval(
                RectF(sizePx * 0.38f, inset, sizePx * 0.62f, sizePx - inset),
                paint
            )
            canvas.drawLine(inset, sizePx * 0.42f, sizePx - inset, sizePx * 0.42f, paint)
            canvas.drawLine(inset, sizePx * 0.58f, sizePx - inset, sizePx * 0.58f, paint)
        }.asImageBitmap()
    }

    private data class AppIconKey(
        val packageName: String,
        val itemType: AppItemType,
        val targetId: String,
        val userHandle: Int,
        val sizePx: Int
    )

    private companion object {
        const val MAX_CACHE_SIZE_KB = 16 * 1024
        const val MIN_CACHE_SIZE_KB = 2 * 1024
        const val BYTES_PER_PIXEL = 4
        const val DEFAULT_ICON_SIZE_DP = 48
        const val LEGACY_ICON_SCALE = 0.7f

        fun maxCacheSizeKb(): Int {
            val memoryFractionKb = Runtime.getRuntime().maxMemory() / 16 / 1024
            return memoryFractionKb.toInt().coerceIn(MIN_CACHE_SIZE_KB, MAX_CACHE_SIZE_KB)
        }
    }
}
