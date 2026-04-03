package com.minimo.launcher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun EmptyScreenView(
    title: String,
    subTitle: String? = null,
    button: @Composable (() -> Unit)? = null,
    horizontalPadding: Dp = Dimens.APP_HORIZONTAL_SPACING
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = horizontalPadding)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        if (subTitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subTitle,
                textAlign = TextAlign.Center
            )
        }
        if (button != null) {
            Spacer(modifier = Modifier.height(16.dp))
            button()
        }
    }
}