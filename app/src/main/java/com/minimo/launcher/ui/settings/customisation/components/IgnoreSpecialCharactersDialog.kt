package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.minimo.launcher.R
import kotlinx.coroutines.android.awaitFrame

@Composable
fun IgnoreSpecialCharactersDialog(
    currentCharacters: String,
    onUpdateClick: (String) -> Unit,
    onCancelClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    var newCharacters by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentCharacters,
                selection = TextRange(currentCharacters.length)
            )
        )
    }
    AlertDialog(
        onDismissRequest = onCancelClick,
        title = { Text(stringResource(R.string.ignore_special_characters)) },
        text = {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = newCharacters,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 30) {
                        newCharacters = newValue
                    }
                },
                singleLine = true,
                label = { Text(stringResource(R.string.enter_characters)) }
            )
        },
        confirmButton = {
            Button(onClick = { onUpdateClick(newCharacters.text.trim()) }) {
                Text(stringResource(R.string.update))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelClick) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    LaunchedEffect(focusRequester) {
        awaitFrame()
        focusRequester.requestFocus()
    }
}