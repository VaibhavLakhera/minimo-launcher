package com.minimo.launcher.ui.theme

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.minimo.launcher.R
import com.minimo.launcher.data.CustomFonts

val AvailableFonts = listOf("DynaPuff", "Inter", "Lexend", "Nunito", "Poppins")

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

val NunitoFontFamily by lazy {
    FontFamily(
        Font(R.font.nunito_regular, FontWeight.Normal),
        Font(R.font.nunito_semibold, FontWeight.SemiBold),
        Font(R.font.nunito_bold, FontWeight.Bold)
    )
}

fun getTypographyForFont(fontFamily: FontFamily?): Typography {
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

@Composable
fun rememberFontFamily(fontName: String): FontFamily? {
    when (fontName) {
        "Inter" -> return InterFontFamily
        "Lexend" -> return LexendFontFamily
        "Poppins" -> return PoppinsFontFamily
        "DynaPuff" -> return DynaPuffFontFamily
        "Nunito" -> return NunitoFontFamily
    }
    if (!CustomFonts.isCustom(fontName)) return null

    val context = LocalContext.current.applicationContext
    // Start from the cache so an already loaded font is used in the first frame, not the default.
    var family by remember(context, fontName) {
        mutableStateOf(CustomFonts.cachedTypeface(fontName)?.let(::customFontFamily))
    }
    LaunchedEffect(context, fontName) {
        if (family == null) {
            family = CustomFonts.typeface(context, fontName)?.let(::customFontFamily)
        }
    }
    return family
}

private fun customFontFamily(typeface: Typeface) = FontFamily(LoadedCustomFont(typeface))

// Unlike FontFamily(Typeface), a Font participates in Compose's weight/style synthesis.
// The typeface is already loaded on IO; Compose's blocking loader only returns it from memory.
private data class LoadedCustomFont(val typeface: Typeface) : AndroidFont(
    loadingStrategy = FontLoadingStrategy.Blocking,
    typefaceLoader = Loader,
    variationSettings = FontVariation.Settings()
) {
    override val weight = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> FontWeight(
            typeface.weight.coerceIn(
                1,
                1000
            )
        )

        typeface.isBold -> FontWeight.Bold
        else -> FontWeight.Normal
    }
    override val style = if (typeface.isItalic) FontStyle.Italic else FontStyle.Normal

    private object Loader : TypefaceLoader {
        override fun loadBlocking(context: Context, font: AndroidFont): Typeface =
            (font as LoadedCustomFont).typeface

        override suspend fun awaitLoad(context: Context, font: AndroidFont): Typeface =
            loadBlocking(context, font)
    }
}
