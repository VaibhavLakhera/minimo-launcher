package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.DropdownView
import com.minimo.launcher.ui.components.rememberCurrentDateTime
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.HomeDateFormat

@Composable
fun DateFormatDropdown(
    selectedFormat: HomeDateFormat,
    onFormatSelected: (HomeDateFormat) -> Unit
) {
    val today = rememberCurrentDateTime().toLocalDate()
    val locale = LocalConfiguration.current.locales[0]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.date_format),
            fontSize = 20.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        DropdownView(
            selectedOption = selectedFormat.format(today, locale),
            options = HomeDateFormat.entries,
            onOptionSelected = onFormatSelected,
            optionLabel = { it.format(today, locale) }
        )
    }
}
