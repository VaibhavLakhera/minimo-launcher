package com.minimo.launcher.ui.favourite_apps.reorder_apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.entities.AppInfo
import com.minimo.launcher.ui.home.components.AppNameItem
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.Constants

@Composable
fun ReorderAppItem(
    modifier: Modifier,
    appInfo: AppInfo,
    onUpArrowClick: () -> Unit,
    onDownArrowClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppNameItem(
            modifier = Modifier.weight(1f),
            appName = appInfo.name,
            isFavourite = appInfo.isFavourite,
            isHidden = appInfo.isHidden,
            isWorkProfile = appInfo.isWorkProfile,
            onClick = { },
            onToggleFavouriteClick = { },
            onRenameClick = { },
            onToggleHideClick = { },
            onAppInfoClick = { },
            appsArrangement = Arrangement.Start,
            textSize = Constants.DEFAULT_HOME_TEXT_SIZE.sp,
            onUninstallClick = { },
            showNotificationDot = false,
            clickEnabled = false
        )

        Row {
            FilledTonalIconButton(
                onClick = onUpArrowClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null
                )
            }
            FilledTonalIconButton(
                onClick = onDownArrowClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimens.APP_HORIZONTAL_SPACING))
    }
}