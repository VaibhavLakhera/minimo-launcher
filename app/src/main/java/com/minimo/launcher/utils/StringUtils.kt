package com.minimo.launcher.utils

import android.content.Context
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.ThemeMode
import java.text.Normalizer

object StringUtils {
    // Generic accents used by Latin, Greek and Cyrillic. Marks of other scripts
    // (Devanagari vowel signs, kana voicing marks, etc.) change meaning, so they are kept.
    private val combiningDiacriticalMarks = Regex("[\\u0300-\\u036F]+")

    // Letters that have no Unicode decomposition into a base letter and an accent.
    private val accentFreeLetters = mapOf(
        'ø' to "o", 'ł' to "l", 'đ' to "d", 'æ' to "ae", 'œ' to "oe", 'ß' to "ss",
        'ħ' to "h", 'ŧ' to "t", 'þ' to "th", 'ð' to "d", 'ŋ' to "n", 'ŀ' to "l"
    )

    /** Normalizes [searchText] once, so filtering a list does not repeat it for every app. */
    fun appSearchMatcher(searchText: String, mode: SearchMode): (appName: String) -> Boolean {
        val query = searchText.removeAccents()
        // A query of only accents is empty now and would otherwise match every app.
        if (query.isEmpty()) return { false }

        return { appName ->
            val name = appName.removeAccents()
            when (mode) {
                SearchMode.Contains -> name.contains(query, ignoreCase = true)
                SearchMode.StartsWith -> name.startsWith(query, ignoreCase = true)
            }
        }
    }

    /** Makes search accent-insensitive, e.g. "MyŠkoda" becomes "myskoda". */
    private fun String.removeAccents(): String {
        if (all { it.code < 0x80 }) return this

        val stripped = Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
            .replace(combiningDiacriticalMarks, "")
        // Re-compose so scripts like Hangul are compared as whole syllables again.
        return buildString {
            Normalizer.normalize(stripped, Normalizer.Form.NFC).forEach { char ->
                append(accentFreeLetters[char] ?: char)
            }
        }
    }

    fun searchModeText(context: Context, mode: SearchMode): String {
        return when (mode) {
            SearchMode.Contains -> context.getString(R.string.search_contains)
            SearchMode.StartsWith -> context.getString(R.string.search_starts_with)
        }
    }

    fun themeModeText(context: Context, mode: ThemeMode?): String {
        return when (mode) {
            ThemeMode.System -> context.getString(R.string.system)
            ThemeMode.Dark -> context.getString(R.string.dark)
            ThemeMode.Light -> context.getString(R.string.light)
            else -> mode?.preset?.name.orEmpty()
        }
    }

    fun screenOrientationText(context: Context, orientation: ScreenOrientation): String {
        return when (orientation) {
            ScreenOrientation.Portrait -> context.getString(R.string.portrait)
            ScreenOrientation.Landscape -> context.getString(R.string.landscape)
            ScreenOrientation.Auto -> context.getString(R.string.auto)
        }
    }

    fun homeAppsAlignmentHorizontalText(
        context: Context,
        alignment: HomeAppsAlignmentHorizontal?
    ): String {
        return when (alignment) {
            HomeAppsAlignmentHorizontal.Start -> context.getString(R.string.left)
            HomeAppsAlignmentHorizontal.Center -> context.getString(R.string.center)
            HomeAppsAlignmentHorizontal.End -> context.getString(R.string.right)
            else -> ""
        }
    }

    fun appIconAlignmentText(context: Context, alignment: AppIconAlignment): String {
        return when (alignment) {
            AppIconAlignment.Left -> context.getString(R.string.left)
            AppIconAlignment.Right -> context.getString(R.string.right)
        }
    }

    fun fastScrollerAlignmentText(context: Context, alignment: FastScrollerAlignment): String {
        return when (alignment) {
            FastScrollerAlignment.Left -> context.getString(R.string.left)
            FastScrollerAlignment.Right -> context.getString(R.string.right)
        }
    }

    fun homeAppsAlignmentVerticalText(
        context: Context,
        alignment: HomeAppsAlignmentVertical?
    ): String {
        return when (alignment) {
            HomeAppsAlignmentVertical.Top -> context.getString(R.string.top)
            HomeAppsAlignmentVertical.Center -> context.getString(R.string.center)
            HomeAppsAlignmentVertical.Bottom -> context.getString(R.string.bottom)
            else -> ""
        }
    }

    fun homeClockAlignmentText(context: Context, alignment: HomeClockAlignment?): String {
        return when (alignment) {
            HomeClockAlignment.Start -> context.getString(R.string.left)
            HomeClockAlignment.Center -> context.getString(R.string.center)
            HomeClockAlignment.End -> context.getString(R.string.right)
            else -> ""
        }
    }

    fun homeClockModeText(context: Context, mode: HomeClockMode?): String {
        return when (mode) {
            HomeClockMode.Full -> context.getString(R.string.full)
            HomeClockMode.TimeOnly -> context.getString(R.string.time_only)
            HomeClockMode.DateOnly -> context.getString(R.string.date_only)
            else -> ""
        }
    }

    fun minimoSettingsPositionText(
        context: Context,
        position: MinimoSettingsPosition
    ): String {
        return when (position) {
            MinimoSettingsPosition.Auto -> context.getString(R.string.auto)
            MinimoSettingsPosition.Top -> context.getString(R.string.top)
            MinimoSettingsPosition.Bottom -> context.getString(R.string.bottom)
        }
    }
}
