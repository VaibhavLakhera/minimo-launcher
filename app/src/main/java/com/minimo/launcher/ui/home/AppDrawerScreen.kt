package com.minimo.launcher.ui.home

import android.app.Activity
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.RenameDialog
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.home.components.AppDrawerFastScroller
import com.minimo.launcher.ui.home.components.AppDrawerSearch
import com.minimo.launcher.ui.home.components.AppLaunchConfirmationDialog
import com.minimo.launcher.ui.home.components.AppNameItem
import com.minimo.launcher.ui.home.components.LaunchDelayDialog
import com.minimo.launcher.ui.home.components.MinimoSettingsItem
import com.minimo.launcher.ui.home.components.appIconSizeFor
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.FastScrollerAlignment
import com.minimo.launcher.utils.launchAppInfo
import com.minimo.launcher.utils.uninstallApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration.Companion.milliseconds

private const val POINTER_DRAG_CLOSE_GRACE_MILLIS = 150L

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppDrawerScreen(
    viewModel: HomeViewModel,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    onCloseAppDrawer: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val iconCacheRevision by viewModel.iconCacheRevision.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val allAppsLazyListState = rememberLazyListState()
    val windowInfo = LocalWindowInfo.current
    val dragDownCloseThresholdPx = windowInfo.containerSize.height * 0.15f
    var pointerDragCloseEnabled by remember { mutableStateOf(false) }
    var autoOpenKeyboardHandled by rememberSaveable { mutableStateOf(false) }
    var shortcutToDelete by remember { mutableStateOf<AppInfo?>(null) }
    val searchVisible = state.searchPreferencesLoaded && !state.hideAppDrawerSearch
    val bottomSearchVisible = searchVisible && state.drawerSearchBarAtBottom

    fun hideKeyboardWithClearFocus() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    fun closeAppDrawer() {
        hideKeyboardWithClearFocus()
        viewModel.onAppDrawerClosed()
        onCloseAppDrawer()
    }

    DisposableEffect(Unit) {
        onDispose {
            hideKeyboardWithClearFocus()
            viewModel.onAppDrawerClosed()
        }
    }

    LaunchedEffect(
        searchVisible,
        state.autoOpenKeyboardAllApps
    ) {
        if (!searchVisible || autoOpenKeyboardHandled) return@LaunchedEffect

        autoOpenKeyboardHandled = true
        if (state.autoOpenKeyboardAllApps) {
            delay(state.keyboardOpenDelay.milliseconds)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(state.launchConfirmDialog?.deadlineElapsedRealtimeMillis) {
        if (state.launchConfirmDialog != null) {
            hideKeyboardWithClearFocus()
        }
    }

    LaunchedEffect(allAppsLazyListState) {
        snapshotFlow { allAppsLazyListState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (isScrolling && allAppsLazyListState.canScrollBackward) {
                    hideKeyboardWithClearFocus()
                }
            }
    }

    val enableWallpaper = state.enableWallpaperOnDrawer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val useDarkIconsOnSurface = useDarkIconsOnColor(surfaceColor)
    val useDarkSystemBarIcons = if (enableWallpaper) {
        !state.lightTextOnWallpaper
    } else {
        useDarkIconsOnSurface
    }

    ApplySystemBarIconColor(useDarkSystemBarIcons)

    val textColor = remember(
        enableWallpaper,
        state.lightTextOnWallpaper,
        onSurfaceColor
    ) {
        if (enableWallpaper) {
            if (state.lightTextOnWallpaper) Color.White else Color.Black
        } else {
            onSurfaceColor
        }
    }
    val textShadow = remember(enableWallpaper, state.lightTextOnWallpaper) {
        if (enableWallpaper && state.lightTextOnWallpaper) {
            Shadow(
                color = Color.Black.copy(alpha = 0.5f),
                offset = Offset(2f, 2f),
                blurRadius = 4f
            )
        } else {
            null
        }
    }
    val wallpaperContentColor = if (enableWallpaper) textColor else null

    var swipeYAccumulator by remember { mutableFloatStateOf(0f) }
    val closeGestureEnabledState = rememberUpdatedState(pointerDragCloseEnabled)
    val onCloseAppDrawerState = rememberUpdatedState(::closeAppDrawer)
    val onSettingsClickState = rememberUpdatedState {
        hideKeyboardWithClearFocus()
        onSettingsClick()
    }
    val nestedScrollConnection = remember(allAppsLazyListState, dragDownCloseThresholdPx) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!closeGestureEnabledState.value || source != NestedScrollSource.UserInput) {
                    swipeYAccumulator = 0f
                    return Offset.Zero
                }

                if (available.y > 0 && !allAppsLazyListState.canScrollBackward) {
                    swipeYAccumulator += available.y
                } else {
                    swipeYAccumulator = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (closeGestureEnabledState.value &&
                    !allAppsLazyListState.canScrollBackward &&
                    (swipeYAccumulator > dragDownCloseThresholdPx || available.y > 1000f)
                ) {
                    onCloseAppDrawerState.value()
                }
                swipeYAccumulator = 0f
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                swipeYAccumulator = 0f
                return Velocity.Zero
            }
        }
    }

    val showFastScroller =
        state.enableFastScroller && state.searchText.isBlank() && state.filteredAllApps.isNotEmpty()
    val fastScrollerContentPadding = if (state.enableFastScroller) 40.dp else 0.dp
    val fastScrollerAtStart = state.fastScrollerAlignment == FastScrollerAlignment.Left
    val startContentPadding = if (
        !fastScrollerAtStart && state.drawerAppsArrangementHorizontal == Arrangement.Start
    ) {
        0.dp
    } else {
        fastScrollerContentPadding
    }
    val endContentPadding = if (
        fastScrollerAtStart && state.drawerAppsArrangementHorizontal == Arrangement.End
    ) {
        0.dp
    } else {
        fastScrollerContentPadding
    }
    val textSize = if (state.applyHomeAppSizeToAllApps) {
        state.homeTextSize.sp
    } else {
        20.sp
    }
    val appIconSizeScale = state.appIconSizePercent / 100f
    val iconSizePx = with(LocalDensity.current) {
        appIconSizeFor(textSize, appIconSizeScale).roundToPx()
    }
    val verticalPadding = state.homeAppVerticalPadding.dp
    val showAppIcon = state.showAppIconInDrawer
    val appIconAlignment = state.drawerAppIconAlignment
    val appsArrangement = state.drawerAppsArrangementHorizontal
    val showSettingsIcon = !state.hideSettingsIcon
    val onKeyboardDone: (() -> Unit)? = if (state.keyboardDoneOpensFirstApp) {
        {
            hideKeyboardWithClearFocus()
            viewModel.onKeyboardDone()
        }
    } else {
        null
    }
    val onDrawerSettingsClick = onSettingsClickState.value

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .markPointerDragForDrawerClose(
                onPointerDragCloseEnabledChange = { pointerDragCloseEnabled = it }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onSettingsClickState.value() }
                )
            },
        containerColor = Color.Transparent,
        contentWindowInsets = if (bottomSearchVisible) {
            ScaffoldDefaults
                .contentWindowInsets
                .exclude(WindowInsets.navigationBars)
        } else {
            ScaffoldDefaults.contentWindowInsets
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .windowInsetsPadding(WindowInsets.imeAnimationTarget)
        ) {
            // For blank space at the top of the drawer screen.
            Spacer(modifier = Modifier.height(16.dp))

            if (searchVisible && !state.drawerSearchBarAtBottom) {
                AppDrawerSearch(
                    focusRequester = focusRequester,
                    searchText = state.searchText,
                    onSearchTextChange = viewModel::onSearchTextChange,
                    searchBarBorderPercent = state.searchBarBorderPercent,
                    searchBarBackground = state.searchBarBackground,
                    onKeyboardDone = onKeyboardDone,
                    showSettingsIcon = showSettingsIcon,
                    onSettingsClick = onDrawerSettingsClick,
                    wallpaperContentColor = wallpaperContentColor,
                    wallpaperTextShadow = textShadow
                )
            }

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
                        bottom = 16.dp,
                        start = startContentPadding,
                        end = endContentPadding
                    )
                ) {
                    items(items = state.filteredAllApps, key = { it.id }) { appInfo ->
                        if (appInfo.packageName == Constants.MINIMO_SETTINGS_PACKAGE) {
                            MinimoSettingsItem(
                                modifier = Modifier.animateItem(),
                                horizontalArrangement = appsArrangement,
                                textSize = textSize,
                                onClick = onDrawerSettingsClick,
                                compactTouchArea = state.compactAppTouchArea,
                                verticalPadding = verticalPadding,
                                showAppIcon = showAppIcon,
                                appIconSizeScale = appIconSizeScale,
                                appIconAlignment = appIconAlignment,
                                textColor = textColor,
                                textShadow = textShadow
                            )
                        } else {
                            val appIcon by produceState<ImageBitmap?>(
                                initialValue = null,
                                key1 = showAppIcon,
                                key2 = appInfo.id,
                                key3 = iconSizePx to iconCacheRevision
                            ) {
                                if (showAppIcon) {
                                    value = viewModel.loadAppIcon(appInfo, iconSizePx)
                                }
                            }

                            AppNameItem(
                                modifier = Modifier.animateItem(),
                                appName = appInfo.name,
                                isFavourite = appInfo.isFavourite,
                                isHidden = appInfo.isHidden,
                                isShortcut = appInfo.isShortcut,
                                isWorkProfile = appInfo.isWorkProfile,
                                onClick = {
                                    hideKeyboardWithClearFocus()
                                    viewModel.onAppLaunchRequest(appInfo)
                                },
                                onToggleFavouriteClick = {
                                    viewModel.onToggleFavouriteAppClick(appInfo)
                                },
                                onRenameClick = { viewModel.onRenameAppClick(appInfo) },
                                onToggleHideClick = { viewModel.onToggleHideClick(appInfo) },
                                onAppInfoClick = { context.launchAppInfo(appInfo) },
                                onLaunchDelayClick = {
                                    viewModel.onLaunchDelayClick(appInfo)
                                },
                                appsArrangement = appsArrangement,
                                onLongClick = ::hideKeyboardWithClearFocus,
                                onUninstallClick = { context.uninstallApp(appInfo) },
                                onDeleteShortcutClick = { shortcutToDelete = appInfo },
                                textSize = textSize,
                                showNotificationDot = appInfo.showNotificationDot,
                                compactTouchArea = state.compactAppTouchArea,
                                showAppIcon = showAppIcon,
                                appIcon = appIcon,
                                appIconSizeScale = appIconSizeScale,
                                appIconAlignment = appIconAlignment,
                                bottomSheetStatusBarVisible = statusBarVisible,
                                bottomSheetNavigationBarVisible = navigationBarVisible,
                                useDarkBottomSheetStatusBarIcons = useDarkIconsOnSurface,
                                useDarkBottomSheetNavigationBarIcons = useDarkIconsOnSurface,
                                verticalPadding = verticalPadding,
                                textColor = textColor,
                                shadow = textShadow
                            )
                        }
                    }
                }

                if (showFastScroller) {
                    AppDrawerFastScroller(
                        apps = state.filteredAllApps,
                        listState = allAppsLazyListState,
                        modifier = Modifier.align(
                            if (fastScrollerAtStart) {
                                Alignment.CenterStart
                            } else {
                                Alignment.CenterEnd
                            }
                        ),
                        isAtStart = fastScrollerAtStart,
                        onInteractionStart = ::hideKeyboardWithClearFocus,
                        textColor = textColor,
                        textShadow = textShadow,
                        indicatorColor = if (enableWallpaper) {
                            textColor
                        } else {
                            onSurfaceColor
                        },
                        indicatorTextColor = if (enableWallpaper) {
                            if (state.lightTextOnWallpaper) Color.Black else Color.White
                        } else {
                            surfaceColor
                        }
                    )
                }
            }

            if (bottomSearchVisible) {
                AppDrawerSearch(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp),
                    focusRequester = focusRequester,
                    searchText = state.searchText,
                    onSearchTextChange = viewModel::onSearchTextChange,
                    searchBarBorderPercent = state.searchBarBorderPercent,
                    searchBarBackground = state.searchBarBackground,
                    onKeyboardDone = onKeyboardDone,
                    showSettingsIcon = showSettingsIcon,
                    onSettingsClick = onDrawerSettingsClick,
                    wallpaperContentColor = wallpaperContentColor,
                    wallpaperTextShadow = textShadow
                )
            }
        }
    }

    if (state.renameAppDialog != null) {
        val app = state.renameAppDialog!!
        RenameDialog(
            title = stringResource(
                if (app.isShortcut) R.string.rename_shortcut else R.string.rename_app
            ),
            label = stringResource(
                if (app.isShortcut) R.string.shortcut_name else R.string.app_name_label
            ),
            originalName = app.appName,
            currentName = app.name,
            onRenameClick = viewModel::onRenameApp,
            onCancelClick = viewModel::onDismissRenameAppDialog
        )
    }

    state.launchDelayDialog?.let { app ->
        LaunchDelayDialog(
            app = app,
            onSave = viewModel::onUpdateLaunchDelay,
            onDismiss = viewModel::onDismissLaunchDelayDialog
        )
    }

    state.launchConfirmDialog?.let { pendingLaunch ->
        AppLaunchConfirmationDialog(
            app = pendingLaunch.app,
            deadlineElapsedRealtimeMillis = pendingLaunch.deadlineElapsedRealtimeMillis,
            onLaunch = viewModel::onConfirmAppLaunch,
            onDismiss = viewModel::onDismissAppLaunch
        )
    }

    shortcutToDelete?.let { shortcut ->
        DeleteShortcutConfirmationDialog(
            onConfirm = {
                shortcutToDelete = null
                viewModel.onConfirmDeleteShortcut(shortcut)
            },
            onDismiss = { shortcutToDelete = null }
        )
    }
}

private fun Modifier.markPointerDragForDrawerClose(
    onPointerDragCloseEnabledChange: (Boolean) -> Unit
): Modifier {
    return pointerInput(Unit) {
        while (true) {
            awaitPointerEventScope {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                onPointerDragCloseEnabledChange(true)

                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                } while (event.changes.any { it.pressed })
            }

            delay(POINTER_DRAG_CLOSE_GRACE_MILLIS.milliseconds)
            onPointerDragCloseEnabledChange(false)
        }
    }
}

@Composable
private fun ApplySystemBarIconColor(useDarkSystemBarIcons: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, useDarkSystemBarIcons) {
        if (!view.isInEditMode) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = useDarkSystemBarIcons
            insetsController.isAppearanceLightNavigationBars = useDarkSystemBarIcons
        }
        onDispose { }
    }
}

private fun useDarkIconsOnColor(color: Color): Boolean = color.luminance() > 0.5f
