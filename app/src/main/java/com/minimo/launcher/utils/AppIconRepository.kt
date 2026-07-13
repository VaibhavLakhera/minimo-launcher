package com.minimo.launcher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.UserManager
import android.util.DisplayMetrics
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
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
    @ApplicationContext context: Context
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)

    // Prevents an in-flight load from restoring a stale icon after the cache was cleared or an
    // app's cached icons were removed. Loads only cache results from the generation they started in.
    private val cacheGeneration = AtomicInteger()
    private val loadSemaphore = Semaphore(permits = 2)

    // Reuses icons by component, user profile, and rendered size. Entries are measured in KB so
    // the cache stays within the memory-based limit returned by maxCacheSizeKb().
    private val iconCache = object : LruCache<AppIconKey, ImageBitmap>(maxCacheSizeKb()) {
        override fun sizeOf(key: AppIconKey, value: ImageBitmap): Int {
            return (value.width * value.height * BYTES_PER_PIXEL / 1024).coerceAtLeast(1)
        }
    }

    suspend fun loadIcon(
        packageName: String,
        className: String,
        userHandle: Int,
        sizePx: Int
    ): ImageBitmap? {
        if (sizePx <= 0) return null

        val key = AppIconKey(packageName, className, userHandle, sizePx)
        iconCache.get(key)?.let { return it }

        return loadSemaphore.withPermit {
            iconCache.get(key)?.let { return@withPermit it }
            val generation = cacheGeneration.get()

            withContext(Dispatchers.IO) {
                val loadedImage = runCatching {
                    val profile = userManager.userProfiles.firstOrNull {
                        it.hashCode() == userHandle
                    } ?: return@runCatching null

                    val activity = launcherApps.getActivityList(packageName, profile)
                        .firstOrNull { it.componentName.className == className }
                        ?: return@runCatching null

                    val requestedDensity = (sizePx * DisplayMetrics.DENSITY_DEFAULT /
                            DEFAULT_ICON_SIZE_DP.toFloat())
                        .toInt()
                        .coerceAtLeast(DisplayMetrics.DENSITY_LOW)
                    activity.getIcon(requestedDensity).renderToImageBitmap(sizePx)
                }.onFailure {
                    Timber.w(it, "Unable to load icon for %s/%s", packageName, className)
                }.getOrNull()
                val image = loadedImage ?: createWhiteIcon(sizePx)

                ensureActive()
                if (generation == cacheGeneration.get()) {
                    iconCache.put(key, image)
                }
                image
            }
        }
    }

    fun clear() {
        cacheGeneration.incrementAndGet()
        iconCache.evictAll()
    }

    fun removeIcon(packageName: String, userHandle: Int) {
        cacheGeneration.incrementAndGet()
        iconCache.snapshot().keys
            .filter { it.packageName == packageName && it.userHandle == userHandle }
            .forEach(iconCache::remove)
    }

    private fun Drawable.renderToImageBitmap(sizePx: Int): ImageBitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)

        if (this is AdaptiveIconDrawable) {
            // Android extends both 108dp layers beyond the visible viewport so their inner
            // safe zone fills the mask. Keep masking in Compose to support configurable shapes.
            val extraInset = (sizePx * AdaptiveIconDrawable.getExtraInsetFraction()).roundToInt()
            background.setBounds(-extraInset, -extraInset, sizePx + extraInset, sizePx + extraInset)
            background.draw(canvas)
            foreground.setBounds(-extraInset, -extraInset, sizePx + extraInset, sizePx + extraInset)
            foreground.draw(canvas)
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

    private data class AppIconKey(
        val packageName: String,
        val className: String,
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
