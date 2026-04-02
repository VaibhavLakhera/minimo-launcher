package com.minimo.launcher.ui.main

import com.minimo.launcher.ui.theme.ThemeMode

data class MainState(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontPreference: String = "",
    val statusBarVisible: Boolean = true,
    val useDynamicTheme: Boolean = false,
    val blackTheme: Boolean = false,
    val setWallpaperToThemeColor: Boolean = false,
    val enableWallpaper: Boolean = false,
    val lightTextOnWallpaper: Boolean = true,
)