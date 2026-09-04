package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun AppDrawerSearch(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchBarBorderPercent: Int,
    searchBarBackground: Boolean,
    onKeyboardDone: (() -> Unit)? = null,
    onSettingsClick: () -> Unit,
    showSettingsIcon: Boolean = true,
    wallpaperContentColor: Color? = null,
    wallpaperTextShadow: Shadow? = null
) {
    val settingsIconColor = wallpaperContentColor ?: MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchItem(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            searchBarBorderPercent = searchBarBorderPercent,
            searchBarBackground = searchBarBackground,
            onKeyboardDone = onKeyboardDone,
            endPadding = if (showSettingsIcon) 0.dp else Dimens.APP_HORIZONTAL_SPACING,
            wallpaperContentColor = wallpaperContentColor,
            wallpaperTextShadow = wallpaperTextShadow
        )
        if (showSettingsIcon) {
            IconButton(
                onClick = onSettingsClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (wallpaperTextShadow != null) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            tint = wallpaperTextShadow.color,
                            contentDescription = null,
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = wallpaperTextShadow.offset.x.toInt(),
                                        y = wallpaperTextShadow.offset.y.toInt()
                                    )
                                }
                                .blur(
                                    with(density) {
                                        wallpaperTextShadow.blurRadius.toDp()
                                    }
                                )
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        tint = settingsIconColor,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        }
    }
}
