package com.minimo.launcher.ui.main

import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.ScreenOrientation

data class MainState(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontPreference: String = "",
    val screenOrientation: ScreenOrientation = ScreenOrientation.Portrait,
    val statusBarVisible: Boolean = true,
    val navigationBarVisible: Boolean = true,
    val useDynamicTheme: Boolean = false,
    val blackTheme: Boolean = false,
    val setWallpaperToThemeColor: Boolean = false,
    val enableWallpaper: Boolean = false,
    val lightTextOnWallpaper: Boolean = true,
)
