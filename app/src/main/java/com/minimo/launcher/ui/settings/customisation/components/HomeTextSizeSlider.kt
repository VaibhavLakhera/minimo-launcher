package com.minimo.launcher.ui.settings.customisation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.theme.Dimens
import kotlin.math.roundToInt

@Composable
fun HomeTextSizeSlider(
    @StringRes titleRes: Int,
    value: Int?,
    defaultValue: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChanged: (Int?) -> Unit
) {
    val title = stringResource(titleRes)
    val effectiveValue = (value ?: defaultValue).toFloat().coerceIn(valueRange)

    Column(modifier = Modifier.padding(horizontal = Dimens.APP_HORIZONTAL_SPACING)) {
        Text(
            text = "$title: ${effectiveValue.roundToInt()}",
            fontSize = 20.sp
        )
        Slider(
            value = effectiveValue,
            onValueChange = { onValueChanged(it.roundToInt()) },
            valueRange = valueRange,
            steps = (valueRange.endInclusive - valueRange.start).toInt() - 1,
            modifier = Modifier.semantics { contentDescription = title }
        )
    }
}
