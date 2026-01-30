import Foundation
import UIKit
import GoogleMobileAds
import ComposeApp

/// AdMob banner implementation that bridges to the Kotlin IosAdBannerDelegate interface.
class AdMobBannerDelegate: IosAdBannerDelegate {

    func createBannerView(
        adUnitId: String,
        onAdLoaded: @escaping () -> Void,
        onAdFailedToLoad: @escaping (String) -> Void
    ) -> UIView {
        let bannerView = GADBannerView(adSize: GADAdSizeBanner)
        bannerView.adUnitID = adUnitId

        // Get the root view controller
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootViewController = windowScene.windows.first?.rootViewController {
            bannerView.rootViewController = rootViewController
        }

        // Set up delegate to handle callbacks
        let delegate = BannerViewDelegate(
            onAdLoaded: onAdLoaded,
            onAdFailedToLoad: onAdFailedToLoad
        )
        bannerView.delegate = delegate

        // Store delegate to prevent deallocation
        objc_setAssociatedObject(
            bannerView,
            "bannerDelegate",
            delegate,
            .OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )

        // Load the ad
        bannerView.load(GADRequest())

        return bannerView
    }
}

/// Delegate class to handle GADBannerView callbacks
private class BannerViewDelegate: NSObject, GADBannerViewDelegate {
    let onAdLoaded: () -> Void
    let onAdFailedToLoad: (String) -> Void

    init(onAdLoaded: @escaping () -> Void, onAdFailedToLoad: @escaping (String) -> Void) {
        self.onAdLoaded = onAdLoaded
        self.onAdFailedToLoad = onAdFailedToLoad
    }

    func bannerViewDidReceiveAd(_ bannerView: GADBannerView) {
        onAdLoaded()
    }

    func bannerView(_ bannerView: GADBannerView, didFailToReceiveAdWithError error: Error) {
        onAdFailedToLoad(error.localizedDescription)
    }
}

/// Helper to initialize AdMob SDK
class AdMobInitializer {
    static func initialize() {
        GADMobileAds.sharedInstance().start(completionHandler: nil)
    }
}
