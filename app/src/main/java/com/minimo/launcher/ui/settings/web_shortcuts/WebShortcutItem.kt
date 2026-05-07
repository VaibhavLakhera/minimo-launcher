package com.minimo.launcher.ui.settings.web_shortcuts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.entities.ShortcutInfo
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun WebShortcutItem(
    modifier: Modifier,
    shortcutInfo: ShortcutInfo,
    onToggleFavouriteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(onClick = onToggleFavouriteClick)
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (shortcutInfo.isWorkProfile) {
            Icon(
                painter = painterResource(id = R.drawable.ic_work_profile),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(16.dp),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }

        Text(
            text = shortcutInfo.displayName,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Box {
            IconButton(onClick = { isMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.rename)) },
                    onClick = {
                        isMenuExpanded = false
                        onRenameClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete)) },
                    onClick = {
                        isMenuExpanded = false
                        onDeleteClick()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Switch(
            checked = shortcutInfo.isFavourite,
            onCheckedChange = { onToggleFavouriteClick() }
        )
    }
}