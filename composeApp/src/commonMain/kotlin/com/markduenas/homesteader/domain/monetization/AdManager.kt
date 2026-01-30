package com.markduenas.homesteader.domain.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Platform-specific function to get the banner ad unit ID.
 */
expect fun getPlatformBannerAdUnitId(): String


/**
 * Manages advertisement display logic.
 * Coordinates with PremiumManager to determine when to show ads.
 */
class AdManager(
    private val premiumManager: PremiumManager
) {
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _adError = MutableStateFlow<String?>(null)
    val adError: StateFlow<String?> = _adError.asStateFlow()

    /**
     * Whether user has premium status (no ads).
     */
    val isPremium: StateFlow<Boolean> = premiumManager.isPremium

    /**
     * Whether ads should be shown.
     * Returns false if user has premium status.
     */
    val shouldShowAds = combine(
        premiumManager.isPremium,
        _isAdLoaded
    ) { isPremium, isLoaded ->
        !isPremium && isLoaded
    }

    /**
     * Initialize ad SDK.
     * In production, this would initialize AdMob.
     */
    fun initialize() {
        // TODO: Initialize AdMob SDK
        // Android: MobileAds.initialize(context)
        // iOS: GADMobileAds.sharedInstance().start()

        // For now, simulate ad being loaded
        _isAdLoaded.value = true
    }

    /**
     * Load a banner ad.
     * In production, this would request an ad from AdMob.
     */
    fun loadBannerAd() {
        // TODO: Load actual banner ad
        // This is called by platform-specific ad composables
        _isAdLoaded.value = true
    }

    /**
     * Report that an ad failed to load.
     */
    fun onAdFailedToLoad(error: String) {
        _adError.value = error
        _isAdLoaded.value = false
    }

    /**
     * Report that an ad was loaded successfully.
     */
    fun onAdLoaded() {
        _adError.value = null
        _isAdLoaded.value = true
    }

    /**
     * Get the banner ad unit ID for the current platform.
     */
    fun getBannerAdUnitId(): String = getPlatformBannerAdUnitId()

    companion object {
        // Test ad unit IDs from Google (use for development)
        const val AD_UNIT_BANNER_TEST_ANDROID = "ca-app-pub-3940256099942544/6300978111"
        const val AD_UNIT_BANNER_TEST_IOS = "ca-app-pub-3940256099942544/2934735716"

        // Production ad unit IDs
        const val AD_UNIT_BANNER_ANDROID = "ca-app-pub-7540731406850248/7070657722"
        const val AD_UNIT_BANNER_IOS = "ca-app-pub-7540731406850248/4345595499"
    }
}
