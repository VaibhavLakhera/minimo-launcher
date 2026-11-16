package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.rememberCurrentDateTime
import com.minimo.launcher.utils.HomeDateFormat

@Composable
fun DateFormatDropdown(
    selectedFormat: HomeDateFormat,
    onFormatSelected: (HomeDateFormat) -> Unit
) {
    val today = rememberCurrentDateTime(updateEveryMinute = false).toLocalDate()
    val locale = LocalConfiguration.current.locales[0]
    val formatters = remember(locale) {
        HomeDateFormat.entries.associateWith { it.createFormatter(locale) }
    }
    val labels = remember(today, formatters) {
        formatters.mapValues { (_, formatter) -> today.format(formatter) }
    }

    SettingsDropdownRow(
        title = stringResource(R.string.date_format),
        selectedOption = selectedFormat,
        options = HomeDateFormat.entries,
        onOptionSelected = onFormatSelected,
        optionLabel = labels::getValue
    )
}
