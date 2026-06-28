package com.minimo.launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class BatteryChangeObserver(
    private val context: Context,
    private val onBatteryChanged: (Int) -> Unit
) : DefaultLifecycleObserver {

    private var receiver: BroadcastReceiver? = null

    override fun onResume(owner: LifecycleOwner) {
        context.currentBatteryPercent()?.let(onBatteryChanged)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                intent?.batteryPercent()?.let(onBatteryChanged)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)?.batteryPercent()?.let(onBatteryChanged)
    }

    override fun onPause(owner: LifecycleOwner) {
        receiver?.let { context.unregisterReceiver(it) }
        receiver = null
    }
}

fun Context.currentBatteryPercent(): Int? {
    return registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.batteryPercent()
}

private fun Intent.batteryPercent(): Int? {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level < 0 || scale <= 0) return null

    return (level * 100) / scale
}
