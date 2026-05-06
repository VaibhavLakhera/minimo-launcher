package com.minimo.launcher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.minimo.launcher.ui.components.RenameAppDialog
import com.minimo.launcher.ui.home.components.AppDrawerSheet
import com.minimo.launcher.ui.home.components.EmptyHomeBody
import com.minimo.launcher.ui.home.components.HomeBody
import com.minimo.launcher.utils.launchApp
import com.minimo.launcher.utils.launchAppFromPreference
import com.minimo.launcher.utils.lockScreen
import com.minimo.launcher.utils.showNotificationDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSettingsClick: () -> Unit,
    onAddFavouriteAppsClick: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val triggerHomePressed by viewModel.triggerHomePressed.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    val focusRequester = remember { FocusRequester() }

    val homeLazyListState = rememberLazyListState()
    val allAppsLazyListState = rememberLazyListState()

    // Calculate dynamic threshold based on window container height & width
    val screenSize = LocalWindowInfo.current.containerSize
    val swipeVerticalThresholdPx = screenSize.height * 0.8f // 8% of window height
    val swipeHorizontalThresholdPx = screenSize.width * 0.15f // 15% of window width

    val swipeUpThreshold = -swipeVerticalThresholdPx
    val swipeDownThreshold = swipeVerticalThresholdPx
    val swipeLeftThreshold = -swipeHorizontalThresholdPx
    val swipeRightThreshold = swipeHorizontalThresholdPx

    fun hideKeyboardWithClearFocus() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    BackHandler {
        coroutineScope.launch {
            if (bottomSheetScaffoldState.bottomSheetState.currentValue != SheetValue.PartiallyExpanded) {
                hideKeyboardWithClearFocus()
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
            } else {
                allAppsLazyListState.scrollToItem(0)
                bottomSheetScaffoldState.bottomSheetState.expand()
            }
        }
    }

    LaunchedEffect(triggerHomePressed) {
        if (triggerHomePressed) {
            if (bottomSheetScaffoldState.bottomSheetState.currentValue != SheetValue.PartiallyExpanded) {
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
            }
        }
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.launchApp.collect { app ->
                hideKeyboardWithClearFocus()
                context.launchApp(app.packageName, app.className, app.userHandle)
            }
        }
    }

    LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.targetValue) {
        when (bottomSheetScaffoldState.bottomSheetState.targetValue) {
            SheetValue.Expanded -> {
                if (!state.isBottomSheetExpanded) {
                    viewModel.setBottomSheetExpanded(true)

                    // Request focus only when the autoOpenKeyboardAllApps is true and drawer search bar is not hidden
                    if (state.autoOpenKeyboardAllApps && !state.hideAppDrawerSearch) {
                        // Add a small delay to let the bottom sheet animation get a head start.
                        // This prevents the focus request and keyboard layout from halting the sheet's expansion.
                        delay(state.keyboardOpenDelay)
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
            }

            else -> {
                delay(250)
                viewModel.setBottomSheetExpanded(false)
                focusManager.clearFocus()
            }
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

    var swipeYAccumulator by remember { mutableFloatStateOf(0f) }
    var swipeXAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(homeLazyListState, swipeVerticalThresholdPx) {
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
                    if (available.y > 0 && !homeLazyListState.canScrollBackward) {
                        swipeYAccumulator += available.y
                    } else if (available.y < 0 && !homeLazyListState.canScrollForward) {
                        swipeYAccumulator += available.y
                    } else {
                        swipeYAccumulator = 0f
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (swipeYAccumulator > swipeDownThreshold) {
                    context.showNotificationDrawer()
                } else if (swipeYAccumulator < swipeUpThreshold) {
                    coroutineScope.launch {
                        allAppsLazyListState.scrollToItem(0)
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                } else {
                    if (available.y > 1000f && !homeLazyListState.canScrollBackward) {
                        context.showNotificationDrawer()
                    } else if (available.y < -1000f && !homeLazyListState.canScrollForward) {
                        coroutineScope.launch {
                            allAppsLazyListState.scrollToItem(0)
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
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

    val systemNavigationHeight =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val safeDrawingTop =
        WindowInsets.statusBars.union(WindowInsets.ime).union(WindowInsets.displayCutout)

    val surfaceColor = MaterialTheme.colorScheme.surface

    val boxBackgroundColor = remember(state.enableWallpaper, state.dimWallpaper, surfaceColor) {
        if (state.enableWallpaper) {
            if (state.dimWallpaper) {
                Color.Black.copy(alpha = 0.20f)
            } else {
                Color.Transparent
            }
        } else {
            surfaceColor
        }
    }

    val scaffoldContainerColor = remember(state.enableWallpaper, surfaceColor) {
        if (state.enableWallpaper) {
            Color.Transparent
        } else {
            surfaceColor
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(boxBackgroundColor)
            .windowInsetsPadding(safeDrawingTop)
            .pointerInput(state.doubleTapToLock) {
                detectTapGestures(onDoubleTap = {
                    if (state.doubleTapToLock) {
                        context.lockScreen()
                    }
                })
            }
            .pointerInput(swipeHorizontalThresholdPx) {
                detectHorizontalDragGestures(
                    onDragStart = { swipeXAccumulator = 0f },
                    onDragEnd = {
                        if (swipeXAccumulator > swipeRightThreshold) {
                            context.launchAppFromPreference(state.swipeRightAppPreference)
                        } else if (swipeXAccumulator < swipeLeftThreshold) {
                            context.launchAppFromPreference(state.swipeLeftAppPreference)
                        }
                        swipeXAccumulator = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeXAccumulator += dragAmount
                    }
                )
            }
    ) {
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetSwipeEnabled = false,
            sheetDragHandle = null,
            sheetShadowElevation = 0.dp,
            sheetContent = {
                AppDrawerSheet(
                    state = state,
                    viewModel = viewModel,
                    focusRequester = focusRequester,
                    allAppsLazyListState = allAppsLazyListState,
                    systemNavigationHeight = systemNavigationHeight,
                    onSettingsClick = onSettingsClick,
                    hideKeyboardWithClearFocus = ::hideKeyboardWithClearFocus,
                    swipeDownThreshold = swipeDownThreshold,
                    onCloseSheet = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                )
            },
            sheetPeekHeight = 0.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            containerColor = scaffoldContainerColor
        ) { paddingValues ->
            if (state.initialLoaded && state.favouriteApps.isEmpty() && state.favouriteShortcuts.isEmpty()) {
                EmptyHomeBody(
                    paddingValues = paddingValues,
                    onAddFavouriteAppsClick = onAddFavouriteAppsClick
                )
            } else {
                HomeBody(
                    paddingValues = paddingValues,
                    state = state,
                    viewModel = viewModel,
                    homeLazyListState = homeLazyListState,
                    nestedScrollConnection = nestedScrollConnection,
                    systemNavigationHeight = systemNavigationHeight
                )
            }
        }
    }

    if (state.renameAppDialog != null) {
        val app = state.renameAppDialog!!
        RenameAppDialog(
            originalName = app.appName,
            currentName = app.name,
            onRenameClick = viewModel::onRenameApp,
            onCancelClick = viewModel::onDismissRenameAppDialog
        )
    }
}