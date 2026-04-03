package com.minimo.launcher.utils

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun startBillingConnection(callback: BillingManagerCallback) {
        callback.onSetupFinished(false)
    }

    fun endBillingConnection() {}
    suspend fun queryProducts(): List<AppProduct> = emptyList()
    fun launchBilling(activity: Activity, product: AppProduct) {}
}

interface BillingManagerCallback {
    fun onSetupFinished(isSuccess: Boolean)
    fun onBillingServiceDisconnected()
    fun onPremiumPurchaseComplete()
}