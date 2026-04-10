import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseAnalytics
import FirebaseCrashlytics
import GoogleMobileAds

@main
struct iOSApp: App {
    init() {
        // Initialize Firebase first
        FirebaseApp.configure()

        // Initialize AdMob
        AdMobInitializer.initialize()

        // Set up Firebase delegates for Kotlin bridge
        IosAnalyticsService.Companion.shared.setDelegate(analyticsService: FirebaseAnalyticsDelegate())
        IosCrashReportingService.Companion.shared.setDelegate(crashReportingService: FirebaseCrashlyticsDelegate())

        // Set up AdMob delegate for Kotlin bridge
        IosAdBannerProvider.shared.setDelegate(delegate: AdMobBannerDelegate())

        // Set up StoreKit delegate for Kotlin bridge
        IosStoreKitProvider.shared.setDelegate(delegate: StoreKitDelegateWrapper())

        // Initialize Koin
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Handle .vcf files shared/opened into the app
                    guard url.pathExtension.lowercased() == "vcf" else { return }
                    if let content = try? String(contentsOf: url, encoding: .utf8), !content.isEmpty {
                        IncomingContactStore.shared.setPending(vcard: content)
                    }
                }
        }
    }
}
