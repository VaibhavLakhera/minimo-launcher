package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun SearchItem(
    modifier: Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onKeyboardDone: (() -> Unit)? = null,
    startPadding: Dp = Dimens.APP_HORIZONTAL_SPACING,
    endPadding: Dp = Dimens.APP_HORIZONTAL_SPACING,
    placeholderText: String = stringResource(R.string.search_app),
    wallpaperContentColor: Color? = null,
    wallpaperTextShadow: Shadow? = null
) {
    val textStyle = if (wallpaperContentColor != null) {
        LocalTextStyle.current.copy(
            color = wallpaperContentColor,
            shadow = wallpaperTextShadow
        )
    } else {
        LocalTextStyle.current
    }
    val textFieldColors = if (wallpaperContentColor != null) {
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = wallpaperContentColor,
            unfocusedTextColor = wallpaperContentColor,
            cursorColor = wallpaperContentColor,
            focusedBorderColor = wallpaperContentColor,
            unfocusedBorderColor = wallpaperContentColor.copy(alpha = 0.7f),
            focusedPlaceholderColor = wallpaperContentColor.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = wallpaperContentColor.copy(alpha = 0.7f)
        )
    } else {
        OutlinedTextFieldDefaults.colors()
    }

    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = {
            Text(
                text = placeholderText,
                style = if (wallpaperContentColor != null) {
                    LocalTextStyle.current.copy(shadow = wallpaperTextShadow)
                } else {
                    LocalTextStyle.current
                }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        singleLine = true,
        textStyle = textStyle,
        colors = textFieldColors,
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
