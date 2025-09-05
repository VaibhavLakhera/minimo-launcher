package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun AppNameItem(
    modifier: Modifier,
    appName: String,
    isFavourite: Boolean,
    isHidden: Boolean,
    isWorkProfile: Boolean,
    appsArrangement: Arrangement.Horizontal,
    textSize: TextUnit,
    showNotificationDot: Boolean,
    onClick: () -> Unit,
    onToggleFavouriteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onToggleHideClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    onLongClick: () -> Unit = { },
    onUninstallClick: () -> Unit,
    verticalPadding: Dp = 16.dp
) {
    var appBottomSheetVisible by remember { mutableStateOf(false) }
    val lineHeight by remember { derivedStateOf { textSize * 1.2 } }

    val paddingValues = remember(isWorkProfile, showNotificationDot) {
        if (isWorkProfile || showNotificationDot) {
            PaddingValues(
                start = if (isWorkProfile) 0.dp else Dimens.APP_HORIZONTAL_SPACING,
                end = if (showNotificationDot) 0.dp else Dimens.APP_HORIZONTAL_SPACING,
                top = verticalPadding,
                bottom = verticalPadding
            )
        } else {
            PaddingValues(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = verticalPadding)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    appBottomSheetVisible = true
                }
            )
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = appsArrangement
    ) {
        if (isWorkProfile) {
            Icon(
                painter = painterResource(id = R.drawable.ic_work_profile),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }

        Text(
            text = appName,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = textSize,
            lineHeight = lineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (showNotificationDot) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 11.dp)
                    .size(10.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
            )
        }
    }

    if (appBottomSheetVisible) {
        AppListBottomSheetDialog(
            appName = appName,
            isFavourite = isFavourite,
            isHidden = isHidden,
            onDismiss = { appBottomSheetVisible = false },
            onToggleFavouriteClick = {
                appBottomSheetVisible = false
                onToggleFavouriteClick()
            },
            onRenameClick = {
                appBottomSheetVisible = false
                onRenameClick()
            },
            onToggleHideClick = {
                appBottomSheetVisible = false
                onToggleHideClick()
            },
            onAppInfoClick = {
                appBottomSheetVisible = false
                onAppInfoClick()
            },
            onUninstallClick = {
                appBottomSheetVisible = false
                onUninstallClick()
            }
        )
    }
}