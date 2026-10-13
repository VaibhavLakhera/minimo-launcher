package com.minimo.launcher.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.CustomFonts
import com.minimo.launcher.data.FontUse
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.utils.HomePressedNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper,
    private val homePressedNotifier: HomePressedNotifier,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    init {
        viewModelScope.launch {
            preferenceHelper.getMainPreferencesFlow()
                .distinctUntilChanged()
                .collect { prefs ->
                    // Load a custom font before publishing it so the first frame already uses it.
                    CustomFonts.activate(context, FontUse.App, prefs.fontPreference)
                    _state.update {
                        it.copy(
                            initialLoaded = true,
                            themeMode = prefs.themeMode,
                            fontPreference = prefs.fontPreference,
                            screenOrientation = prefs.screenOrientation,
                            statusBarVisible = prefs.showStatusBar,
                            navigationBarVisible = prefs.showNavigationBar,
                            useDynamicTheme = prefs.dynamicTheme,
                            blackTheme = prefs.blackTheme,
                            setWallpaperToThemeColor = prefs.setWallpaperToThemeColor,
                            enableWallpaper = prefs.enableWallpaper,
                            enableWallpaperOnDrawer = prefs.enableWallpaperOnDrawer,
                            dimWallpaper = prefs.dimWallpaper,
                            dimWallpaperPercentage = prefs.dimWallpaperPercentage,
                            lightTextOnWallpaper = prefs.lightTextOnWallpaper
                        )
                    }
                }
        }
    }

    fun onHomeButtonPressed() {
        viewModelScope.launch {
            homePressedNotifier.notifyHomePressed()
        }
    }

    fun getHomePressedNotifier() = homePressedNotifier
}
