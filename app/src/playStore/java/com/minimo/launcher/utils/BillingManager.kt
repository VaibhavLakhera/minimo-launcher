package com.minimo.launcher.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.edit
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
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Connects to Play Billing only on demand and closes the connection as soon as nothing is
 * using it. Public methods are called on the main thread and every billing callback is hopped
 * onto it via [runOnMain], so the mutable state here needs no synchronization.
 */
@Singleton
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

        private const val PREFS_NAME = "billing"
        private const val KEY_NEEDS_PURCHASE_CHECK = "needs_purchase_check"
        private const val PURCHASE_CHECK_INTERVAL_MS = 60 * 60 * 1000L
    }

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val mainHandler = Handler(Looper.getMainLooper())

    private var billingClient: BillingClient? = null
    private var callback: BillingManagerCallback? = null
    private val onConnectedActions = mutableListOf<(Boolean) -> Unit>()

    // Queries/consumes still running; the connection is kept open until they finish.
    private var inFlightOps = 0
    private var lastPurchaseCheckAt = 0L

    // Play Billing invokes some listeners (e.g. query/consume) on its own background thread.
    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        runOnMain {
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
    }

    private fun handlePurchase(purchase: Purchase) {
        val client = billingClient ?: return
        inFlightOps++
        val isConsumable = purchase.products.any { it in ONE_TIME_PRODUCTS }
        if (isConsumable) {
            val consumeParams = ConsumeParams
                .newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            client.consumeAsync(consumeParams) { billingResult, _ ->
                runOnMain {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // Consume complete
                        Timber.d("BillingClient: Consume complete")
                    } else {
                        Timber.w("BillingClient: Consume failed ${billingResult.responseCode}: ${billingResult.debugMessage}")
                    }
                    inFlightOps--
                    closeIfIdle()
                }
            }
        } else {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                runOnMain {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // Acknowledgement complete
                        Timber.d("BillingClient: Acknowledgement complete")
                    } else {
                        Timber.w("BillingClient: Acknowledgement failed ${billingResult.responseCode}: ${billingResult.debugMessage}")
                    }
                    inFlightOps--
                    closeIfIdle()
                }
            }
        }
    }

    /**
     * Consumes purchases that were missed by [purchasesUpdatedListener], e.g. a pending purchase
     * that completed while the app was not connected, or a consume that failed or was cut off.
     */
    private fun consumeOwnedPurchases() {
        val client = billingClient ?: return
        inFlightOps++
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { billingResult, purchases ->
            runOnMain {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases
                        .filter { it.purchaseState == PurchaseState.PURCHASED }
                        .forEach { handlePurchase(it) }
                    if (purchases.isEmpty()) {
                        prefs.edit { putBoolean(KEY_NEEDS_PURCHASE_CHECK, false) }
                    }
                }
                inFlightOps--
                closeIfIdle()
            }
        }
    }

    private fun createBillingClient(): BillingClient {
        return BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .enableAutoServiceReconnection()
            .build()
    }

    private fun withConnection(action: (isSuccess: Boolean) -> Unit) {
        // A closed client can't be reused, so a new one is created for every connection.
        val client = billingClient ?: createBillingClient().also { billingClient = it }
        if (client.isReady) {
            action(true)
            return
        }
        onConnectedActions.add(action)
        if (client.connectionState == BillingClient.ConnectionState.CONNECTING) return

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                runOnMain {
                    val isSuccess =
                        billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    val actions = onConnectedActions.toList()
                    onConnectedActions.clear()
                    actions.forEach { it(isSuccess) }
                }
            }

            override fun onBillingServiceDisconnected() {
                runOnMain {
                    callback?.onBillingServiceDisconnected()
                }
            }
        })
    }

    private fun closeIfIdle() {
        if (callback != null || inFlightOps > 0 || onConnectedActions.isNotEmpty()) return
        billingClient?.endConnection()
        billingClient = null
    }

    fun startBillingConnection(callback: BillingManagerCallback) {
        this.callback = callback
        withConnection { isSuccess ->
            if (isSuccess) {
                consumeOwnedPurchases()
            }
            this.callback?.onSetupFinished(isSuccess)
            closeIfIdle()
        }
    }

    fun endBillingConnection(callback: BillingManagerCallback) {
        // A screen that is already being replaced must not detach the one that replaced it.
        if (this.callback !== callback) return
        this.callback = null
        closeIfIdle()
    }

    /**
     * Called whenever the launcher comes to the foreground. Throttled and gated behind a flag so
     * it only connects to Play Billing when a purchase may still need consuming.
     */
    fun checkUnconsumedPurchases() {
        val now = SystemClock.elapsedRealtime()
        if (lastPurchaseCheckAt != 0L && now - lastPurchaseCheckAt < PURCHASE_CHECK_INTERVAL_MS) return
        lastPurchaseCheckAt = now
        if (!prefs.getBoolean(KEY_NEEDS_PURCHASE_CHECK, true)) return

        withConnection { isSuccess ->
            if (isSuccess) {
                consumeOwnedPurchases()
            }
            closeIfIdle()
        }
    }

    suspend fun queryProducts(): List<AppProduct> {
        val client = billingClient ?: return emptyList()
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
            client.queryProductDetails(params.build())
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
        val client = billingClient ?: return
        val details = product.originalDetails as? ProductDetails ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        prefs.edit { putBoolean(KEY_NEEDS_PURCHASE_CHECK, true) }
        client.launchBillingFlow(activity, billingFlowParams)
    }
}

interface BillingManagerCallback {
    fun onSetupFinished(isSuccess: Boolean)
    fun onBillingServiceDisconnected()
    fun onPremiumPurchaseComplete()
}
