package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.minimo.launcher.R
import com.minimo.launcher.utils.HomeTimeFormat
import java.time.LocalTime

private val SAMPLE_TIME = LocalTime.of(14, 30)

@Composable
fun TimeFormatDropdown(
    selectedFormat: HomeTimeFormat,
    onFormatSelected: (HomeTimeFormat) -> Unit
) {
    // Each option shows a sample time in that format, e.g. "12-Hour (2:30 PM)"
    val twelveHour = stringResource(R.string.twelve_hour)
    val twentyFourHour = stringResource(R.string.twenty_four_hour)
    val locale = LocalConfiguration.current.locales[0]
    val labels = remember(locale, twelveHour, twentyFourHour) {
        HomeTimeFormat.entries.associateWith { format ->
            val name = if (format.is24Hour) twentyFourHour else twelveHour
            "$name (${SAMPLE_TIME.format(format.createFormatter(locale))})"
        }
    }

    SettingsDropdownRow(
        title = stringResource(R.string.time_format),
        selectedOption = selectedFormat,
        options = HomeTimeFormat.entries,
        onOptionSelected = onFormatSelected,
        optionLabel = labels::getValue
    )
}
