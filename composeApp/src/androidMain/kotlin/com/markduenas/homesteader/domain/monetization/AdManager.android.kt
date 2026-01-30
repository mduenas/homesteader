package com.markduenas.homesteader.domain.monetization

/**
 * Android implementation - returns the Android banner ad unit ID.
 */
actual fun getPlatformBannerAdUnitId(): String = AdManager.AD_UNIT_BANNER_ANDROID
