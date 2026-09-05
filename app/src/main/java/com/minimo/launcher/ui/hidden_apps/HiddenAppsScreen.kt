package com.minimo.launcher.ui.hidden_apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.ToggleAppItem
import com.minimo.launcher.ui.home.components.SearchItem
import com.minimo.launcher.utils.launchApp
import com.minimo.launcher.utils.startShortcut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenAppsScreen(
    viewModel: HiddenAppsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.hidden_apps)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info_outline),
                            contentDescription = null
                        )
                    }

                    Box {
                        IconButton(onClick = {
                            viewModel.onToggleAppBarMorePopup()
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = state.showAppBarMorePopup,
                            onDismissRequest = viewModel::onToggleAppBarMorePopup
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.onToggleShowHiddenOnly()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.hidden_only),
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = state.showHiddenOnly,
                                    onCheckedChange = { viewModel.onToggleShowHiddenOnly() }
                                )
                            }
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.searchPreferencesLoaded) {
                SearchItem(
                    modifier = Modifier.fillMaxWidth(),
                    searchText = state.searchText,
                    onSearchTextChange = viewModel::onSearchTextChange,
                    searchBarBorderPercent = state.searchBarBorderPercent,
                    searchBarBackground = state.searchBarBackground
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(items = state.filteredAllApps, key = { it.id }) { appInfo ->
                    ToggleAppItem(
                        modifier = Modifier.animateItem(),
                        appName = appInfo.name,
                        isChecked = appInfo.isHidden,
                        isWorkProfile = appInfo.isWorkProfile,
                        onToggleClick = { viewModel.onToggleHiddenAppClick(appInfo) },
                        onLongClick = {
                            if (appInfo.isShortcut) {
                                context.startShortcut(
                                    appInfo.packageName,
                                    appInfo.targetId,
                                    appInfo.userHandle
                                )
                            } else {
                                context.launchApp(
                                    appInfo.packageName,
                                    appInfo.targetId,
                                    appInfo.userHandle
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            text = {
                Text(
                    text = stringResource(R.string.hidden_apps_long_press_hint),
                    fontSize = 18.sp
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 18.sp
                    )
                }
            }
        )
    }
}
