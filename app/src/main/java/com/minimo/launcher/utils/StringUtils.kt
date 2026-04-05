package com.minimo.launcher.utils

import android.content.Context
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.ThemeMode

object StringUtils {
    fun themeModeText(context: Context, mode: ThemeMode?): String {
        return when (mode) {
            ThemeMode.System -> context.getString(R.string.system)
            ThemeMode.Dark -> context.getString(R.string.dark)
            ThemeMode.Light -> context.getString(R.string.light)
            else -> ""
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