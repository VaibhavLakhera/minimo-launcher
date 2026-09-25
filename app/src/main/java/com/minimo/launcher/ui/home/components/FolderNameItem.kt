package com.minimo.launcher.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.AppBottomSheetDialog
import com.minimo.launcher.ui.components.AppBottomSheetText
import com.minimo.launcher.ui.home.FolderInfo
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.AppIconAlignment
import androidx.compose.ui.graphics.shadow.Shadow as DropShadow

@Composable
fun FolderNameItem(
    modifier: Modifier,
    info: FolderInfo,
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
    onToggleExpanded: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (info.isExpanded) 180f else 0f, label = "folder_chevron")
    val expandedDescription =
        stringResource(if (info.isExpanded) R.string.folder_expanded else R.string.folder_collapsed)
    val alignment = when (appsArrangement) {
        Arrangement.Center -> Alignment.Center
        Arrangement.End -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }
    val iconSize = appIconSizeFor(textSize, iconSizeScale)

    Box(modifier.fillMaxWidth(), contentAlignment = alignment) {
        Row(
            modifier = (if (compactTouchArea) Modifier else Modifier.fillMaxWidth())
                .semantics { stateDescription = expandedDescription }
                .combinedClickable(
                    role = Role.Button,
                    onClick = onToggleExpanded,
                    onLongClick = { onLongClick(); showMenu = true }
                )
                .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = verticalPadding),
            horizontalArrangement = if (compactTouchArea) Arrangement.Start else appsArrangement,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = !compactTouchArea),
                horizontalArrangement = appsArrangement,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIcon && iconAlignment == AppIconAlignment.Left) {
                    FolderIcon(iconSize, info.showNotificationDot)
                    Spacer(Modifier.width(Dimens.APP_ICON_LABEL_SPACING))
                }
                Text(
                    text = info.folder.name,
                    modifier = Modifier.weight(1f, fill = false),
                    color = textColor,
                    fontSize = textSize,
                    lineHeight = textSize * 1.2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(shadow = textShadow)
                )
                if (showIcon && iconAlignment == AppIconAlignment.Right) {
                    Spacer(Modifier.width(Dimens.APP_ICON_LABEL_SPACING))
                    FolderIcon(iconSize, info.showNotificationDot)
                }
                if (!showIcon && info.showNotificationDot) {
                    Spacer(Modifier.width(11.dp))
                    Box(Modifier
                        .size(10.dp)
                        .background(textColor, CircleShape))
                }
            }
            Spacer(Modifier.width(8.dp))
            val density = LocalDensity.current
            val chevronShadow = if (textShadow == null) Modifier else Modifier.dropShadow(
                shape = ChevronShape,
                shadow = DropShadow(
                    radius = with(density) { textShadow.blurRadius.toDp() },
                    color = textShadow.color,
                    offset = with(density) {
                        DpOffset(textShadow.offset.x.toDp(), textShadow.offset.y.toDp())
                    }
                )
            )
            val chevronModifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = rotation }
                .then(chevronShadow)
            Box(modifier = chevronModifier, contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showMenu) {
        AppBottomSheetDialog(
            appName = info.folder.name,
            onDismiss = { showMenu = false },
            statusBarVisible = statusBarVisible,
            navigationBarVisible = navigationBarVisible,
            useDarkStatusBarIcons = useDarkStatusBarIcons,
            useDarkNavigationBarIcons = useDarkNavigationBarIcons
        ) {
            AppBottomSheetText(stringResource(if (info.folder.isFavourite) R.string.remove_favourite else R.string.add_favourite)) {
                showMenu = false
                onToggleFavourite()
            }
            AppBottomSheetText(stringResource(R.string.rename_folder)) {
                showMenu = false; onRename()
            }
            AppBottomSheetText(stringResource(R.string.delete_folder)) {
                showMenu = false; onDelete()
            }
        }
    }
}

// Match the path in ic_keyboard_arrow_down.xml so the shadow follows the chevron itself.
private const val CHEVRON_PATH_DATA =
    "M465,596.5Q458,594 452,588L268,404Q257,393 257,376Q257,359 268,348Q279,337 296,337Q313,337 324,348L480,504L636,348Q647,337 664,337Q681,337 692,348Q703,359 703,376Q703,393 692,404L508,588Q502,594 495,596.5Q488,599 480,599Q472,599 465,596.5Z"

private val ChevronShape = GenericShape { size, _ ->
    val path = PathParser().parsePathString(CHEVRON_PATH_DATA).toPath()
    path.transform(Matrix().apply { scale(size.width / 960f, size.height / 960f, 1f) })
    addPath(path)
}

@Composable
private fun FolderIcon(size: Dp, showDot: Boolean) {
    Box(Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(Dimens.APP_ICON_SHADOW_ELEVATION, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.folder_24px),
                contentDescription = null,
                tint = Color(0xFF202124),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.12f)
            )
        }
        if (showDot) NotificationDot(size)
    }
}
