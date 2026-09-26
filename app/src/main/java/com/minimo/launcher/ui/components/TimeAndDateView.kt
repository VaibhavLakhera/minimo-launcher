package com.minimo.launcher.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.theme.getFontFamily
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.HomeDateFormat
import com.minimo.launcher.utils.defaultHomeDateTextSize
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

@Composable
fun TimeAndDateView(
    horizontalAlignment: Alignment.Horizontal,
    showHomeClock: Boolean,
    clockMode: HomeClockMode,
    twentyFourHourFormat: Boolean,
    showBatteryLevel: Boolean,
    timeTextSize: Int?,
    dateTextSize: Int?,
    timeFont: String?,
    dateFormat: HomeDateFormat,
    textColor: Color,
    textShadow: Shadow?,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
    onBatteryClick: () -> Unit
) {
    val showTime = showHomeClock && clockMode != HomeClockMode.DateOnly
    val showDate = showHomeClock && clockMode != HomeClockMode.TimeOnly
    val currentDateTime = rememberCurrentDateTime(
        enabled = showHomeClock,
        updateEveryMinute = showTime
    )
    val locale = LocalConfiguration.current.locales[0]
    val timeText = if (showTime) {
        val formatter = remember(twentyFourHourFormat, locale) {
            val pattern = DateFormat.getBestDateTimePattern(
                locale,
                if (twentyFourHourFormat) "HHmm" else "hhmm"
            )
            DateTimeFormatter.ofPattern(pattern, locale).withDecimalStyle(DecimalStyle.of(locale))
        }
        remember(currentDateTime, formatter) { currentDateTime.format(formatter) }
    } else {
        ""
    }
    val dateText = if (showDate) {
        val formatter = remember(dateFormat, locale) { dateFormat.createFormatter(locale) }
        val date = currentDateTime.toLocalDate()
        remember(date, formatter) { date.format(formatter) }
    } else {
        ""
    }
    val dateFontSize = (dateTextSize ?: defaultHomeDateTextSize(showHomeClock, clockMode)).sp
    val dateFontWeight =
        if (showDate && clockMode == HomeClockMode.DateOnly) FontWeight.Bold else null
    val timeFontFamily = timeFont?.let { getFontFamily(it) ?: FontFamily.Default }
        ?: LocalTextStyle.current.fontFamily

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showTime) {
            Text(
                modifier = Modifier.clickable(onClick = onClockClick),
                text = timeText,
                fontSize = (timeTextSize ?: Constants.DEFAULT_HOME_TIME_TEXT_SIZE).sp,
                lineHeight = timeTextSize?.let { (it * 1.2f).sp }
                    ?: LocalTextStyle.current.lineHeight,
                fontWeight = FontWeight.Bold,
                fontFamily = timeFontFamily,
                color = textColor,
                style = LocalTextStyle.current.copy(shadow = textShadow)
            )
        }
        if (showDate || showBatteryLevel) {
            // Preserve the existing date spacing; a battery on its own needs no top gap.
            if (showDate || showTime) Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDate) {
                    Text(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable(onClick = onDateClick),
                        text = dateText,
                        fontSize = dateFontSize,
                        lineHeight = dateTextSize?.let { (it * 1.2f).sp }
                            ?: LocalTextStyle.current.lineHeight,
                        fontWeight = dateFontWeight,
                        color = textColor,
                        style = LocalTextStyle.current.copy(shadow = textShadow)
                    )
                }

                if (showDate && showBatteryLevel) {
                    Text(
                        "  |  ",
                        fontSize = dateFontSize,
                        fontWeight = dateFontWeight,
                        color = textColor,
                        style = LocalTextStyle.current.copy(shadow = textShadow)
                    )
                }

                if (showBatteryLevel) {
                    BatteryPercentView(
                        fontSize = dateFontSize,
                        fontWeight = dateFontWeight,
                        textColor = textColor,
                        textShadow = textShadow,
                        onClick = onBatteryClick
                    )
                }
            }
        }
    }
}
