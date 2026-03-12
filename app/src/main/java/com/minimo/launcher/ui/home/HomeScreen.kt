package com.minimo.launcher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
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
import com.minimo.launcher.utils.lockScreen
import com.minimo.launcher.utils.showNotificationDrawer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val swipeUpThreshold = -60f
private const val swipeDownThreshold = 60f

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

    fun hideKeyboardWithClearFocus() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    BackHandler {
        coroutineScope.launch {
            if (bottomSheetScaffoldState.bottomSheetState.currentValue != SheetValue.PartiallyExpanded) {
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
            } else {
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

    LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.currentValue) {
        when (bottomSheetScaffoldState.bottomSheetState.currentValue) {
            SheetValue.Expanded -> {
                if (!state.isBottomSheetExpanded) {
                    viewModel.setBottomSheetExpanded(true)

                    // Request focus only when the autoOpenKeyboardAllApps is true and drawer search bar is not hidden
                    if (state.autoOpenKeyboardAllApps && !state.hideAppDrawerSearch) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
            }

            else -> {
                viewModel.setBottomSheetExpanded(false)
                focusManager.clearFocus()
                allAppsLazyListState.scrollToItem(0)
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

    /**
     * 0 -> No swipe
     * -1 -> Down swipe
     * 1 -> Up swipe
     * */
    var swipeYDirection by remember { mutableIntStateOf(0) }

    // Temporary fix for swipe gestures when the LazyColumn is scrollable
    val swipeMultiplier by remember {
        derivedStateOf {
            if (!homeLazyListState.canScrollBackward && !homeLazyListState.canScrollForward) {
                1.0f
            } else {
                0.18f
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // Called while swipe is ongoing
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!homeLazyListState.canScrollBackward && available.y > (swipeDownThreshold * swipeMultiplier)) {
                    swipeYDirection = -1
                } else if (!homeLazyListState.canScrollForward && available.y < (swipeUpThreshold * swipeMultiplier)) {
                    swipeYDirection = 1
                }
                return Offset.Zero
            }

            // Called when the gesture is finishing
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (swipeYDirection < 0) {
                    context.showNotificationDrawer()
                } else if (swipeYDirection > 0) {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                }
                // Reset after gesture completes.
                swipeYDirection = 0
                return super.onPreFling(available)
            }

            // Reset the value after fling completes
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                swipeYDirection = 0
                return super.onPostFling(consumed, available)
            }
        }
    }

    val systemNavigationHeight =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val sheetPeekHeight = if (state.hideAppDrawerArrow || state.enableWallpaper) {
        // Don't peek the bottom sheet if either the hideAppDrawerArrow or enableWallpaper is true
        0.dp
    } else {
        56.dp + systemNavigationHeight
    }

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
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (state.doubleTapToLock) {
                        context.lockScreen()
                    }
                })
            }
    ) {
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
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
                    hideKeyboardWithClearFocus = ::hideKeyboardWithClearFocus
                )
            },
            sheetPeekHeight = sheetPeekHeight,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            containerColor = scaffoldContainerColor
        ) { paddingValues ->
            if (state.initialLoaded && state.favouriteApps.isEmpty()) {
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

        // To cover the navigation bars with surface color.
        Spacer(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(systemNavigationHeight)
                .fillMaxWidth()
                .background(if (state.enableWallpaper) Color.Transparent else MaterialTheme.colorScheme.surface)
        )
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