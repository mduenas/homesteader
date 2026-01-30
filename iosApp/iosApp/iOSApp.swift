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

        // Initialize Koin
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
