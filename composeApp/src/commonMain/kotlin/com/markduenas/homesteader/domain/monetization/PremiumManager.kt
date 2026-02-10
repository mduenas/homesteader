package com.markduenas.homesteader.domain.monetization

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages premium subscription state.
 * Delegates to platform-specific BillingService for actual purchases.
 */
class PremiumManager(
    private val billingService: BillingService
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    val isPremium: StateFlow<Boolean> = billingService.isPremiumPurchased

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    val isReady: StateFlow<Boolean> = billingService.isReady

    val productInfo: StateFlow<ProductInfo?> = billingService.productDetails

    /**
     * Initialize billing and check for existing purchases.
     */
    fun initialize() {
        scope.launch {
            billingService.initialize()
            billingService.queryProducts()
        }
    }

    /**
     * Initiate premium purchase.
     */
    suspend fun purchasePremium(): PurchaseResult {
        _purchaseState.value = PurchaseState.Processing

        val result = billingService.purchasePremium()

        _purchaseState.value = PurchaseState.Idle

        return when (result) {
            is BillingResult.Success -> PurchaseResult.Success
            is BillingResult.Cancelled -> PurchaseResult.Cancelled
            is BillingResult.AlreadyOwned -> PurchaseResult.AlreadyOwned
            is BillingResult.NotAvailable -> PurchaseResult.Error("Product not available")
            is BillingResult.NetworkError -> PurchaseResult.Error("Network error. Please try again.")
            is BillingResult.Error -> PurchaseResult.Error(result.message)
        }
    }

    /**
     * Restore previous purchases.
     */
    suspend fun restorePurchases(): RestoreResult {
        _purchaseState.value = PurchaseState.Restoring

        val result = billingService.restorePurchases()

        _purchaseState.value = PurchaseState.Idle

        return when (result) {
            is BillingResult.Success -> RestoreResult.Success(premiumRestored = true)
            is BillingResult.NotAvailable -> RestoreResult.NothingToRestore
            is BillingResult.Error -> RestoreResult.Error(result.message)
            else -> RestoreResult.NothingToRestore
        }
    }

    /**
     * Get premium product info for display.
     */
    fun getPremiumProductInfo(): PremiumProduct {
        val info = productInfo.value
        return PremiumProduct(
            productId = info?.productId ?: PREMIUM_PRODUCT_ID,
            title = info?.title ?: "Remove Ads",
            description = info?.description ?: "Remove all advertisements with a one-time purchase",
            price = info?.formattedPrice ?: "$7.99",
            currencyCode = "USD"
        )
    }

    /**
     * Clean up resources.
     */
    fun cleanup() {
        billingService.cleanup()
    }
}

/**
 * Represents the premium product available for purchase.
 */
data class PremiumProduct(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val currencyCode: String
)

/**
 * Current state of a purchase operation.
 */
sealed class PurchaseState {
    data object Idle : PurchaseState()
    data object Processing : PurchaseState()
    data object Restoring : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

/**
 * Result of a purchase attempt.
 */
sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data object AlreadyOwned : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

/**
 * Result of a restore purchases attempt.
 */
sealed class RestoreResult {
    data class Success(val premiumRestored: Boolean) : RestoreResult()
    data object NothingToRestore : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}
