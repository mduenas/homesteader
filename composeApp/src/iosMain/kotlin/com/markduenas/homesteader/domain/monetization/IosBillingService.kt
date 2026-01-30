package com.markduenas.homesteader.domain.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of BillingService using StoreKit via Swift bridge.
 */
class IosBillingService : BillingService {

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isPremiumPurchased = MutableStateFlow(false)
    override val isPremiumPurchased: StateFlow<Boolean> = _isPremiumPurchased.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductInfo?>(null)
    override val productDetails: StateFlow<ProductInfo?> = _productDetails.asStateFlow()

    override suspend fun initialize() {
        // Check for cached purchase state
        val isPurchased = NSUserDefaults.standardUserDefaults.boolForKey(PREMIUM_PURCHASED_KEY)
        _isPremiumPurchased.value = isPurchased

        // Initialize StoreKit via delegate
        IosStoreKitProvider.shared?.initialize(
            onReady = { _isReady.value = true },
            onProductLoaded = { id, title, description, price ->
                _productDetails.value = ProductInfo(
                    productId = id,
                    title = title,
                    description = description,
                    formattedPrice = price
                )
            },
            onPurchaseCompleted = {
                _isPremiumPurchased.value = true
                NSUserDefaults.standardUserDefaults.setBool(true, PREMIUM_PURCHASED_KEY)
            }
        )
    }

    override suspend fun queryProducts() {
        IosStoreKitProvider.shared?.queryProducts(PREMIUM_PRODUCT_ID)
    }

    override suspend fun purchasePremium(): BillingResult {
        val delegate = IosStoreKitProvider.shared
            ?: return BillingResult.NotAvailable

        return delegate.purchase(PREMIUM_PRODUCT_ID)
    }

    override suspend fun restorePurchases(): BillingResult {
        val delegate = IosStoreKitProvider.shared
            ?: return BillingResult.NotAvailable

        val result = delegate.restorePurchases()
        if (result == BillingResult.Success) {
            _isPremiumPurchased.value = true
            NSUserDefaults.standardUserDefaults.setBool(true, PREMIUM_PURCHASED_KEY)
        }
        return result
    }

    override fun cleanup() {
        // No cleanup needed for StoreKit
    }

    companion object {
        private const val PREMIUM_PURCHASED_KEY = "homesteader_premium_purchased"
    }
}

/**
 * Interface for iOS StoreKit provider.
 * Implementation is set from Swift side.
 */
interface IosStoreKitDelegate {
    fun initialize(
        onReady: () -> Unit,
        onProductLoaded: (id: String, title: String, description: String, price: String) -> Unit,
        onPurchaseCompleted: () -> Unit
    )

    fun queryProducts(productId: String)
    fun purchase(productId: String): BillingResult
    fun restorePurchases(): BillingResult
}

/**
 * Singleton to hold the iOS StoreKit provider.
 * Set from Swift during app initialization.
 */
object IosStoreKitProvider {
    var shared: IosStoreKitDelegate? = null

    fun setDelegate(delegate: IosStoreKitDelegate) {
        shared = delegate
    }
}
