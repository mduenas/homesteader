package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.markduenas.homesteader.domain.monetization.AdManager

/**
 * Standard banner ad height (50dp is standard for mobile banner ads)
 */
val AdBannerHeight = 50.dp

/**
 * Platform-specific banner ad implementation.
 * Android uses AdMob AdView, iOS uses GADBannerView.
 */
@Composable
expect fun PlatformAdBanner(
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: (String) -> Unit,
    modifier: Modifier
)

/**
 * Composable that displays a banner ad.
 * Uses platform-specific implementation (AdMob on Android, Google Mobile Ads on iOS).
 * Respects premium status - no ads shown for premium users.
 *
 * @param adManager The ad manager to check if ads should be shown
 * @param modifier Modifier for the ad banner container
 */
@Composable
fun AdBanner(
    adManager: AdManager,
    modifier: Modifier = Modifier
) {
    val isPremium by adManager.isPremium.collectAsState()

    // Show ads if user is not premium
    if (!isPremium) {
        PlatformAdBanner(
            adUnitId = adManager.getBannerAdUnitId(),
            onAdLoaded = { adManager.onAdLoaded() },
            onAdFailedToLoad = { error -> adManager.onAdFailedToLoad(error) },
            modifier = modifier
        )
    }
}

/**
 * Placeholder ad banner for development/testing when ads are not loaded.
 */
@Composable
fun AdBannerPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AdBannerHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Advertisement",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Composable that provides padding at the bottom for ad banner space.
 * Use this when you need to add space for the ad but not show it directly.
 */
@Composable
fun AdBannerSpacer(
    adManager: AdManager,
    modifier: Modifier = Modifier
) {
    val shouldShowAds by adManager.shouldShowAds.collectAsState(initial = false)

    if (shouldShowAds) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(AdBannerHeight)
        )
    }
}
