package com.minimo.launcher.ui.components

import android.os.Build
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import com.minimo.launcher.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheetDialog(
    appName: String,
    onDismiss: () -> Unit,
    useDarkStatusBarIcons: Boolean? = null,
    useDarkNavigationBarIcons: Boolean? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val properties =
        if (useDarkStatusBarIcons != null && useDarkNavigationBarIcons != null) {
            ModalBottomSheetProperties(
                isAppearanceLightStatusBars = useDarkStatusBarIcons,
                isAppearanceLightNavigationBars = useDarkNavigationBarIcons
            )
        } else {
            ModalBottomSheetDefaults.properties
        }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = null,
        properties = properties
    ) {
        DisableBottomSheetNavigationBarContrast()

        Text(
            text = appName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                vertical = 24.dp,
                horizontal = Dimens.APP_HORIZONTAL_SPACING
            )
        )
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        content()
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun DisableBottomSheetNavigationBarContrast() {
    val view = LocalView.current
    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ((view as? DialogWindowProvider) ?: (view.parent as? DialogWindowProvider))
                ?.window
                ?.isNavigationBarContrastEnforced = false
        }
    }
}
