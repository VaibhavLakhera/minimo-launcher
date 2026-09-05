package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.Constants

@Composable
fun SearchItem(
    modifier: Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchBarBorderPercent: Int,
    searchBarBackground: Boolean,
    onKeyboardDone: (() -> Unit)? = null,
    startPadding: Dp = Dimens.APP_HORIZONTAL_SPACING,
    endPadding: Dp = Dimens.APP_HORIZONTAL_SPACING,
    placeholderText: String = stringResource(R.string.search_app),
    wallpaperContentColor: Color? = null,
    wallpaperTextShadow: Shadow? = null,
    enabled: Boolean = true
) {
    // A search bar with a background uses the theme's text colors even over wallpaper.
    val wallpaperTextColor = if (searchBarBackground) null else wallpaperContentColor
    val containerColor = if (searchBarBackground) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        Color.Transparent
    }
    val textStyle = when {
        wallpaperTextColor != null -> LocalTextStyle.current.copy(
            color = wallpaperTextColor,
            shadow = wallpaperTextShadow
        )

        searchBarBackground -> LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface,
            shadow = null
        )

        else -> LocalTextStyle.current
    }
    val textFieldColors = if (wallpaperTextColor != null) {
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = wallpaperTextColor,
            unfocusedTextColor = wallpaperTextColor,
            cursorColor = wallpaperTextColor,
            focusedBorderColor = wallpaperTextColor,
            unfocusedBorderColor = wallpaperTextColor.copy(alpha = 0.7f),
            focusedPlaceholderColor = wallpaperTextColor.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = wallpaperTextColor.copy(alpha = 0.7f)
        )
    } else {
        OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor
        )
    }

    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholderText,
                style = if (wallpaperTextColor != null) {
                    LocalTextStyle.current.copy(shadow = wallpaperTextShadow)
                } else {
                    LocalTextStyle.current
                }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding)
            .then(if (enabled) Modifier else Modifier.clearAndSetSemantics {}),
        singleLine = true,
        shape = searchBarShape(searchBarBorderPercent),
        textStyle = textStyle,
        // Keep the display-only preview identical to an unfocused, enabled search bar.
        colors = if (enabled) textFieldColors else textFieldColors.copy(
            disabledTextColor = textFieldColors.unfocusedTextColor,
            disabledContainerColor = textFieldColors.unfocusedContainerColor,
            disabledIndicatorColor = textFieldColors.unfocusedIndicatorColor,
            disabledPlaceholderColor = textFieldColors.unfocusedPlaceholderColor
        ),
        keyboardOptions = if (onKeyboardDone != null) {
            KeyboardOptions(imeAction = ImeAction.Done)
        } else {
            KeyboardOptions.Default
        },
        keyboardActions = if (onKeyboardDone != null) {
            KeyboardActions(onDone = { onKeyboardDone() })
        } else {
            KeyboardActions.Default
        }
    )
}

@Composable
private fun searchBarShape(borderPercent: Int): Shape {
    // The 7% default keeps the original 4dp corners, including at larger font sizes.
    return if (borderPercent == Constants.DEFAULT_SEARCH_BAR_BORDER_PERCENT) {
        OutlinedTextFieldDefaults.shape
    } else {
        RoundedCornerShape(percent = borderPercent)
    }
}
