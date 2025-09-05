package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun IgnoreSpecialCharacters(
    currentCharacters: String,
    onUpdateCharacters: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clickable { showDialog = true }
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.ignore_special_characters),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.these_characters_will_be_ignored_when_searching_for_apps_in_home_screen)
        )
    }

    if (showDialog) {
        IgnoreSpecialCharactersDialog(
            currentCharacters = currentCharacters,
            onUpdateClick = { characters ->
                onUpdateCharacters(characters)
                showDialog = false
            },
            onCancelClick = {
                showDialog = false
            }
        )
    }
}