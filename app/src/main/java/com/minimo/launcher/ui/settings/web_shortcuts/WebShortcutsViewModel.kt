package com.minimo.launcher.ui.settings.web_shortcuts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.ShortcutInfoDao
import com.minimo.launcher.data.usecase.UpdateAllShortcutsUseCase
import com.minimo.launcher.ui.entities.ShortcutInfo
import com.minimo.launcher.utils.ShortcutsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WebShortcutsViewModel @Inject constructor(
    private val shortcutInfoDao: ShortcutInfoDao,
    private val shortcutsUtils: ShortcutsUtils,
    private val updateAllShortcutsUseCase: UpdateAllShortcutsUseCase
) : ViewModel() {
    private val _state =
        MutableStateFlow(WebShortcutsState(hasShortcutHostPermission = shortcutsUtils.hasShortcutHostPermission()))
    val state: StateFlow<WebShortcutsState> = _state

    init {
        viewModelScope.launch {
            shortcutInfoDao.getAllShortcutsFlow()
                .mapLatest { entities ->
                    shortcutsUtils.mapToShortcutInfo(entities)
                }
                .flowOn(Dispatchers.IO)
                .collect { allApps ->
                    _state.update { currentState ->
                        currentState.copy(
                            initialLoaded = true,
                            allShortcuts = allApps,
                            filteredAllShortcuts = shortcutsUtils.getShortcutsWithSearch(
                                searchText = currentState.searchText,
                                apps = allApps
                            )
                        )
                    }
                }
        }

        if (_state.value.hasShortcutHostPermission) {
            viewModelScope.launch {
                updateAllShortcutsUseCase.invoke()
            }
        }
    }

    fun onSearchTextChange(searchText: String) {
        val currentAllApps = _state.value.allShortcuts

        _state.update {
            it.copy(
                searchText = searchText,
                filteredAllShortcuts = shortcutsUtils.getShortcutsWithSearch(
                    searchText = searchText,
                    apps = currentAllApps
                )
            )
        }
    }

    fun onToggleFavouriteShortcutClick(shortcutInfo: ShortcutInfo) {
        viewModelScope.launch {
            if (shortcutInfo.isFavourite) {
                shortcutInfoDao.removeShortcutFromFavourite(
                    shortcutInfo.packageName,
                    shortcutInfo.shortcutId,
                    shortcutInfo.userHandle
                )
            } else {
                shortcutInfoDao.addShortcutToFavourite(
                    shortcutInfo.packageName,
                    shortcutInfo.shortcutId,
                    shortcutInfo.userHandle
                )
            }
        }
    }

    fun onRenameClick(shortcutInfo: ShortcutInfo) {
        _state.update {
            it.copy(shortcutToRename = shortcutInfo)
        }
    }

    fun onDeleteClick(shortcutInfo: ShortcutInfo) {
        _state.update {
            it.copy(shortcutToDelete = shortcutInfo)
        }
    }

    fun onCancelRename() {
        _state.update {
            it.copy(shortcutToRename = null)
        }
    }

    fun onCancelDelete() {
        _state.update {
            it.copy(shortcutToDelete = null)
        }
    }

    fun onConfirmRename(newName: String) {
        val shortcut = _state.value.shortcutToRename ?: return
        val name = if (newName.isBlank() || newName == shortcut.shortcutName) {
            ""
        } else {
            newName
        }
        viewModelScope.launch {
            shortcutInfoDao.renameShortcut(
                shortcut.packageName,
                shortcut.shortcutId,
                shortcut.userHandle,
                name
            )
            _state.update {
                it.copy(shortcutToRename = null)
            }
        }
    }

    fun onConfirmDelete() {
        val shortcut = _state.value.shortcutToDelete ?: return
        viewModelScope.launch {
            shortcutsUtils.deleteShortcut(
                shortcut.packageName,
                shortcut.shortcutId,
                shortcut.userHandle
            )
            shortcutInfoDao.deleteShortcut(
                shortcut.packageName,
                shortcut.shortcutId,
                shortcut.userHandle
            )
            _state.update {
                it.copy(shortcutToDelete = null)
            }
        }
    }
}