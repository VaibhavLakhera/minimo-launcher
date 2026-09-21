package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.ui.theme.themeColorScheme
import com.minimo.launcher.utils.StringUtils

@Composable
fun ThemeDropdown(
    selectedTheme: ThemeMode,
    blackTheme: Boolean,
    useDynamicTheme: Boolean,
    onOptionSelected: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val selectedLabel = StringUtils.themeModeText(context, selectedTheme)
    val selectedColors = themeColorScheme(selectedTheme, blackTheme, useDynamicTheme)
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "theme_dropdown_rotation"
    )
    val selectorDescription = stringResource(R.string.theme_selector_description, selectedLabel)
    val menuState = stringResource(
        if (expanded) R.string.theme_menu_expanded else R.string.theme_menu_collapsed
    )
    val shape = RoundedCornerShape(4.dp)

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
            stringResource(R.string.theme),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.widthIn(max = 200.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(selectedColors.surface)
                    .border(1.dp, DividerDefaults.color, shape)
                    .clickable(role = Role.DropdownList) { expanded = !expanded }
                    .semantics {
                        contentDescription = selectorDescription
                        stateDescription = menuState
                    }
                    .padding(12.dp)
            ) {
                Text(
                    text = selectedLabel,
                    color = selectedColors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clearAndSetSemantics { }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                    contentDescription = null,
                    tint = selectedColors.onSurface,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 280.dp)
                    .heightIn(max = 400.dp),
                tonalElevation = 0.dp
            ) {
                ThemeMode.entries.forEach { theme ->
                    val colors = themeColorScheme(theme, blackTheme, useDynamicTheme)
                    val isSelected = theme == selectedTheme

                    MaterialTheme(colorScheme = colors) {
                        DropdownMenuItem(
                            text = { Text(StringUtils.themeModeText(context, theme)) },
                            onClick = {
                                expanded = false
                                onOptionSelected(theme)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(colors.surface)
                                .semantics { selected = isSelected },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(24.dp))
                                }
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = colors.onSurface,
                                trailingIconColor = colors.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
