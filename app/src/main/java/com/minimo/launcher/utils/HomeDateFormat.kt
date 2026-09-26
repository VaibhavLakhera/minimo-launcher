package com.minimo.launcher.utils

import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

enum class HomeDateFormat(private val pattern: String) {
    Default("EEE, dd MMMM"),
    FullWeekday("EEEE, d MMMM"),
    ShortMonthWithWeekday("EEE, d MMM"),
    DayMonth("d MMMM"),
    DayShortMonth("d MMM"),
    MonthDay("MMMM d"),
    WeekdayMonthDay("EEE, MMM d"),
    DayMonthYear("d MMM yyyy"),
    MonthDayYear("MMM d, yyyy"),
    NumericDayFirst("dd/MM/yyyy"),
    NumericMonthFirst("MM/dd/yyyy"),
    Iso("yyyy-MM-dd");

    fun createFormatter(locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern(pattern, locale).withDecimalStyle(DecimalStyle.of(locale))

    companion object {
        fun fromPreference(value: String?): HomeDateFormat =
            entries.firstOrNull { it.name == value } ?: Default
    }
}

fun defaultHomeDateTextSize(showHomeClock: Boolean, clockMode: HomeClockMode?): Int =
    if (showHomeClock && clockMode == HomeClockMode.DateOnly) {
        Constants.DEFAULT_HOME_DATE_ONLY_TEXT_SIZE
    } else {
        Constants.DEFAULT_HOME_DATE_TEXT_SIZE
    }
