package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.AppIconAlignment
import com.minimo.launcher.utils.Constants
import kotlin.math.roundToInt

@Composable
fun AppSizeSlider(
    homeTextSize: Float,
    onHomeTextSizeChanged: (Int) -> Unit,
    homeAppVerticalPadding: Float,
    onHomeVerticalPaddingChanged: (Int) -> Unit,
    showAppIcon: Boolean,
    appIconSizeScale: Float,
    appIconAlignment: AppIconAlignment,
) {
    Row(
        modifier = Modifier.padding(
            horizontal = Dimens.APP_HORIZONTAL_SPACING,
        )
    ) {
        Text(
            text = stringResource(R.string.home_app_size),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = homeTextSize.roundToInt().toString(),
            fontSize = 20.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Slider(
        modifier = Modifier
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING),
        value = homeTextSize,
        onValueChange = {
            onHomeTextSizeChanged(it.roundToInt())
        },
        valueRange = Constants.HOME_TEXT_SIZE_RANGE,
        steps = 16,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.padding(
            horizontal = Dimens.APP_HORIZONTAL_SPACING,
        )
    ) {
        Text(
            text = stringResource(R.string.home_app_spacing),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = homeAppVerticalPadding.roundToInt().toString(),
            fontSize = 20.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Slider(
        modifier = Modifier
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING),
        value = homeAppVerticalPadding,
        onValueChange = {
            onHomeVerticalPaddingChanged(it.roundToInt())
        },
        valueRange = Constants.HOME_VERTICAL_PADDING_RANGE,
        steps = 7,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = homeAppVerticalPadding.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconSize = with(LocalDensity.current) {
            (homeTextSize.sp * appIconSizeScale).toDp()
        }

        if (showAppIcon && appIconAlignment == AppIconAlignment.Left) {
            SampleAppIcon(size = iconSize)
            Spacer(modifier = Modifier.width(Dimens.APP_ICON_LABEL_SPACING))
        }

        Text(
            text = stringResource(R.string.sample_app),
            modifier = if (showAppIcon && appIconAlignment == AppIconAlignment.Right) {
                Modifier.weight(1f, fill = false)
            } else {
                Modifier
            },
            fontSize = homeTextSize.sp,
            lineHeight = homeTextSize.sp
        )

        if (showAppIcon && appIconAlignment == AppIconAlignment.Right) {
            Spacer(modifier = Modifier.width(Dimens.APP_ICON_LABEL_SPACING))
            SampleAppIcon(size = iconSize)
        }
    }
}

@Composable
private fun SampleAppIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = Dimens.APP_ICON_SHADOW_ELEVATION,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
