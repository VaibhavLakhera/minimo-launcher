package com.minimo.launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.minimo.launcher.R

val AvailableFonts = listOf("DynaPuff", "Inter", "Lexend", "NotoSans", "Poppins")

val InterFontFamily by lazy {
    FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold)
    )
}

val LexendFontFamily by lazy {
    FontFamily(
        Font(R.font.lexend_regular, FontWeight.Normal),
        Font(R.font.lexend_semibold, FontWeight.SemiBold),
        Font(R.font.lexend_bold, FontWeight.Bold)
    )
}

val PoppinsFontFamily by lazy {
    FontFamily(
        Font(R.font.poppins_regular, FontWeight.Normal),
        Font(R.font.poppins_semibold, FontWeight.SemiBold),
        Font(R.font.poppins_bold, FontWeight.Bold)
    )
}

val DynaPuffFontFamily by lazy {
    FontFamily(
        Font(R.font.dynapuff_regular, FontWeight.Normal),
        Font(R.font.dynapuff_semibold, FontWeight.SemiBold),
        Font(R.font.dynapuff_bold, FontWeight.Bold)
    )
}

val NotoSansFontFamily by lazy {
    FontFamily(
        Font(R.font.notosans_regular, FontWeight.Normal),
        Font(R.font.notosans_semibold, FontWeight.SemiBold),
        Font(R.font.notosans_bold, FontWeight.Bold)
    )
}

fun getTypographyForFont(fontName: String): Typography {
    val fontFamily = getFontFamily(fontName)
    return if (fontFamily != null) {
        Typography().copy(
            displayLarge = Typography().displayLarge.copy(fontFamily = fontFamily),
            displayMedium = Typography().displayMedium.copy(fontFamily = fontFamily),
            displaySmall = Typography().displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = Typography().headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = Typography().headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = Typography().headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = Typography().titleLarge.copy(fontFamily = fontFamily),
            titleMedium = Typography().titleMedium.copy(fontFamily = fontFamily),
            titleSmall = Typography().titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = Typography().bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = Typography().bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = Typography().bodySmall.copy(fontFamily = fontFamily),
            labelLarge = Typography().labelLarge.copy(fontFamily = fontFamily),
            labelMedium = Typography().labelMedium.copy(fontFamily = fontFamily),
            labelSmall = Typography().labelSmall.copy(fontFamily = fontFamily),
        )
    } else {
        Typography()
    }
}

fun getFontFamily(fontName: String): FontFamily? {
    return when (fontName) {
        "Inter" -> InterFontFamily
        "Lexend" -> LexendFontFamily
        "Poppins" -> PoppinsFontFamily
        "DynaPuff" -> DynaPuffFontFamily
        "NotoSans" -> NotoSansFontFamily
        else -> null
    }
}
