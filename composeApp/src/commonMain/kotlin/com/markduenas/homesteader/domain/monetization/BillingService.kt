package com.markduenas.homesteader.domain.monetization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic billing service interface.
 * Implemented by Google Play Billing on Android and StoreKit on iOS.
 */
interface BillingService {
    /**
     * Whether the billing service is ready to make purchases.
     */
    val isReady: StateFlow<Boolean>

    /**
     * Whether the user has purchased premium (remove ads).
     */
    val isPremiumPurchased: StateFlow<Boolean>

    /**
     * Current product details for display.
     */
    val productDetails: StateFlow<ProductInfo?>

    /**
     * Initialize the billing service and connect to the store.
     */
    suspend fun initialize()

    /**
     * Query available products and check for existing purchases.
     */
    suspend fun queryProducts()

    /**
     * Launch the purchase flow for premium.
     * @return Result indicating success, cancellation, or error
     */
    suspend fun purchasePremium(): BillingResult

    /**
     * Restore previous purchases.
     * @return Result indicating what was restored
     */
    suspend fun restorePurchases(): BillingResult

    /**
     * Clean up resources when no longer needed.
     */
    fun cleanup()
}

/**
 * Product information for display in the UI.
 */
data class ProductInfo(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String
)

/**
 * Result of a billing operation.
 */
sealed class BillingResult {
    data object Success : BillingResult()
    data object Cancelled : BillingResult()
    data object AlreadyOwned : BillingResult()
    data object NotAvailable : BillingResult()
    data object NetworkError : BillingResult()
    data class Error(val message: String) : BillingResult()
}

/**
 * Product ID for the premium purchase.
 */
const val PREMIUM_PRODUCT_ID = "homesteader_remove_ads"

/**
 * Free tier animal limit. Users above this count must upgrade to premium to add more.
 */
const val FREE_TIER_ANIMAL_LIMIT = 20
