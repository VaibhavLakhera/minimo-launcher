package com.minimo.launcher.utils

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class BillingManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    companion object {
        private val ONE_TIME_PRODUCTS = listOf(
            "supporter_1_onetime",
            "supporter_2_onetime",
            "supporter_5_onetime",
            "supporter_10_onetime"
        )
    }

    private var callback: BillingManagerCallback? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == PurchaseState.PURCHASED) {
                        callback?.onPremiumPurchaseComplete()
                        if (!purchase.isAcknowledged) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.d("BillingClient.BillingResponseCode.USER_CANCELED")
            }

            else -> {
                Timber.d("${billingResult.responseCode}: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val isConsumable = purchase.products.any { it in ONE_TIME_PRODUCTS }
        if (isConsumable) {
            val consumeParams = ConsumeParams
                .newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Consume complete
                    Timber.d("BillingClient: Consume complete")
                }
            }
        } else {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Acknowledgement complete
                    Timber.d("BillingClient: Acknowledgement complete")
                }
            }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun startBillingConnection(callback: BillingManagerCallback) {
        this.callback = callback
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                callback.onSetupFinished(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            }

            override fun onBillingServiceDisconnected() {
                callback.onBillingServiceDisconnected()
            }
        })
    }

    fun endBillingConnection() {
        if (billingClient.connectionState != BillingClient.ConnectionState.DISCONNECTED &&
            billingClient.connectionState != BillingClient.ConnectionState.CLOSED
        ) {
            billingClient.endConnection()
        }
        callback = null
    }

    suspend fun queryProducts(): List<AppProduct> {
        val productList = mutableListOf<QueryProductDetailsParams.Product>()

        ONE_TIME_PRODUCTS.forEach { id ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        val appProducts = mutableListOf<AppProduct>()

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList?.forEach { details ->
                if (details.productType == BillingClient.ProductType.INAPP) {
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    appProducts.add(
                        AppProduct(
                            productId = details.productId,
                            name = details.name,
                            price = price,
                            originalDetails = details
                        )
                    )
                }
            }
        }
        return appProducts
    }

    fun launchBilling(activity: Activity, product: AppProduct) {
        val details = product.originalDetails as? ProductDetails ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
}

interface BillingManagerCallback {
    fun onSetupFinished(isSuccess: Boolean)
    fun onBillingServiceDisconnected()
    fun onPremiumPurchaseComplete()
}