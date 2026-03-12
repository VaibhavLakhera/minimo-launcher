package com.minimo.launcher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.minimo.launcher.utils.BatteryChangeObserver
import com.minimo.launcher.utils.openPowerUsageSummary

@Composable
fun BatteryPercentView(
    fontSize: TextUnit,
    fontWeight: FontWeight?,
    textColor: Color,
    textShadow: Shadow?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var batteryPercent by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = BatteryChangeObserver(context) { percent ->
            batteryPercent = percent
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Text(
        modifier = Modifier.clickable(onClick = context::openPowerUsageSummary),
        text = "$batteryPercent%",
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = textColor,
        style = LocalTextStyle.current.copy(shadow = textShadow)
    )
}
