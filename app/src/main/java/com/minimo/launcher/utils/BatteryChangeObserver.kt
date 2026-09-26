package com.minimo.launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

data class BatteryState(val percent: Int, val isCharging: Boolean)

class BatteryChangeObserver(
    private val context: Context,
    private val onBatteryChanged: (BatteryState) -> Unit
) : DefaultLifecycleObserver {

    private var receiver: BroadcastReceiver? = null

    override fun onResume(owner: LifecycleOwner) {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                intent?.batteryState()?.let(onBatteryChanged)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)?.batteryState()?.let(onBatteryChanged)
    }

    override fun onPause(owner: LifecycleOwner) = dispose()

    override fun onDestroy(owner: LifecycleOwner) = dispose()

    fun dispose() {
        receiver?.let { context.unregisterReceiver(it) }
        receiver = null
    }
}

fun Context.currentBatteryState(): BatteryState? =
    registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.batteryState()

internal fun Intent.batteryState(): BatteryState? {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level < 0 || scale <= 0) return null

    return BatteryState(
        percent = ((level.toLong() * 100) / scale).toInt().coerceIn(0, 100),
        isCharging = getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                BatteryManager.BATTERY_STATUS_CHARGING
    )
}
