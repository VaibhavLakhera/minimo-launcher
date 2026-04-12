package com.minimo.launcher.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.utils.HomePressedNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper,
    private val homePressedNotifier: HomePressedNotifier
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    init {
        viewModelScope.launch {
            preferenceHelper.getMainPreferencesFlow()
                .distinctUntilChanged()
                .collect { prefs ->
                    _state.update {
                        it.copy(
                            themeMode = prefs.themeMode,
                            fontPreference = prefs.fontPreference,
                            statusBarVisible = prefs.showStatusBar,
                            useDynamicTheme = prefs.dynamicTheme,
                            blackTheme = prefs.blackTheme,
                            setWallpaperToThemeColor = prefs.setWallpaperToThemeColor,
                            enableWallpaper = prefs.enableWallpaper,
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