package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.AvailableFonts
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.ui.theme.getFontFamily

@Composable
fun FontDropdown(
    selectedFont: String,
    onFontSelected: (String) -> Unit
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
            text = stringResource(R.string.font),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))

        var expanded by remember { mutableStateOf(false) }
        val rotationAngle by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            label = "dropdown_rotation"
        )

        val systemString = stringResource(R.string.system)
        val displaySelectedFont = selectedFont.ifEmpty { systemString }
        val selectedFontFamily = getFontFamily(selectedFont)

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
                    fontFamily = selectedFontFamily
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    modifier = Modifier.rotate(rotationAngle),
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Dropdown arrow"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // First option is always "System"
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
                                fontFamily = getFontFamily(fontName)
                            )
                        },
                        onClick = {
                            onFontSelected(fontName)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
