package com.minimo.launcher.ui.settings.customisation.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.DropdownView
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.ScreenOrientation

@Composable
fun OrientationDropdown(
    selectedOption: String,
    options: List<Pair<ScreenOrientation, String>>,
    onOptionSelected: (ScreenOrientation) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val platformControlsOrientation = remember(context, configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            // Use the full display, not the current (possibly narrow) split-screen window.
            val metrics = context.getSystemService(WindowManager::class.java).maximumWindowMetrics
            minOf(metrics.bounds.width(), metrics.bounds.height()) / metrics.density >= 600f
        } else {
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = 8.dp
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.orientation),
                modifier = Modifier.weight(1f),
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            DropdownView(
                selectedOption = selectedOption,
                options = options.map { it.second },
                onOptionSelected = { selected ->
                    onOptionSelected(options.first { it.second == selected }.first)
                }
            )
        }
        if (platformControlsOrientation) {
            Text(
                text = stringResource(R.string.orientation_large_screen_note),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
