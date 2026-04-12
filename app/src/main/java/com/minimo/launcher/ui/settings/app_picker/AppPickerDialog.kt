package com.minimo.launcher.ui.settings.app_picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.home.components.SearchItem
import com.minimo.launcher.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerDialog(
    viewModel: AppPickerViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
    onAppSelected: (AppInfo) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onSearchTextChange("")
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(R.string.select_app))
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
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
                SearchItem(
                    modifier = Modifier.fillMaxWidth(),
                    searchText = state.searchText,
                    onSearchTextChange = viewModel::onSearchTextChange
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    items(items = state.filteredApps, key = { it.id }) { appInfo ->
                        AppPickerItem(
                            modifier = Modifier.animateItem(),
                            appName = appInfo.name,
                            isWorkProfile = appInfo.isWorkProfile,
                            onClick = { onAppSelected(appInfo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppPickerItem(
    modifier: Modifier = Modifier,
    appName: String,
    isWorkProfile: Boolean,
    onClick: () -> Unit
) {
    val paddingValues = remember(isWorkProfile) {
        if (isWorkProfile) {
            PaddingValues(
                start = 0.dp,
                end = Dimens.APP_HORIZONTAL_SPACING,
                top = 16.dp,
                bottom = 16.dp
            )
        } else {
            PaddingValues(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 16.dp)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isWorkProfile) {
            Icon(
                painter = painterResource(id = R.drawable.ic_work_profile),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = appName,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
