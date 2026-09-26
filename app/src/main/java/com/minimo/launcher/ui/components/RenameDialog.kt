package com.minimo.launcher.ui.components

import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import com.minimo.launcher.R
import kotlinx.coroutines.android.awaitFrame

@Composable
fun RenameDialog(
    title: String,
    label: String,
    originalName: String,
    currentName: String,
    onRenameClick: (String) -> Unit,
    onCancelClick: () -> Unit
) {
    var name by remember {
        mutableStateOf(TextFieldValue(currentName, selection = TextRange(currentName.length)))
    }
    RenameDialog(
        title = title,
        label = label,
        value = name,
        onValueChange = { name = it },
        placeholder = originalName,
        confirmText = stringResource(R.string.rename),
        onConfirm = { onRenameClick(name.text.trim()) },
        onDismiss = onCancelClick
    )
}

@Composable
fun RenameDialog(
    title: String,
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    placeholder: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
    confirmEnabled: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = { if (enabled) onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = value,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 150) {
                        onValueChange(newValue)
                    }
                },
                singleLine = true,
                label = { Text(label) },
                placeholder = placeholder?.let { { Text(it) } },
                enabled = enabled,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = enabled && confirmEnabled) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = enabled) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    LaunchedEffect(focusRequester) {
        awaitFrame()
        focusRequester.requestFocus()
    }
}
