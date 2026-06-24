package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.home.HomeScreenState
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.launchAppInfo
import com.minimo.launcher.utils.uninstallApp
import kotlinx.coroutines.delay

private const val STALE_IME_INSET_TIMEOUT_MILLIS = 350L
private val BottomSearchKeyboardGap = 8.dp

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
    statusBarVisible: Boolean,
    useDarkBottomSheetStatusBarIcons: Boolean,
    useDarkBottomSheetNavigationBarIcons: Boolean,
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

    val showFastScroller = state.enableFastScroller && state.searchText.isBlank()
    val endContentPadding = if (state.enableFastScroller) 40.dp else 0.dp
    val startContentPadding =
        if (state.drawerAppsArrangementHorizontal == Arrangement.Start) 0.dp else endContentPadding

    Box(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        LazyColumn(
            state = allAppsLazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = systemNavigationHeight,
                start = startContentPadding,
                end = endContentPadding
            )
        ) {
            items(items = state.filteredAllApps, key = { it.id }) { appInfo ->
                if (appInfo.packageName == Constants.MINIMO_SETTINGS_PACKAGE) {
                    MinimoSettingsItem(
                        modifier = Modifier.animateItem(),
                        horizontalArrangement = state.drawerAppsArrangementHorizontal,
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
                        appsArrangement = state.drawerAppsArrangementHorizontal,
                        onLongClick = hideKeyboardWithClearFocus,
                        onUninstallClick = { context.uninstallApp(appInfo) },
                        textSize = if (state.applyHomeAppSizeToAllApps) state.homeTextSize.sp else 20.sp,
                        showNotificationDot = appInfo.showNotificationDot,
                        bottomSheetStatusBarVisible = statusBarVisible,
                        useDarkBottomSheetStatusBarIcons = useDarkBottomSheetStatusBarIcons,
                        useDarkBottomSheetNavigationBarIcons = useDarkBottomSheetNavigationBarIcons,
                        verticalPadding = state.homeAppVerticalPadding.dp
                    )
                }
            }
        }

        if (showFastScroller) {
            AppDrawerFastScroller(
                apps = state.filteredAllApps,
                listState = allAppsLazyListState,
                modifier = Modifier.align(Alignment.CenterEnd),
                onInteractionStart = hideKeyboardWithClearFocus
            )
        }
    }

    if (!state.hideAppDrawerSearch && state.drawerSearchBarAtBottom) {
        val bottomSearchImeExtraPadding = bottomSearchImeExtraPadding()

        Column(
            modifier = Modifier.padding(bottom = bottomSearchImeExtraPadding)
        ) {
            AppDrawerSearch(
                focusRequester = focusRequester,
                searchText = state.searchText,
                onSearchTextChange = viewModel::onSearchTextChange,
                onSettingsClick = {
                    hideKeyboardWithClearFocus()
                    onSettingsClick()
                }
            )

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun bottomSearchImeExtraPadding(): Dp {
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val imeOnlyBottom = (imeBottom - navigationBottom).coerceAtLeast(0)
    val isImeVisible = WindowInsets.isImeVisible

    var suppressStaleImePadding by remember { mutableStateOf(false) }

    LaunchedEffect(isImeVisible, imeOnlyBottom) {
        if (isImeVisible || imeOnlyBottom == 0) {
            suppressStaleImePadding = false
        } else {
            delay(STALE_IME_INSET_TIMEOUT_MILLIS)
            suppressStaleImePadding = true
        }
    }

    val effectiveImeBottom =
        if (!isImeVisible && suppressStaleImePadding) 0 else imeOnlyBottom
    val keyboardGap =
        if (isImeVisible && effectiveImeBottom > 0) BottomSearchKeyboardGap else 0.dp

    return with(density) { effectiveImeBottom.toDp() } + keyboardGap
}
