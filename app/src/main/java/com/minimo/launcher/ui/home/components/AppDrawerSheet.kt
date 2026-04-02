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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.components.SheetDragHandle
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
) {
    val context = LocalContext.current

    SheetDragHandle(
        isExpanded = state.isBottomSheetExpanded,
        isIconHidden = state.hideAppDrawerArrow
    )

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

    val nestedScrollConnection = remember(allAppsLazyListState) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0 && allAppsLazyListState.canScrollBackward) {
                    return Offset(0f, available.y)
                }
                return Offset.Zero
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
