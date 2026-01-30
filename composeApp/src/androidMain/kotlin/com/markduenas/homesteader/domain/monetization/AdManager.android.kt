package com.markduenas.homesteader.domain.monetization

import com.markduenas.homesteader.BuildConfig

/**
 * Android implementation - returns the Android banner ad unit ID.
 * Uses test ads in debug builds, production ads in release builds.
 */
actual fun getPlatformBannerAdUnitId(): String =
    if (BuildConfig.DEBUG) {
        AdManager.AD_UNIT_BANNER_TEST_ANDROID
    } else {
        AdManager.AD_UNIT_BANNER_ANDROID
    }
