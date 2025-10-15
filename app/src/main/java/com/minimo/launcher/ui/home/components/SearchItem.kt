package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun SearchItem(
    modifier: Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    startPadding: Dp = Dimens.APP_HORIZONTAL_SPACING,
    endPadding: Dp = Dimens.APP_HORIZONTAL_SPACING
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = { Text(text = stringResource(R.string.search_app)) },
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        singleLine = true
    )
}