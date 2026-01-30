import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseAnalytics
import FirebaseCrashlytics

@main
struct iOSApp: App {
    init() {
        // Initialize Firebase first
        FirebaseApp.configure()

        // Set up Firebase delegates for Kotlin bridge
        IosAnalyticsService.Companion.shared.setDelegate(analyticsService: FirebaseAnalyticsDelegate())
        IosCrashReportingService.Companion.shared.setDelegate(crashReportingService: FirebaseCrashlyticsDelegate())

        // Initialize Koin
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
