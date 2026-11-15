package com.minimo.launcher.ui.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

/**
 * A small glyph drawn beside app and folder names, with the same shadow as the text so it stays
 * readable on a wallpaper. [modifier] sizes or pads the slot; the glyph is centred in it.
 */
@Composable
internal fun ShadowedIcon(
    @DrawableRes iconRes: Int,
    size: Dp,
    tint: Color,
    shadow: Shadow?,
    modifier: Modifier = Modifier
) {
    val painter = painterResource(iconRes)
    Box(modifier, contentAlignment = Alignment.Center) {
        if (shadow != null) {
            val density = LocalDensity.current
            Icon(
                painter = painter,
                contentDescription = null,
                tint = shadow.color,
                modifier = Modifier
                    .size(size)
                    .offset(
                        x = with(density) { shadow.offset.x.toDp() },
                        y = with(density) { shadow.offset.y.toDp() }
                    )
                    .blur(with(density) { shadow.blurRadius.toDp() })
            )
        }
        Icon(
            painter = painter,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
