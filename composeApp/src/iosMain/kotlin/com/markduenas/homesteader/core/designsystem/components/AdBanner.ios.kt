package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

/**
 * iOS implementation of PlatformAdBanner.
 * Uses a bridge to Swift for GADBannerView since Google Mobile Ads SDK
 * requires Swift/ObjC interop.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformAdBanner(
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: (String) -> Unit,
    modifier: Modifier
) {
    val adBannerProvider = remember { IosAdBannerProvider.shared }

    UIKitView(
        modifier = modifier
            .fillMaxWidth()
            .height(AdBannerHeight),
        factory = {
            adBannerProvider?.createBannerView(
                adUnitId = adUnitId,
                onAdLoaded = onAdLoaded,
                onAdFailedToLoad = onAdFailedToLoad
            ) ?: UIView()
        },
        update = { _ ->
            // Banner view is managed by the provider
        }
    )
}

/**
 * Interface for iOS ad banner provider.
 * Implementation is set from Swift side.
 */
interface IosAdBannerDelegate {
    fun createBannerView(
        adUnitId: String,
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: (String) -> Unit
    ): UIView
}

/**
 * Singleton to hold the iOS ad banner provider.
 * Set from Swift during app initialization.
 */
object IosAdBannerProvider {
    var shared: IosAdBannerDelegate? = null

    fun setDelegate(delegate: IosAdBannerDelegate) {
        shared = delegate
    }
}
