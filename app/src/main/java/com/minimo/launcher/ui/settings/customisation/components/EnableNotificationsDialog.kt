package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.minimo.launcher.R

@Composable
fun EnableNotificationsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notification_access_required)) },
        text = {
            Text(
                stringResource(R.string.notification_access_required_description)
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(
                    stringResource(R.string.open_settings)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.dismiss)
                )
            }
        }
    )
}