package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.data.FolderError
import com.minimo.launcher.ui.components.AppBottomSheetDialog
import com.minimo.launcher.ui.components.AppBottomSheetText
import com.minimo.launcher.ui.components.AppOutlinedButton
import com.minimo.launcher.ui.components.RenameDialog
import com.minimo.launcher.ui.home.FolderDialog
import com.minimo.launcher.ui.home.HomeScreenState
import com.minimo.launcher.ui.home.HomeViewModel
import com.minimo.launcher.ui.home.messageRes
import com.minimo.launcher.ui.theme.Dimens
import java.util.Locale

@Composable
fun FolderDialogs(
    state: HomeScreenState,
    viewModel: HomeViewModel,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    useDarkStatusBarIcons: Boolean,
    useDarkNavigationBarIcons: Boolean
) {
    when (val dialog = state.folderDialog) {
        null -> Unit

        is FolderDialog.Picker -> {
            AppBottomSheetDialog(
                appName = stringResource(R.string.folders),
                onDismiss = viewModel::dismissFolderDialog,
                statusBarVisible = statusBarVisible,
                navigationBarVisible = navigationBarVisible,
                useDarkStatusBarIcons = useDarkStatusBarIcons,
                useDarkNavigationBarIcons = useDarkNavigationBarIcons,
                titleAction = {
                    AppOutlinedButton(
                        text = stringResource(R.string.new_folder_plus),
                        enabled = !state.folderSaving,
                        compact = true,
                        onClick = {
                            viewModel.showCreateFolder(
                                dialog.app,
                                fromHome = dialog.fromHome
                            )
                        }
                    )
                }
            ) {
                state.folderError?.let { error ->
                    Text(
                        text = stringResource(error.messageRes),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            horizontal = Dimens.APP_HORIZONTAL_SPACING,
                            vertical = 8.dp
                        )
                    )
                }
                LazyColumn(Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)) {
                    items(state.folders, key = { it.folder.id }) { info ->
                        AppBottomSheetText(info.folder.name) {
                            viewModel.assignToFolder(dialog.app, info.folder.id)
                        }
                    }
                }
            }
        }

        is FolderDialog.Create, is FolderDialog.Rename -> {
            val originalFolder = (dialog as? FolderDialog.Rename)?.folder
            var name by rememberSaveable(dialog, stateSaver = TextFieldValue.Saver) {
                val initialName = originalFolder?.name.orEmpty()
                mutableStateOf(
                    TextFieldValue(
                        initialName,
                        selection = TextRange(initialName.length)
                    )
                )
            }
            val trimmed = name.text.trim()
            val duplicate = state.folders.any {
                it.folder.id != originalFolder?.id && it.folder.normalizedName == trimmed.lowercase(
                    Locale.ROOT
                )
            }
            val validationError = when {
                name.text.isNotEmpty() && trimmed.isEmpty() -> FolderError.INVALID_NAME
                duplicate -> FolderError.DUPLICATE_NAME
                else -> state.folderError
            }
            key(dialog) {
                RenameDialog(
                    title = stringResource(if (originalFolder == null) R.string.new_folder else R.string.rename_folder),
                    label = stringResource(R.string.folder_name),
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.clearFolderError()
                    },
                    confirmText = stringResource(if (originalFolder == null) R.string.create_folder else R.string.rename),
                    onConfirm = { viewModel.saveFolderName(name.text) },
                    onDismiss = viewModel::dismissFolderDialog,
                    errorMessage = validationError?.let { stringResource(it.messageRes) },
                    enabled = !state.folderSaving,
                    confirmEnabled = trimmed.isNotEmpty() && !duplicate
                )
            }
        }

        is FolderDialog.Delete -> AlertDialog(
            onDismissRequest = viewModel::dismissFolderDialog,
            title = { Text(stringResource(R.string.delete_folder)) },
            text = {
                Column {
                    Text(stringResource(R.string.delete_folder_confirmation, dialog.folder.name))
                    state.folderError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(it.messageRes), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteFolder(dialog.folder.id) },
                    enabled = !state.folderSaving
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissFolderDialog,
                    enabled = !state.folderSaving
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
