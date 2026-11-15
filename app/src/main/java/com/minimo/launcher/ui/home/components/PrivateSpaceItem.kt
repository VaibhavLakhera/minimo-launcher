package com.minimo.launcher.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.AppBottomSheetDialog
import com.minimo.launcher.ui.components.AppBottomSheetText
import com.minimo.launcher.ui.home.PrivateSpaceSection
import com.minimo.launcher.utils.AppIconAlignment

/**
 * The private space row in the drawer. It is drawn by [FolderStyleRow], the same row the folders
 * use, so it always lines up with them.
 */
@Composable
fun PrivateSpaceItem(
    modifier: Modifier,
    section: PrivateSpaceSection,
    appsArrangement: Arrangement.Horizontal,
    textSize: TextUnit,
    verticalPadding: Dp,
    compactTouchArea: Boolean,
    showIcon: Boolean,
    iconAlignment: AppIconAlignment,
    iconSizeScale: Float,
    textColor: Color,
    textShadow: Shadow?,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    useDarkStatusBarIcons: Boolean,
    useDarkNavigationBarIcons: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onLock: () -> Unit,
    onInstallApps: () -> Unit,
    onAddFiles: () -> Unit,
    onSettings: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val label = stringResource(R.string.private_space)
    val rotation by animateFloatAsState(
        if (section.isExpanded) 180f else 0f,
        label = "private_space_chevron"
    )
    val stateDescriptionText = stringResource(
        when {
            section.isLocked -> R.string.private_space_locked
            section.isExpanded -> R.string.folder_expanded
            else -> R.string.folder_collapsed
        }
    )

    FolderStyleRow(
        modifier = modifier,
        label = label,
        stateDescriptionText = stateDescriptionText,
        iconRes = if (section.isLocked) R.drawable.ic_lock else R.drawable.ic_lock_open,
        showNotificationDot = section.showNotificationDot,
        appsArrangement = appsArrangement,
        textSize = textSize,
        verticalPadding = verticalPadding,
        compactTouchArea = compactTouchArea,
        showIcon = showIcon,
        iconAlignment = iconAlignment,
        iconSizeScale = iconSizeScale,
        textColor = textColor,
        textShadow = textShadow,
        onClick = onClick,
        onLongClick = { onLongClick(); showMenu = true }
    ) {
        when {
            !section.isLocked -> FolderChevron(rotation, textColor, textShadow)
            // With icons on, the leading lock icon already shows the state.
            !showIcon -> ShadowedIcon(
                iconRes = R.drawable.ic_lock,
                size = 18.dp,
                tint = textColor,
                shadow = textShadow,
                modifier = Modifier.size(24.dp)
            )
            // Keep the trailing space so the text lines up with the folder rows.
            else -> Spacer(Modifier.size(24.dp))
        }
    }

    if (showMenu) {
        AppBottomSheetDialog(
            appName = label,
            onDismiss = { showMenu = false },
            statusBarVisible = statusBarVisible,
            navigationBarVisible = navigationBarVisible,
            useDarkStatusBarIcons = useDarkStatusBarIcons,
            useDarkNavigationBarIcons = useDarkNavigationBarIcons
        ) {
            if (section.isLocked) {
                AppBottomSheetText(stringResource(R.string.unlock_private_space)) {
                    showMenu = false; onClick()
                }
            } else {
                AppBottomSheetText(stringResource(R.string.lock_private_space)) {
                    showMenu = false; onLock()
                }
                AppBottomSheetText(stringResource(R.string.private_space_install_apps)) {
                    showMenu = false; onInstallApps()
                }
                if (section.canAddFiles) {
                    AppBottomSheetText(stringResource(R.string.private_space_add_files)) {
                        showMenu = false; onAddFiles()
                    }
                }
            }
            AppBottomSheetText(stringResource(R.string.private_space_settings)) {
                showMenu = false; onSettings()
            }
        }
    }
}
