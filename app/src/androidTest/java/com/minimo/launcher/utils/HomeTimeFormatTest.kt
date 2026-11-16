package com.minimo.launcher.utils

import android.text.format.DateFormat
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class HomeTimeFormatTest {

    @Test
    fun toTwelveHourPattern_takesTextFrom24HourAndHourWidthFrom12Hour() {
        // (24-hour pattern, 12-hour pattern) to expected
        val cases = mapOf(
            ("HH:mm" to "h:mm a") to "h:mm",
            ("HH:mm" to "hh:mm a") to "hh:mm",
            ("H:mm" to "aK:mm") to "h:mm",
            ("HH:mm" to "a 'ga' h:mm") to "h:mm",
            ("HH.mm" to "h.mm a") to "h.mm",
            ("kk:mm" to "h:mm a") to "h:mm",
            ("HH:mm 'ч'." to "h:mm 'ч'. a") to "h:mm 'ч'.",
            ("HH 'h' mm" to "h 'h' mm a") to "h 'h' mm",
            ("HH'h'mm" to "h'h'mm a") to "h'h'mm",
            ("'Kl'. H.mm" to "'Kl'. h.mm a") to "'Kl'. h.mm",
            ("HH 'Hrs' mm" to "'h' hh:mm a") to "hh 'Hrs' mm",
            ("HH 'o''clock' mm" to "h:mm a") to "h 'o''clock' mm",
            ("'' HH:mm" to "h:mm a") to "'' h:mm",
            ("HH'' mm" to "h:mm a") to "h'' mm",
            ("HH:mm" to "mm") to "hh:mm",
        )
        for ((input, expected) in cases) {
            assertEquals(input.toString(), expected, toTwelveHourPattern(input.first, input.second))
        }
    }

    /** Checks the format against every locale the device knows, using Android's own data. */
    @Test
    fun twelveHourNoAmPm_isTheTwelveHourClockWithoutAmPm_inEveryLocale() {
        val failures = mutableListOf<String>()
        val fallbacks = mutableListOf<String>()
        val patterns = sortedMapOf<String, MutableList<String>>()

        for (locale in Locale.getAvailableLocales()) {
            val tag = locale.toLanguageTag()
            val pattern24 = DateFormat.getBestDateTimePattern(locale, "HHmm")
            val pattern12 = DateFormat.getBestDateTimePattern(locale, "hhmm")
            val pattern = toTwelveHourPattern(pattern24, pattern12)
            val usesFallback =
                runCatching { DateTimeFormatter.ofPattern(pattern, locale) }.isFailure
            if (usesFallback) fallbacks += "$tag [$pattern]"
            patterns.getOrPut("[$pattern12] [$pattern24] -> [$pattern]") { mutableListOf() } += tag

            val noAmPm = HomeTimeFormat.TwelveHourNoAmPm.createFormatter(locale)
            val twentyFour = HomeTimeFormat.TwentyFourHour.createFormatter(locale)
            val twelve = HomeTimeFormat.TwelveHour.createFormatter(locale)
            // Locales like Japanese show 12:30 PM as "0:30"; we deliberately show 12:30
            val twelveUsesZeroHour = 'K' in unquotedLetters(pattern12)

            // Only hour and minute fields may remain: no AM/PM, no 0-23 hour
            if (!usesFallback) {
                val fields = unquotedLetters(pattern)
                if (!fields.all { it == 'h' || it == 'm' } || 'h' !in fields || 'm' !in fields) {
                    failures += "$tag: fields $fields in [$pattern]"
                }
            }

            for (minuteOfDay in 0 until 12 * 60 step 7) {
                val morning = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
                val afternoon = morning.plusHours(12)
                val text = morning.format(noAmPm)

                // Afternoon reads the same as morning, like a 12-hour clock
                if (afternoon.format(noAmPm) != text) {
                    failures += "$tag: $afternoon -> ${afternoon.format(noAmPm)}, $morning -> $text"
                }
                if (usesFallback) continue
                // The words and separators are the 24-hour clock's
                if (nonDigits(text) != nonDigits(morning.format(twentyFour))) {
                    failures += "$tag: $morning -> $text vs 24-hour ${morning.format(twentyFour)}"
                }
                // The numbers are the locale's own 12-hour clock's, hour width included
                if (!(twelveUsesZeroHour && morning.hour == 0) &&
                    digits(text) != digits(
                        withoutDayPeriod(
                            morning.format(twelve),
                            morning,
                            locale
                        )
                    )
                ) {
                    failures += "$tag: $morning -> $text vs 12-hour ${morning.format(twelve)}"
                }
            }
            // The 0 hour shows as 12 (never "0:05" or "00:05"), in the locale's own digits
            val noon = LocalTime.of(12, 5)
            val midnightText = LocalTime.of(0, 5).format(noAmPm)
            if (!usesFallback && digits(midnightText) != digits(noon.format(twentyFour))) {
                failures += "$tag: 00:05 -> $midnightText, expected hour 12 (${
                    noon.format(
                        twentyFour
                    )
                })"
            }
        }

        patterns.forEach { (mapping, tags) ->
            Log.i(
                TAG,
                "$mapping  ${tags.size}  ${tags.take(8)}"
            )
        }
        fallbacks.forEach { Log.i(TAG, "fallback: $it") }
        Log.i(TAG, "locales=${Locale.getAvailableLocales().size} failures=${failures.size}")
        // One example per failing locale, so every affected locale is listed
        val byLocale =
            failures.groupBy { it.substringBefore(':') }.map { (_, list) -> list.first() }
        assertTrue("${byLocale.size} locales:\n" + byLocale.joinToString("\n"), failures.isEmpty())
    }

    private fun unquotedLetters(pattern: String): Set<Char> {
        var quoted = false
        val letters = mutableSetOf<Char>()
        for (char in pattern) {
            if (char == '\'') quoted = !quoted
            else if (!quoted && char.isLetter()) letters += char
        }
        return letters
    }

    private fun digits(text: String): String = text.filter { it.isDigit() }

    private fun nonDigits(text: String): String = text.filterNot { it.isDigit() }

    /** Removes the AM/PM text, which can contain digits (Bodo's AM is "1ka"). */
    private fun withoutDayPeriod(text: String, time: LocalTime, locale: Locale): String =
        listOf("a", "B").fold(text) { result, letter ->
            val marker = runCatching { time.format(DateTimeFormatter.ofPattern(letter, locale)) }
                .getOrNull()
            if (marker.isNullOrEmpty()) result else result.replace(marker, "")
        }

    private companion object {
        const val TAG = "HomeTimeFormatTest"
    }
}
