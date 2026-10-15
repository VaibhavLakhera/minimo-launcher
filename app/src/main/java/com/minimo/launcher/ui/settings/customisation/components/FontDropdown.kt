package com.minimo.launcher.ui.settings.customisation.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.data.CustomFont
import com.minimo.launcher.ui.theme.AvailableFonts
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.ui.theme.rememberFontFamily

@Composable
fun FontDropdown(
    selectedFont: String?,
    onFontSelected: (String) -> Unit,
    customFonts: List<CustomFont>,
    importingFont: Boolean,
    onAddFont: () -> Unit,
    onDeleteFont: (CustomFont) -> Unit,
    @StringRes titleRes: Int = R.string.font,
    onUseAppFont: (() -> Unit)? = null
) {
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
            text = stringResource(titleRes),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))

        var expanded by remember { mutableStateOf(false) }
        var fontToDelete by remember { mutableStateOf<CustomFont?>(null) }
        val rotationAngle by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            label = "dropdown_rotation"
        )

        val systemString = stringResource(R.string.system)
        val appFontString = stringResource(R.string.use_app_font)
        val displaySelectedFont = when (selectedFont) {
            null -> appFontString
            "" -> systemString
            else -> customFonts.firstOrNull { it.value == selectedFont }?.name
                ?: selectedFont.takeIf { it in AvailableFonts } ?: systemString
        }
        val selectedFontFamily = when (selectedFont) {
            null -> LocalTextStyle.current.fontFamily
            else -> rememberFontFamily(selectedFont) ?: FontFamily.Default
        }

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .border(
                        width = 1.dp,
                        color = DividerDefaults.color,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = displaySelectedFont,
                    fontFamily = selectedFontFamily,
                    modifier = Modifier.widthIn(max = 180.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    modifier = Modifier.rotate(rotationAngle),
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                    contentDescription = "Dropdown arrow"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (onUseAppFont != null) {
                    DropdownMenuItem(
                        text = { Text(appFontString) },
                        onClick = {
                            onUseAppFont()
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = systemString,
                            fontFamily = FontFamily.Default
                        )
                    },
                    onClick = {
                        onFontSelected("")
                        expanded = false
                    }
                )

                AvailableFonts.forEach { fontName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = fontName,
                                fontFamily = rememberFontFamily(fontName)
                            )
                        },
                        onClick = {
                            onFontSelected(fontName)
                            expanded = false
                        }
                    )
                }
                // Separates imported fonts from the built-in ones, even when their names match.
                HorizontalDivider()
                customFonts.forEach { font ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = font.name,
                                fontFamily = rememberFontFamily(font.value) ?: FontFamily.Default,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    expanded = false
                                    fontToDelete = font
                                },
                                enabled = !importingFont
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.delete_font)
                                )
                            }
                        },
                        onClick = {
                            onFontSelected(font.value)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(stringResource(if (importingFont) R.string.importing_font else R.string.add_font))
                    },
                    enabled = !importingFont,
                    onClick = {
                        expanded = false
                        onAddFont()
                    }
                )
            }
        }

        fontToDelete?.let { font ->
            AlertDialog(
                onDismissRequest = { fontToDelete = null },
                title = { Text(stringResource(R.string.delete_font)) },
                text = { Text(stringResource(R.string.delete_font_confirmation, font.name)) },
                confirmButton = {
                    Button(
                        onClick = {
                            fontToDelete = null
                            onDeleteFont(font)
                        }
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fontToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
