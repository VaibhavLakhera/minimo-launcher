package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.components.DropdownView
import com.minimo.launcher.ui.theme.Dimens

/**
 * A settings row with a title (and optional [description] below it) on the left and a dropdown
 * of [options] on the right. A null [selectedOption] (preferences not loaded yet) shows no label.
 */
@Composable
fun <T> SettingsDropdownRow(
    title: String,
    selectedOption: T?,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    description: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (description == null) {
            Text(
                text = title,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        DropdownView(
            selectedOption = selectedOption?.let(optionLabel).orEmpty(),
            options = options,
            onOptionSelected = onOptionSelected,
            optionLabel = optionLabel
        )
    }
}
