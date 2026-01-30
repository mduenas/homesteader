package com.markduenas.homesteader.domain.monetization

import platform.Foundation.NSProcessInfo

/**
 * iOS implementation - returns the iOS banner ad unit ID.
 * Uses test ads in debug/simulator builds, production ads in release builds.
 */
actual fun getPlatformBannerAdUnitId(): String {
    // Check if running in simulator (development)
    val isSimulator = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    return if (isSimulator) {
        AdManager.AD_UNIT_BANNER_TEST_IOS
    } else {
        AdManager.AD_UNIT_BANNER_IOS
    }
}
