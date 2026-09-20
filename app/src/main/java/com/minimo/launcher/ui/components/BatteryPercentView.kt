package com.minimo.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.minimo.launcher.R
import com.minimo.launcher.utils.BatteryChangeObserver
import com.minimo.launcher.utils.currentBatteryState
import java.text.NumberFormat

@Composable
fun BatteryPercentView(
    fontSize: TextUnit,
    fontWeight: FontWeight?,
    textColor: Color,
    textShadow: Shadow?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var batteryState by remember(context) { mutableStateOf(context.currentBatteryState()) }
    val locale = LocalConfiguration.current.locales[0]
    val percentFormatter = remember(locale) {
        NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 0 }
    }
    val batteryPercent = batteryState?.percent
    val batteryText = remember(batteryPercent, percentFormatter) {
        batteryPercent?.let { percentFormatter.format(it / 100.0) }.orEmpty()
    }

    DisposableEffect(context, lifecycleOwner) {
        val observer = BatteryChangeObserver(context) { batteryState = it }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            observer.dispose()
        }
    }

    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = batteryText,
            fontSize = fontSize,
            softWrap = false,
            fontWeight = fontWeight,
            color = textColor,
            style = LocalTextStyle.current.copy(shadow = textShadow)
        )
        AnimatedVisibility(
            visible = batteryState?.isCharging == true,
            // Animate the occupied width too, so the parent date/battery row reflows smoothly.
            // Leave clipping off to preserve the icon's fade + scale appearance.
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f) +
                    expandHorizontally(tween(300), expandFrom = Alignment.Start, clip = false),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f) +
                    shrinkHorizontally(tween(300), shrinkTowards = Alignment.Start, clip = false)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_charging),
                contentDescription = stringResource(R.string.battery_charging),
                tint = textColor,
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(with(LocalDensity.current) { fontSize.toDp() })
            )
        }
    }
}
