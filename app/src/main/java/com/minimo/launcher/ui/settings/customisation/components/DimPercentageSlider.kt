package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun DimPercentageSlider(
    dimPercentage: Float,
    onDimPercentageChanged: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.padding(
            horizontal = Dimens.APP_HORIZONTAL_SPACING,
        )
    ) {
        Text(
            text = stringResource(R.string.dim_percentage),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${dimPercentage.roundToInt()}%",
            fontSize = 20.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Slider(
        modifier = Modifier
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING),
        value = dimPercentage,
        onValueChange = {
            onDimPercentageChanged(it.roundToInt())
        },
        valueRange = Constants.DIM_WALLPAPER_PERCENTAGE_RANGE,
        steps = 5,
    )

    Spacer(modifier = Modifier.height(12.dp))
}
