package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.AppIconAlignment
import com.minimo.launcher.utils.Constants

@Composable
fun MinimoSettingsItem(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    textSize: TextUnit,
    onClick: () -> Unit,
    compactTouchArea: Boolean = false,
    verticalPadding: Dp = 16.dp,
    showAppIcon: Boolean = false,
    appIconSizeScale: Float = Constants.DEFAULT_APP_ICON_SIZE_PERCENT / 100f,
    appIconAlignment: AppIconAlignment = AppIconAlignment.Left,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textShadow: Shadow? = null
) {
    val lineHeight by remember { derivedStateOf { textSize * 1.2 } }
    val contentAlignment = when (horizontalArrangement) {
        Arrangement.Center -> Alignment.Center
        Arrangement.End -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = contentAlignment
    ) {
        Row(
            modifier = (if (compactTouchArea) Modifier else Modifier.fillMaxWidth())
                .clickable { onClick() }
                .padding(
                    PaddingValues(
                        horizontal = Dimens.APP_HORIZONTAL_SPACING,
                        vertical = verticalPadding
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (compactTouchArea) {
                Arrangement.Start
            } else {
                horizontalArrangement
            }
        ) {
            if (showAppIcon && appIconAlignment == AppIconAlignment.Left) {
                MinimoAppIcon(size = appIconSizeFor(textSize, appIconSizeScale))
                Spacer(modifier = Modifier.width(Dimens.APP_ICON_LABEL_SPACING))
            }

            Text(
                text = stringResource(R.string.minimo_settings),
                modifier = if (showAppIcon && appIconAlignment == AppIconAlignment.Right) {
                    Modifier.weight(1f, fill = false)
                } else {
                    Modifier
                },
                color = textColor,
                fontSize = textSize,
                lineHeight = lineHeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = LocalTextStyle.current.copy(shadow = textShadow)
            )

            if (showAppIcon && appIconAlignment == AppIconAlignment.Right) {
                Spacer(modifier = Modifier.width(Dimens.APP_ICON_LABEL_SPACING))
                MinimoAppIcon(size = appIconSizeFor(textSize, appIconSizeScale))
            }
        }
    }
}

@Composable
private fun MinimoAppIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = Dimens.APP_ICON_SHADOW_ELEVATION,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
