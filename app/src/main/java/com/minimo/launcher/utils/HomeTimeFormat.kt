package com.minimo.launcher.utils

import android.text.format.DateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

/** Time formats for the home screen clock. Stored by name, so never rename entries. */
enum class HomeTimeFormat(private val fallbackPattern: String) {
    TwelveHour("h:mm a"),
    TwelveHourNoAmPm("h:mm"),
    TwentyFourHour("HH:mm");

    val is24Hour: Boolean get() = this == TwentyFourHour

    /** Uses the locale's own time pattern, or [fallbackPattern] if java.time can't read it. */
    fun createFormatter(locale: Locale): DateTimeFormatter {
        val formatter = try {
            DateTimeFormatter.ofPattern(localePattern(locale), locale)
        } catch (_: IllegalArgumentException) {
            // e.g. Toki Pona's pattern uses '#', which java.time reserves
            DateTimeFormatter.ofPattern(fallbackPattern, locale)
        }
        return formatter.withDecimalStyle(DecimalStyle.of(locale))
    }

    private fun localePattern(locale: Locale): String = when (this) {
        TwelveHour -> DateFormat.getBestDateTimePattern(locale, SKELETON_12)
        TwentyFourHour -> DateFormat.getBestDateTimePattern(locale, SKELETON_24)
        // Built from the 24-hour pattern: some 12-hour patterns put text next to AM/PM
        // ("a 'ga' h:mm"), and only the 24-hour pattern shows which text belongs to the time.
        TwelveHourNoAmPm -> toTwelveHourPattern(
            pattern24 = DateFormat.getBestDateTimePattern(locale, SKELETON_24),
            pattern12 = DateFormat.getBestDateTimePattern(locale, SKELETON_12)
        )
    }

    companion object {
        private const val SKELETON_12 = "hhmm"
        private const val SKELETON_24 = "HHmm"

        fun fromPreference(value: String?): HomeTimeFormat =
            entries.firstOrNull { it.name == value } ?: TwelveHour
    }
}

/**
 * Turns the 24-hour [pattern24] into a 12-hour pattern without AM/PM: the hour field (`H` 0-23
 * or `k` 1-24) becomes `h` (1-12), as wide as the hour in the locale's 12-hour [pattern12]
 * ("1:05" vs "01:05"). Everything else, quoted text included, comes from [pattern24], e.g.
 * "HH:mm 'ч'." becomes "hh:mm 'ч'.".
 */
internal fun toTwelveHourPattern(pattern24: String, pattern12: String): String {
    val hourWidth = hourFieldWidth(pattern12)
    val result = StringBuilder(pattern24.length)
    var quoted = false
    var i = 0
    while (i < pattern24.length) {
        val char = pattern24[i]
        // '' is an escaped quote; toggling twice leaves the state unchanged, as it should
        if (char == '\'') quoted = !quoted
        if (!quoted && (char == 'H' || char == 'k')) {
            val end = fieldEnd(pattern24, i)
            result.append("h".repeat(hourWidth ?: (end - i).coerceIn(1, 2)))
            i = end
        } else {
            result.append(char)
            i++
        }
    }
    return result.toString()
}

/** Width (1 or 2) of the first unquoted 12-hour field (`h`, or `K` 0-11) in [pattern]. */
private fun hourFieldWidth(pattern: String): Int? {
    var quoted = false
    var i = 0
    while (i < pattern.length) {
        val char = pattern[i]
        if (char == '\'') quoted = !quoted
        if (!quoted && (char == 'h' || char == 'K')) return (fieldEnd(pattern, i) - i).coerceIn(
            1,
            2
        )
        i++
    }
    return null
}

/** Index just past the run of the letter at [start]. */
private fun fieldEnd(pattern: String, start: Int): Int {
    var end = start
    while (end < pattern.length && pattern[end] == pattern[start]) end++
    return end
}
