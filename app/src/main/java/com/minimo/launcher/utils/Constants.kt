package com.minimo.launcher.utils

object Constants {
    const val INTRO_MINIMUM_FAVOURITE_COUNT = 1

    const val DEFAULT_HOME_TEXT_SIZE = 20
    val HOME_TEXT_SIZE_RANGE by lazy { 16f..50f }

    const val DEFAULT_HOME_VERTICAL_PADDING = 16
    val HOME_VERTICAL_PADDING_RANGE by lazy { 4f..20f }
}

enum class HomeAppsAlignment {
    Start, Center, End
}

enum class HomeClockAlignment {
    Start, Center, End
}

enum class HomeClockMode {
    Full, TimeOnly, DateOnly
}