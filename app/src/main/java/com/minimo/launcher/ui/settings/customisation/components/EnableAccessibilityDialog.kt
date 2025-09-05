package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.minimo.launcher.R

@Composable
fun EnableAccessibilityDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.enable_accessibility_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.enable_accessibility_dialog_message,
                    stringResource(id = R.string.app_name)
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.dismiss))
            }
        }
    )
}