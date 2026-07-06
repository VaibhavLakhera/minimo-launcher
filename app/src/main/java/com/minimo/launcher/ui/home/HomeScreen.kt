package com.minimo.launcher.ui.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.RenameDialog
import com.minimo.launcher.ui.home.components.HomeBody
import com.minimo.launcher.utils.launchAppFromPreference
import com.minimo.launcher.utils.lockScreen
import com.minimo.launcher.utils.showNotificationDrawer

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    onOpenAppDrawer: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val homeLazyListState = rememberLazyListState()

    // Calculate dynamic threshold based on window container height & width.
    val screenSize = LocalWindowInfo.current.containerSize
    val swipeVerticalThresholdPx = screenSize.height * 0.8f
    val swipeHorizontalThresholdPx = screenSize.width * 0.15f

    val swipeUpThreshold = -swipeVerticalThresholdPx
    val swipeDownThreshold = swipeVerticalThresholdPx
    val swipeLeftThreshold = -swipeHorizontalThresholdPx
    val swipeRightThreshold = swipeHorizontalThresholdPx

    BackHandler {
        if (state.backOpensAppDrawer) {
            onOpenAppDrawer()
        }
    }

    var swipeYAccumulator by remember { mutableFloatStateOf(0f) }
    var swipeXAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(
        homeLazyListState,
        swipeVerticalThresholdPx
    ) {
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
                    onOpenAppDrawer()
                } else {
                    if (available.y > 1000f && !homeLazyListState.canScrollBackward) {
                        context.showNotificationDrawer()
                    } else if (available.y < -1000f && !homeLazyListState.canScrollForward) {
                        onOpenAppDrawer()
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
        WindowInsets.statusBars.union(WindowInsets.displayCutout)
    val surfaceColor = MaterialTheme.colorScheme.surface

    val useDarkIconsOnSurface = useDarkIconsOnColor(surfaceColor)
    val useDarkNavigationIcons = shouldUseDarkNavigationIcons(
        enableWallpaper = state.enableWallpaper,
        lightTextOnWallpaper = state.lightTextOnWallpaper,
        useDarkIconsOnSurface = useDarkIconsOnSurface
    )

    ApplyNavigationBarIconColor(useDarkNavigationIcons)

    val useDarkBottomSheetStatusBarIcons =
        shouldUseDarkStatusBarIcons(
            enableWallpaper = state.enableWallpaper,
            lightTextOnWallpaper = state.lightTextOnWallpaper,
            useDarkIconsOnSurface = useDarkIconsOnSurface
        )

    val boxBackgroundColor = remember(
        state.enableWallpaper,
        state.dimWallpaper,
        state.dimWallpaperPercentage,
        surfaceColor
    ) {
        if (state.enableWallpaper) {
            if (state.dimWallpaper) {
                Color.Black.copy(alpha = state.dimWallpaperPercentage / 100f)
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
        Scaffold(
            containerColor = scaffoldContainerColor,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            if (state.initialLoaded) {
                HomeBody(
                    paddingValues = paddingValues,
                    state = state,
                    viewModel = viewModel,
                    homeLazyListState = homeLazyListState,
                    nestedScrollConnection = nestedScrollConnection,
                    systemNavigationHeight = systemNavigationHeight,
                    statusBarVisible = statusBarVisible,
                    navigationBarVisible = navigationBarVisible,
                    useDarkBottomSheetStatusBarIcons = useDarkBottomSheetStatusBarIcons,
                    useDarkBottomSheetNavigationBarIcons = useDarkIconsOnSurface
                )
            }
        }
    }

    if (state.renameAppDialog != null) {
        val app = state.renameAppDialog!!
        RenameDialog(
            title = stringResource(R.string.rename_app),
            label = stringResource(R.string.app_name_label),
            originalName = app.appName,
            currentName = app.name,
            onRenameClick = viewModel::onRenameApp,
            onCancelClick = viewModel::onDismissRenameAppDialog
        )
    }
}

@Composable
private fun ApplyNavigationBarIconColor(useDarkNavigationIcons: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, useDarkNavigationIcons) {
        if (!view.isInEditMode) {
            val window = (view.context as Activity).window
            // In Android's API, "light navigation bars" means dark nav icons.
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                useDarkNavigationIcons
        }
        onDispose { }
    }
}

private fun shouldUseDarkNavigationIcons(
    enableWallpaper: Boolean,
    lightTextOnWallpaper: Boolean,
    useDarkIconsOnSurface: Boolean
): Boolean {
    return if (enableWallpaper) !lightTextOnWallpaper else useDarkIconsOnSurface
}

private fun shouldUseDarkStatusBarIcons(
    enableWallpaper: Boolean,
    lightTextOnWallpaper: Boolean,
    useDarkIconsOnSurface: Boolean
): Boolean {
    return if (enableWallpaper) !lightTextOnWallpaper else useDarkIconsOnSurface
}

private fun useDarkIconsOnColor(color: Color): Boolean = color.luminance() > 0.5f
