package com.minimo.launcher.ui.settings.web_shortcuts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.RenameDialog
import com.minimo.launcher.ui.home.components.SearchItem
import com.minimo.launcher.utils.openHomeSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebShortcutsScreen(
    onBackClick: () -> Unit
) {
    val viewModel: WebShortcutsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.web_shortcuts)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_icon)
                        )
                    }
                },
                actions = {
                    if (state.initialLoaded && state.allShortcuts.isNotEmpty()) {
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.how_to_add_shortcut)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        if (!state.hasShortcutHostPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.default_launcher_required_shortcuts),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { context.openHomeSettings() }) {
                    Text(stringResource(R.string.set_default_launcher))
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        } else if (state.initialLoaded) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (state.allShortcuts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.how_to_add_shortcut),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        SearchItem(
                            modifier = Modifier.fillMaxWidth(),
                            searchText = state.searchText,
                            onSearchTextChange = viewModel::onSearchTextChange,
                            placeholderText = stringResource(R.string.search)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp)
                        ) {
                            items(
                                items = state.filteredAllShortcuts,
                                key = { it.id }) { shortcutInfo ->
                                WebShortcutItem(
                                    modifier = Modifier.animateItem(),
                                    shortcutInfo = shortcutInfo,
                                    onToggleFavouriteClick = {
                                        viewModel.onToggleFavouriteShortcutClick(shortcutInfo)
                                    },
                                    onRenameClick = {
                                        viewModel.onRenameClick(shortcutInfo)
                                    },
                                    onDeleteClick = {
                                        viewModel.onDeleteClick(shortcutInfo)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(stringResource(R.string.web_shortcuts))
            },
            text = {
                Text(
                    stringResource(R.string.how_to_add_shortcut),
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showInfoDialog = false }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        fontSize = 18.sp
                    )
                }
            }
        )
    }

    state.shortcutToRename?.let { shortcut ->
        RenameDialog(
            title = stringResource(R.string.rename_shortcut),
            label = stringResource(R.string.shortcut_name),
            originalName = shortcut.shortcutName,
            currentName = shortcut.displayName,
            onRenameClick = viewModel::onConfirmRename,
            onCancelClick = viewModel::onCancelRename
        )
    }

    state.shortcutToDelete?.let {
        AlertDialog(
            onDismissRequest = viewModel::onCancelDelete,
            text = {
                Text(
                    text = stringResource(R.string.delete_shortcut_confirmation),
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(onClick = viewModel::onConfirmDelete) {
                    Text(
                        stringResource(R.string.delete)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelDelete) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
            }
        )
    }
}