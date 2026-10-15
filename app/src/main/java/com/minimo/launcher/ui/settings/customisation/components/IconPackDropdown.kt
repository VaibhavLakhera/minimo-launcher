package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.IconPackOption

@Composable
fun IconPackDropdown(
    selectedPackage: String,
    packs: List<IconPackOption>,
    loading: Boolean,
    onPackSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "dropdown_rotation"
    )

    val selected = packs.firstOrNull { it.packageName == selectedPackage }
    val title = stringResource(R.string.icon_pack)
    // Only a selected pack needs the list for its name; Default is known before the list loads.
    val selectedLabel = when {
        selected != null -> selected.name
        loading && selectedPackage.isNotEmpty() -> stringResource(R.string.loading)
        else -> stringResource(R.string.default_app)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp
        )
        Box {
            val description = stringResource(R.string.icon_pack_description)
            IconButton(onClick = { showInfo = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_info_outline),
                    contentDescription = description,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showInfo,
                onDismissRequest = { showInfo = false }
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .semantics(mergeDescendants = true) {
                        contentDescription = "$title: $selectedLabel"
                    }
                    .clickable(enabled = !loading, role = Role.Button) {
                        expanded = !expanded
                    }
                    .border(
                        width = 1.dp,
                        color = DividerDefaults.color,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp)
            ) {
                PackIcon(selected)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedLabel,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    modifier = Modifier.rotate(rotationAngle),
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded && !loading,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.default_app)) },
                    leadingIcon = { PackIcon(null) },
                    trailingIcon = { if (selected == null) SelectionCheck() },
                    onClick = {
                        onPackSelected("")
                        expanded = false
                    }
                )

                packs.forEach { pack ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = pack.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = { PackIcon(pack) },
                        trailingIcon = {
                            if (pack.packageName == selectedPackage) SelectionCheck()
                        },
                        onClick = {
                            onPackSelected(pack.packageName)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PackIcon(pack: IconPackOption?) {
    if (pack != null) {
        Image(
            bitmap = pack.icon,
            contentDescription = null,
            modifier = Modifier.size(IconPackOption.ICON_SIZE_DP.dp)
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.ic_apps),
            contentDescription = null,
            modifier = Modifier.size(IconPackOption.ICON_SIZE_DP.dp)
        )
    }
}

@Composable
private fun SelectionCheck() {
    Icon(
        painter = painterResource(R.drawable.ic_check),
        contentDescription = null
    )
}
