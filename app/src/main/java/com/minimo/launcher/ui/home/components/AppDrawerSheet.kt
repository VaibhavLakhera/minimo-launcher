package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.home.HomeScreenState
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.launchAppInfo
import com.minimo.launcher.utils.uninstallApp

@Composable
fun ColumnScope.AppDrawerSheet(
    state: HomeScreenState,
    viewModel: HomeViewModel,
    focusRequester: FocusRequester,
    allAppsLazyListState: LazyListState,
    systemNavigationHeight: Dp,
    onSettingsClick: () -> Unit,
    hideKeyboardWithClearFocus: () -> Unit,
    swipeDownThreshold: Float,
    onCloseSheet: () -> Unit,
) {
    val context = LocalContext.current

    // For blank space at the top of the drawer sheet
    Spacer(modifier = Modifier.height(32.dp))

    if (!state.hideAppDrawerSearch && !state.drawerSearchBarAtBottom) {
        AppDrawerSearch(
            focusRequester = focusRequester,
            searchText = state.searchText,
            onSearchTextChange = viewModel::onSearchTextChange,
            onSettingsClick = {
                hideKeyboardWithClearFocus()
                onSettingsClick()
            }
        )
    }

    var swipeYAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(allAppsLazyListState, swipeDownThreshold) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (available.y > 0 && !allAppsLazyListState.canScrollBackward) {
                        swipeYAccumulator += available.y
                    } else if (available.y < 0 && !allAppsLazyListState.canScrollForward) {
                        swipeYAccumulator += available.y
                    } else {
                        swipeYAccumulator = 0f
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (swipeYAccumulator > swipeDownThreshold) {
                    onCloseSheet()
                } else if (available.y > 1000f && !allAppsLazyListState.canScrollBackward) {
                    onCloseSheet()
                }
                swipeYAccumulator = 0f
                return super.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                swipeYAccumulator = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    LazyColumn(
        state = allAppsLazyListState,
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(top = 16.dp, bottom = systemNavigationHeight)
    ) {
        items(items = state.filteredAllApps, key = { it.id }) { appInfo ->
            if (appInfo.packageName == Constants.MINIMO_SETTINGS_PACKAGE) {
                MinimoSettingsItem(
                    modifier = Modifier.animateItem(),
                    horizontalArrangement = state.appsArrangementHorizontal,
                    textSize = if (state.applyHomeAppSizeToAllApps) state.homeTextSize.sp else 20.sp,
                    onClick = {
                        hideKeyboardWithClearFocus()
                        onSettingsClick()
                    },
                    verticalPadding = state.homeAppVerticalPadding.dp
                )
            } else {
                AppNameItem(
                    modifier = Modifier.animateItem(),
                    appName = appInfo.name,
                    isFavourite = appInfo.isFavourite,
                    isHidden = appInfo.isHidden,
                    isWorkProfile = appInfo.isWorkProfile,
                    onClick = {
                        viewModel.onLaunchAppClick(appInfo)
                    },
                    onToggleFavouriteClick = { viewModel.onToggleFavouriteAppClick(appInfo) },
                    onRenameClick = { viewModel.onRenameAppClick(appInfo) },
                    onToggleHideClick = { viewModel.onToggleHideClick(appInfo) },
                    onAppInfoClick = { context.launchAppInfo(appInfo) },
                    appsArrangement = state.appsArrangementHorizontal,
                    onLongClick = hideKeyboardWithClearFocus,
                    onUninstallClick = { context.uninstallApp(appInfo) },
                    textSize = if (state.applyHomeAppSizeToAllApps) state.homeTextSize.sp else 20.sp,
                    showNotificationDot = appInfo.showNotificationDot,
                    verticalPadding = state.homeAppVerticalPadding.dp
                )
            }
        }
    }

    if (!state.hideAppDrawerSearch && state.drawerSearchBarAtBottom) {
        AppDrawerSearch(
            focusRequester = focusRequester,
            searchText = state.searchText,
            onSearchTextChange = viewModel::onSearchTextChange,
            onSettingsClick = {
                hideKeyboardWithClearFocus()
                onSettingsClick()
            }
        )

        Spacer(modifier = Modifier.height(systemNavigationHeight))
    }
}
