package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.AppBottomSheetDialog
import com.minimo.launcher.ui.components.AppBottomSheetText

@Composable
fun AppListBottomSheetDialog(
    appName: String,
    folderId: String? = null,
    onFolderClick: (() -> Unit)? = null,
    onRemoveFromFolderClick: (() -> Unit)? = null,
    isFavourite: Boolean,
    isHidden: Boolean,
    isShortcut: Boolean,
    onDismiss: () -> Unit,
    statusBarVisible: Boolean = true,
    navigationBarVisible: Boolean = true,
    useDarkStatusBarIcons: Boolean? = null,
    useDarkNavigationBarIcons: Boolean? = null,
    onToggleFavouriteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onToggleHideClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onDeleteShortcutClick: () -> Unit,
    onLaunchDelayClick: () -> Unit,
) {
    AppBottomSheetDialog(
        appName = appName,
        onDismiss = onDismiss,
        statusBarVisible = statusBarVisible,
        navigationBarVisible = navigationBarVisible,
        useDarkStatusBarIcons = useDarkStatusBarIcons,
        useDarkNavigationBarIcons = useDarkNavigationBarIcons
    ) {
        Column(Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState())) {
            // Only the apps outside the folder can be added/removed as favourite independently
            if (!isHidden && folderId == null) {
                AppBottomSheetText(
                    text = if (isFavourite) stringResource(R.string.remove_favourite) else stringResource(
                        R.string.add_favourite
                    ),
                    onClick = onToggleFavouriteClick
                )
            }

            if (!isHidden) {
                if (folderId == null && onFolderClick != null) {
                    AppBottomSheetText(stringResource(R.string.add_to_folder), onFolderClick)
                }
                if (folderId != null && onRemoveFromFolderClick != null) {
                    AppBottomSheetText(
                        stringResource(R.string.remove_from_folder),
                        onRemoveFromFolderClick
                    )
                }
            }
            AppBottomSheetText(
                text = stringResource(R.string.rename),
                onClick = onRenameClick
            )
            AppBottomSheetText(
                text = if (isHidden) stringResource(R.string.unhide_app) else stringResource(R.string.hide_app),
                onClick = onToggleHideClick
            )
            AppBottomSheetText(
                text = stringResource(R.string.launch_delay),
                onClick = onLaunchDelayClick
            )
            if (isShortcut) {
                AppBottomSheetText(
                    text = stringResource(R.string.delete_shortcut),
                    onClick = onDeleteShortcutClick
                )
            } else {
                AppBottomSheetText(
                    text = stringResource(R.string.app_info),
                    onClick = onAppInfoClick
                )
                AppBottomSheetText(
                    text = stringResource(R.string.uninstall),
                    onClick = onUninstallClick
                )
            }
        }
    }
}
