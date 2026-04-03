package com.minimo.launcher.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.utils.AppProduct
import com.minimo.launcher.utils.BillingManager
import com.minimo.launcher.utils.BillingManagerCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupporterViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel(), BillingManagerCallback {

    private val _products = MutableStateFlow<List<AppProduct>>(emptyList())
    val products: StateFlow<List<AppProduct>> = _products

    private val _purchaseComplete = MutableStateFlow(false)
    val purchaseComplete: StateFlow<Boolean> = _purchaseComplete

    init {
        billingManager.startBillingConnection(this)
    }

    override fun onSetupFinished(isSuccess: Boolean) {
        if (isSuccess) {
            viewModelScope.launch {
                _products.value = billingManager.queryProducts()
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        // Retry logic could go here
    }

    override fun onPremiumPurchaseComplete() {
        _purchaseComplete.value = true
    }

    fun resetPurchaseComplete() {
        _purchaseComplete.value = false
    }

    fun launchBilling(activity: Activity, product: AppProduct) {
        billingManager.launchBilling(activity, product)
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endBillingConnection()
    }
}
