package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.home.components.SearchItem
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.Constants
import kotlin.math.roundToInt

@Composable
fun SearchBarBorderSlider(
    searchBarBorderPercent: Float,
    searchBarBackground: Boolean,
    onSearchBarBorderPercentChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.APP_HORIZONTAL_SPACING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.search_bar_border),
            modifier = Modifier.weight(1f, fill = false),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${searchBarBorderPercent.roundToInt()}%",
            fontSize = 20.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Slider(
        modifier = Modifier.padding(horizontal = Dimens.APP_HORIZONTAL_SPACING),
        value = searchBarBorderPercent,
        onValueChange = { onSearchBarBorderPercentChanged(it.roundToInt()) },
        valueRange = Constants.SEARCH_BAR_BORDER_PERCENT_RANGE,
        steps = 49
    )

    Spacer(modifier = Modifier.height(8.dp))

    SearchItem(
        modifier = Modifier.fillMaxWidth(),
        searchText = "",
        onSearchTextChange = {},
        searchBarBorderPercent = searchBarBorderPercent.roundToInt(),
        searchBarBackground = searchBarBackground,
        enabled = false
    )
}
