package com.minimo.launcher.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.AppBottomSheetDialog
import com.minimo.launcher.ui.components.AppBottomSheetText

@Composable
fun AppListBottomSheetDialog(
    appName: String,
    isFavourite: Boolean,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    statusBarVisible: Boolean = true,
    useDarkStatusBarIcons: Boolean? = null,
    useDarkNavigationBarIcons: Boolean? = null,
    onToggleFavouriteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onToggleHideClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    onUninstallClick: () -> Unit,
) {
    AppBottomSheetDialog(
        appName = appName,
        onDismiss = onDismiss,
        statusBarVisible = statusBarVisible,
        useDarkStatusBarIcons = useDarkStatusBarIcons,
        useDarkNavigationBarIcons = useDarkNavigationBarIcons
    ) {
        if (!isHidden) {
            AppBottomSheetText(
                text = if (isFavourite) stringResource(R.string.remove_favourite) else stringResource(
                    R.string.add_favourite
                ),
                onClick = onToggleFavouriteClick
            )
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
            text = stringResource(R.string.app_info),
            onClick = onAppInfoClick
        )
        AppBottomSheetText(
            text = stringResource(R.string.uninstall),
            onClick = onUninstallClick
        )
    }
}
