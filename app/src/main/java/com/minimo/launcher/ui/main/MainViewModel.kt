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
            preferenceHelper.getThemeMode()
                .distinctUntilChanged()
                .collect { mode ->
                    _state.update {
                        it.copy(themeMode = mode)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getFontPreference()
                .distinctUntilChanged()
                .collect { font ->
                    _state.update {
                        it.copy(fontPreference = font)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getShowStatusBar()
                .distinctUntilChanged()
                .collect { visible ->
                    _state.update {
                        it.copy(statusBarVisible = visible)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getDynamicTheme()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(useDynamicTheme = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getBlackTheme()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(blackTheme = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getSetWallpaperToThemeColor()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(setWallpaperToThemeColor = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getEnableWallpaper()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(enableWallpaper = enable)
                    }
                }
        }

        viewModelScope.launch {
            preferenceHelper.getLightTextOnWallpaper()
                .distinctUntilChanged()
                .collect { enable ->
                    _state.update {
                        it.copy(lightTextOnWallpaper = enable)
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