package com.minimo.launcher.ui.settings.customisation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.DropdownView
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.SearchMode

@Composable
fun SearchModeDropdown(
    selectedOption: String,
    options: List<Pair<SearchMode, String>>,
    onOptionSelected: (SearchMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.search_mode),
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = stringResource(R.string.search_mode_description))
        }

        Spacer(modifier = Modifier.width(16.dp))

        DropdownView(
            selectedOption = selectedOption,
            options = options.map { it.second },
            onOptionSelected = { selected ->
                onOptionSelected(options.first { it.second == selected }.first)
            }
        )
    }
}
