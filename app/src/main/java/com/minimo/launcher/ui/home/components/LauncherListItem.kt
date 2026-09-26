package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.home.HomeScreenState
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.ui.home.LauncherRow
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.launchAppInfo
import com.minimo.launcher.utils.uninstallApp

@Composable
fun LauncherListItem(
    modifier: Modifier,
    row: LauncherRow,
    state: HomeScreenState,
    viewModel: HomeViewModel,
    home: Boolean,
    iconCacheRevision: Int,
    textColor: Color,
    textShadow: Shadow?,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    useDarkStatusBarIcons: Boolean,
    useDarkNavigationBarIcons: Boolean,
    onBeforeInteraction: () -> Unit = {},
    onDeleteShortcut: (AppInfo) -> Unit
) {
    val textSize = if (home || state.applyHomeAppSizeToAllApps) state.homeTextSize.sp else 20.sp
    val showIcon = if (home) state.showAppIconInHome else state.showAppIconInDrawer
    val iconAlignment = if (home) state.homeAppIconAlignment else state.drawerAppIconAlignment
    val arrangement =
        if (home) state.appsArrangementHorizontal else state.drawerAppsArrangementHorizontal
    val iconScale = state.appIconSizePercent / 100f
    when (row) {
        is LauncherRow.Folder -> FolderNameItem(
            modifier = modifier,
            info = row.info,
            appsArrangement = arrangement,
            textSize = textSize,
            verticalPadding = state.homeAppVerticalPadding.dp,
            compactTouchArea = state.compactAppTouchArea,
            showIcon = showIcon,
            iconAlignment = iconAlignment,
            iconSizeScale = iconScale,
            textColor = textColor,
            textShadow = textShadow,
            statusBarVisible = statusBarVisible,
            navigationBarVisible = navigationBarVisible,
            useDarkStatusBarIcons = useDarkStatusBarIcons,
            useDarkNavigationBarIcons = useDarkNavigationBarIcons,
            onToggleExpanded = { onBeforeInteraction(); viewModel.toggleFolderExpanded(row.info.folder.id) },
            onLongClick = onBeforeInteraction,
            onToggleFavourite = { viewModel.toggleFolderFavourite(row.info.folder.id) },
            onRename = { viewModel.showRenameFolder(row.info.folder) },
            onDelete = { viewModel.showDeleteFolder(row.info.folder) }
        )

        is LauncherRow.Empty -> Text(
            stringResource(R.string.folder_no_apps),
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.APP_HORIZONTAL_SPACING + 16.dp,
                    vertical = state.homeAppVerticalPadding.dp
                ),
            color = textColor,
            fontSize = textSize,
            style = LocalTextStyle.current.copy(shadow = textShadow),
            textAlign = when (arrangement) {
                Arrangement.Center -> TextAlign.Center
                Arrangement.End -> TextAlign.End
                else -> TextAlign.Start
            }
        )

        is LauncherRow.App -> {
            val context = LocalContext.current
            val app = row.app
            val iconSizePx =
                with(LocalDensity.current) { appIconSizeFor(textSize, iconScale).roundToPx() }
            val icon by produceState<ImageBitmap?>(
                null,
                showIcon,
                app.id,
                iconSizePx to iconCacheRevision
            ) {
                value = if (showIcon) viewModel.loadAppIcon(app, iconSizePx) else null
            }
            AppNameItem(
                modifier = modifier.padding(horizontal = if (row.parentFolderId != null) 16.dp else 0.dp),
                appName = app.name,
                isFavourite = app.isFavourite,
                isHidden = app.isHidden,
                isShortcut = app.isShortcut,
                isWorkProfile = app.isWorkProfile,
                appsArrangement = arrangement,
                textSize = textSize,
                showNotificationDot = app.showNotificationDot,
                compactTouchArea = state.compactAppTouchArea,
                showAppIcon = showIcon,
                appIcon = icon,
                appIconSizeScale = iconScale,
                appIconAlignment = iconAlignment,
                verticalPadding = state.homeAppVerticalPadding.dp,
                textColor = textColor,
                shadow = textShadow,
                bottomSheetStatusBarVisible = statusBarVisible,
                bottomSheetNavigationBarVisible = navigationBarVisible,
                useDarkBottomSheetStatusBarIcons = useDarkStatusBarIcons,
                useDarkBottomSheetNavigationBarIcons = useDarkNavigationBarIcons,
                onClick = { onBeforeInteraction(); viewModel.onAppLaunchRequest(app) },
                onLongClick = onBeforeInteraction,
                onToggleFavouriteClick = { viewModel.onToggleFavouriteAppClick(app) },
                onRenameClick = { viewModel.onRenameAppClick(app) },
                onToggleHideClick = { viewModel.onToggleHideClick(app) },
                onAppInfoClick = { context.launchAppInfo(app) },
                onLaunchDelayClick = { viewModel.onLaunchDelayClick(app) },
                onUninstallClick = { context.uninstallApp(app) },
                onDeleteShortcutClick = { onDeleteShortcut(app) },
                folderId = app.folderId,
                onFolderClick = { viewModel.showFolderPicker(app, fromHome = home) },
                onRemoveFromFolderClick = { viewModel.removeFromFolder(app) }
            )
        }
    }
}
