package com.minimo.launcher.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun rememberCurrentDateTime(
    enabled: Boolean = true,
    updateEveryMinute: Boolean = true
): LocalDateTime {
    val context = LocalContext.current
    var currentDateTime by remember(enabled, updateEveryMinute) {
        mutableStateOf(currentDateTimeSnapshot(updateEveryMinute))
    }

    LifecycleResumeEffect(context, enabled, updateEveryMinute) {
        val receiver = if (enabled) {
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    currentDateTime = currentDateTimeSnapshot(updateEveryMinute)
                }
            }
        } else {
            null
        }

        if (receiver != null) {
            val filter = IntentFilter().apply {
                if (updateEveryMinute) addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            currentDateTime = currentDateTimeSnapshot(updateEveryMinute)
        }

        onPauseOrDispose {
            receiver?.let { context.unregisterReceiver(it) }
        }
    }
    return currentDateTime
}

private fun currentDateTimeSnapshot(includeTime: Boolean): LocalDateTime {
    val now = LocalDateTime.now()
    return if (includeTime) now.truncatedTo(ChronoUnit.MINUTES) else now.toLocalDate()
        .atStartOfDay()
}
