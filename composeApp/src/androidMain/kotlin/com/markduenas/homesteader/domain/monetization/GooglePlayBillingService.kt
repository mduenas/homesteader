package com.markduenas.homesteader.domain.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult as PlayBillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of BillingService using Google Play Billing Library.
 */
class GooglePlayBillingService(
    private val context: Context
) : BillingService {

    private var billingClient: BillingClient? = null
    private var cachedProductDetails: ProductDetails? = null
    private var currentActivity: Activity? = null

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isPremiumPurchased = MutableStateFlow(false)
    override val isPremiumPurchased: StateFlow<Boolean> = _isPremiumPurchased.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductInfo?>(null)
    override val productDetails: StateFlow<ProductInfo?> = _productDetails.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    override suspend fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        connectToPlayStore()
    }

    private suspend fun connectToPlayStore() {
        suspendCancellableCoroutine { continuation ->
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: PlayBillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _isReady.value = true
                    }
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _isReady.value = false
                }
            })
        }
    }

    override suspend fun queryProducts() {
        val client = billingClient ?: return
        if (!_isReady.value) {
            connectToPlayStore()
        }

        // Query product details
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = client.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList?.firstOrNull()?.let { details ->
                cachedProductDetails = details
                _productDetails.value = ProductInfo(
                    productId = details.productId,
                    title = details.title,
                    description = details.description,
                    formattedPrice = details.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99"
                )
            }
        }

        // Check for existing purchases
        checkExistingPurchases()
    }

    private suspend fun checkExistingPurchases() {
        val client = billingClient ?: return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            for (purchase in result.purchasesList) {
                if (purchase.products.contains(PREMIUM_PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                ) {
                    _isPremiumPurchased.value = true
                    // Acknowledge if needed
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                    break
                }
            }
        }
    }

    override suspend fun purchasePremium(): BillingResult {
        val client = billingClient ?: return BillingResult.NotAvailable
        val details = cachedProductDetails ?: run {
            queryProducts()
            cachedProductDetails ?: return BillingResult.NotAvailable
        }

        val activity = currentActivity ?: return BillingResult.Error("No activity available")

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = client.launchBillingFlow(activity, flowParams)

        return when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> BillingResult.Success
            BillingClient.BillingResponseCode.USER_CANCELED -> BillingResult.Cancelled
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _isPremiumPurchased.value = true
                BillingResult.AlreadyOwned
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> BillingResult.NetworkError
            else -> BillingResult.Error(result.debugMessage)
        }
    }

    override suspend fun restorePurchases(): BillingResult {
        val client = billingClient ?: return BillingResult.NotAvailable

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = client.queryPurchasesAsync(params)

        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return BillingResult.Error(result.billingResult.debugMessage)
        }

        var foundPremium = false
        for (purchase in result.purchasesList) {
            if (purchase.products.contains(PREMIUM_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            ) {
                foundPremium = true
                _isPremiumPurchased.value = true
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
                break
            }
        }

        return if (foundPremium) {
            BillingResult.Success
        } else {
            BillingResult.NotAvailable
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(PREMIUM_PRODUCT_ID) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        ) {
            _isPremiumPurchased.value = true
            // Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                CoroutineScope(Dispatchers.IO).launch {
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        val client = billingClient ?: return

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        client.acknowledgePurchase(params)
    }

    override fun cleanup() {
        billingClient?.endConnection()
        billingClient = null
        currentActivity = null
    }

    /**
     * Set the current activity for launching purchase flows.
     * Should be called from Activity.onResume().
     */
    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }
}
