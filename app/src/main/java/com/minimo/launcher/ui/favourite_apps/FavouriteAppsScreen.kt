package com.minimo.launcher.ui.favourite_apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.AppButton
import com.minimo.launcher.ui.components.ToggleAppItem
import com.minimo.launcher.ui.home.components.SearchItem
import com.minimo.launcher.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteAppsScreen(
    viewModel: FavouriteAppsViewModel,
    onBackClick: () -> Unit,
    onReorderClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.favourite_apps)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = {
                            viewModel.onToggleAppBarMorePopup()
                        }) {
                            Icon(
                                Icons.Default.MoreVert,
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
                                        viewModel.onToggleShowFavouritesOnly()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.favourites_only),
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = state.showFavouritesOnly,
                                    onCheckedChange = { viewModel.onToggleShowFavouritesOnly() }
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
            SearchItem(
                modifier = Modifier.fillMaxWidth(),
                searchText = state.searchText,
                onSearchTextChange = viewModel::onSearchTextChange
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(items = state.filteredAllApps, key = { it.id }) { appInfo ->
                    ToggleAppItem(
                        modifier = Modifier.animateItem(),
                        appName = appInfo.name,
                        isChecked = appInfo.isFavourite,
                        isWorkProfile = appInfo.isWorkProfile,
                        onToggleClick = { viewModel.onToggleFavouriteAppClick(appInfo) }
                    )
                }
            }

            AnimatedVisibility(
                state.showReorderButton,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                AppButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            bottom = 8.dp,
                            start = Dimens.APP_HORIZONTAL_SPACING,
                            end = Dimens.APP_HORIZONTAL_SPACING
                        ),
                    onClick = onReorderClick,
                    text = stringResource(R.string.reorder_favourites)
                )
            }
        }
    }
}