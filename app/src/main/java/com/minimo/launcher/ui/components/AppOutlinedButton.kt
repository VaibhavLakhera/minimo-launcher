package com.minimo.launcher.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppOutlinedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    OutlinedButton(
        modifier = modifier.height(if (compact) 40.dp else 56.dp),
        onClick = onClick,
        enabled = enabled,
        contentPadding = if (compact) PaddingValues(horizontal = 16.dp) else ButtonDefaults.ContentPadding
    ) {
        Text(text = text, fontSize = if (compact) 14.sp else 18.sp)
    }
}
