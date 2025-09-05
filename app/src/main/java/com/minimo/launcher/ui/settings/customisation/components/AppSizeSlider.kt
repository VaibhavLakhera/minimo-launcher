package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.Constants
import kotlin.math.roundToInt

@Composable
fun AppSizeSlider(
    homeTextSize: Float,
    onHomeTextSizeChanged: (Int) -> Unit,
    homeAppVerticalPadding: Float,
    onHomeVerticalPaddingChanged: (Int) -> Unit,
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

    Text(
        stringResource(R.string.sample_app),
        fontSize = homeTextSize.sp,
        lineHeight = homeTextSize.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = homeAppVerticalPadding.dp
            )
    )
}