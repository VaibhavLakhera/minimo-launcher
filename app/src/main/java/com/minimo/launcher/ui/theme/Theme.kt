package com.minimo.launcher.ui.theme

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.minimo.launcher.utils.AndroidUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme()

/** Resolves colors only; safe to use for previews without changing windows or wallpaper. */
@Composable
fun themeColorScheme(
    themeMode: ThemeMode,
    blackTheme: Boolean,
    useDynamicTheme: Boolean
): ColorScheme {
    themeMode.preset?.let { return it.colorScheme }

    val context = LocalContext.current
    val isDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Dark -> true
        else -> false
    }
    val isDynamicTheme = useDynamicTheme && AndroidUtils.isDynamicThemeSupported()
    val colorScheme = when {
        isDynamicTheme && isDarkTheme -> dynamicDarkColorScheme(context)
        isDynamicTheme -> dynamicLightColorScheme(context)
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    return if (isDarkTheme && blackTheme) {
        colorScheme.copy(onSurface = Color.White, surface = Color.Black)
    } else {
        colorScheme
    }
}

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    blackTheme: Boolean,
    useDynamicTheme: Boolean,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    setWallpaperToThemeColor: Boolean,
    enableWallpaper: Boolean,
    isHomeScreen: Boolean,
    lightTextOnWallpaper: Boolean,
    fontPreference: String = "",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = themeColorScheme(themeMode, blackTheme, useDynamicTheme)
    val useDarkSystemBarIcons = colorScheme.surface.luminance() > 0.5f

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val surfaceColor =
                if (enableWallpaper) Color.Transparent.toArgb() else colorScheme.surface.toArgb()
            window.statusBarColor = surfaceColor
            window.navigationBarColor = surfaceColor

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            val insetsController = WindowCompat.getInsetsController(window, view)

            if (enableWallpaper) {
                insetsController.isAppearanceLightStatusBars = !lightTextOnWallpaper
                if (!isHomeScreen) {
                    insetsController.isAppearanceLightNavigationBars = !lightTextOnWallpaper
                }
                // HomeScreen owns its navigation icons because the app drawer can cover that area.
            } else {
                insetsController.isAppearanceLightStatusBars = useDarkSystemBarIcons
                insetsController.isAppearanceLightNavigationBars = useDarkSystemBarIcons
            }

            if (!statusBarVisible || !navigationBarVisible) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            if (statusBarVisible) {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            } else {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
            }

            if (navigationBarVisible) {
                insetsController.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    if (setWallpaperToThemeColor) {
        // Only run when the background color changes, and execute setting the wallpaper on the IO thread.
        LaunchedEffect(colorScheme.surface) {
            withContext(Dispatchers.IO) {
                updateWallpaper(context, colorScheme.surface)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypographyForFont(fontPreference),
        content = content
    )
}

private fun updateWallpaper(context: Context, color: Color) {
    try {
        val wallpaperManager = WallpaperManager.getInstance(context)

        val bitmap = createBitmap(1, 1)
        bitmap[0, 0] = color.toArgb()

        wallpaperManager.setBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
