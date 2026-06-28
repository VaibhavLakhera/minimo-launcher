package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R

@Composable
fun AppDrawerSearch(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
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
            endPadding = 0.dp
        )
        IconButton(
            onClick = onSettingsClick
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(R.string.settings)
            )
        }
    }
}
