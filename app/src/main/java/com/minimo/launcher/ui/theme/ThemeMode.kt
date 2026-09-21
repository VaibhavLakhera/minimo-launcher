package com.minimo.launcher.ui.theme

import androidx.compose.ui.graphics.Color

// Entry names are persisted preference IDs. Preset names are intentionally not localized.
enum class ThemeMode(internal val preset: ThemePreset? = null) {
    System, Dark, Light,
    ClassicPaper(
        ThemePreset(
            name = "Classic Paper",
            background = Color(0xFFFFFFFF),
            foreground = Color(0xFF111827),
            accent = Color(0xFF2563EB),
            dark = false
        )
    ),
    Midnight(
        ThemePreset(
            name = "Midnight",
            background = Color(0xFF0F172A),
            foreground = Color(0xFFF8FAFC),
            accent = Color(0xFF38BDF8),
            dark = true
        )
    ),
    SlateNight(
        ThemePreset(
            name = "Slate Night",
            background = Color(0xFF1E293B),
            foreground = Color(0xFFE2E8F0),
            accent = Color(0xFFFBBF24),
            dark = true
        )
    ),
    Dracula(
        ThemePreset(
            name = "Dracula",
            background = Color(0xFF282A36),
            foreground = Color(0xFFF8F8F2),
            accent = Color(0xFFBD93F9),
            dark = true
        )
    ),
    SolarizedDark(
        ThemePreset(
            name = "Solarized Dark",
            background = Color(0xFF002B36),
            foreground = Color(0xFFEEE8D5),
            accent = Color(0xFF2AA198),
            dark = true
        )
    ),
    CharcoalGold(
        ThemePreset(
            name = "Charcoal Gold",
            background = Color(0xFF1C1C1E),
            foreground = Color(0xFFF5F5F0),
            accent = Color(0xFFF5C542),
            dark = true
        )
    ),
    NavyAmber(
        ThemePreset(
            name = "Navy Amber",
            background = Color(0xFF14213D),
            foreground = Color(0xFFF5F7FA),
            accent = Color(0xFFFCA311),
            dark = true
        )
    ),
    IndigoDusk(
        ThemePreset(
            name = "Indigo Dusk",
            background = Color(0xFF312E81),
            foreground = Color(0xFFE0E7FF),
            accent = Color(0xFFFCD34D),
            dark = true
        )
    ),
    OceanDeep(
        ThemePreset(
            name = "Ocean Deep",
            background = Color(0xFF0B3C5D),
            foreground = Color(0xFFF1F5F9),
            accent = Color(0xFFFFC857),
            dark = true
        )
    ),
    DeepTeal(
        ThemePreset(
            name = "Deep Teal",
            background = Color(0xFF0F766E),
            foreground = Color(0xFFF0FDFA),
            accent = Color(0xFFFDE68A),
            dark = true
        )
    ),
    Forest(
        ThemePreset(
            name = "Forest",
            background = Color(0xFF14532D),
            foreground = Color(0xFFECFDF5),
            accent = Color(0xFFBEF264),
            dark = true
        )
    ),
    Crimson(
        ThemePreset(
            name = "Crimson",
            background = Color(0xFF7F1D1D),
            foreground = Color(0xFFFEE2E2),
            accent = Color(0xFFFCD34D),
            dark = true
        )
    ),
    CreamEspresso(
        ThemePreset(
            name = "Cream Espresso",
            background = Color(0xFFFAF3E7),
            foreground = Color(0xFF3E2723),
            accent = Color(0xFFB45309),
            dark = false
        )
    ),
    Sunrise(
        ThemePreset(
            name = "Sunrise",
            background = Color(0xFFFFF4E0),
            foreground = Color(0xFF5A2D0C),
            accent = Color(0xFFC2410C),
            dark = false
        )
    ),
    SageMist(
        ThemePreset(
            name = "Sage Mist",
            background = Color(0xFFE8F0E4),
            foreground = Color(0xFF1F3A2B),
            accent = Color(0xFF6D28D9),
            dark = false
        )
    ),
    MintFresh(
        ThemePreset(
            name = "Mint Fresh",
            background = Color(0xFFD1FAE5),
            foreground = Color(0xFF064E3B),
            accent = Color(0xFF0E7490),
            dark = false
        )
    ),
    SkyBlue(
        ThemePreset(
            name = "Sky Blue",
            background = Color(0xFFE0F2FE),
            foreground = Color(0xFF0C4A6E),
            accent = Color(0xFFBE185D),
            dark = false
        )
    ),
    Lavender(
        ThemePreset(
            name = "Lavender",
            background = Color(0xFFEDE9FE),
            foreground = Color(0xFF3B1D8A),
            accent = Color(0xFF0F766E),
            dark = false
        )
    ),
    RosePetal(
        ThemePreset(
            name = "Rose Petal",
            background = Color(0xFFFFE4E6),
            foreground = Color(0xFF881337),
            accent = Color(0xFF0369A1),
            dark = false
        )
    );

    val isCustom: Boolean
        get() = preset != null
}
