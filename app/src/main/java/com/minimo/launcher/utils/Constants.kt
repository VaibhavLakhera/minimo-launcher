package com.minimo.launcher.utils

object Constants {
    const val INTRO_MINIMUM_FAVOURITE_COUNT = 1

    const val DEFAULT_HOME_TEXT_SIZE = 20
    val HOME_TEXT_SIZE_RANGE by lazy { 16f..50f }

    const val DEFAULT_HOME_VERTICAL_PADDING = 16
    val HOME_VERTICAL_PADDING_RANGE by lazy { 4f..20f }

    const val DEFAULT_KEYBOARD_OPEN_DELAY = 150L
    val KEYBOARD_OPEN_DELAY_RANGE by lazy { 0L..1500L }

    // A dummy package used to show "Minimo Settings" in the app drawer when "Hide App Drawer Search" toggle is on.
    const val MINIMO_SETTINGS_PACKAGE = "APP::com.minimo.launcher.settings"
}

enum class HomeAppsAlignmentHorizontal {
    Start, Center, End
}

enum class HomeAppsAlignmentVertical {
    Top, Center, Bottom
}

enum class HomeClockAlignment {
    Start, Center, End
}

enum class HomeClockMode {
    Full, TimeOnly, DateOnly
}

enum class MinimoSettingsPosition {
    Auto, Top, Bottom
}
