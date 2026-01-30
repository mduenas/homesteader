import Foundation
import ComposeApp
import FirebaseAnalytics
import FirebaseCrashlytics

/// Firebase Analytics implementation that bridges to the Kotlin AnalyticsService interface.
class FirebaseAnalyticsDelegate: AnalyticsService {

    func logEvent(eventName: String, params: [String: Any]?) {
        if let params = params {
            // Convert Kotlin Map to Swift Dictionary with proper types
            var swiftParams: [String: Any] = [:]
            for (key, value) in params {
                if let stringKey = key as? String {
                    swiftParams[stringKey] = value
                }
            }
            Analytics.logEvent(eventName, parameters: swiftParams)
        } else {
            Analytics.logEvent(eventName, parameters: nil)
        }
    }

    func setUserProperty(name: String, value: String) {
        Analytics.setUserProperty(value, forName: name)
    }

    func logScreenView(screenName: String) {
        Analytics.logEvent(AnalyticsEventScreenView, parameters: [
            AnalyticsParameterScreenName: screenName,
            AnalyticsParameterScreenClass: screenName
        ])
    }

    func logError(errorType: String, message: String) {
        Analytics.logEvent("error_occurred", parameters: [
            "error_type": errorType,
            "error_message": message
        ])
    }
}

/// Firebase Crashlytics implementation that bridges to the Kotlin CrashReportingService interface.
class FirebaseCrashlyticsDelegate: CrashReportingService {

    func recordException(throwable: KotlinThrowable) {
        let error = NSError(
            domain: "com.markduenas.homesteader",
            code: 0,
            userInfo: [
                NSLocalizedDescriptionKey: throwable.message ?? "Unknown error"
            ]
        )
        Crashlytics.crashlytics().record(error: error)
    }

    func recordException(throwable: KotlinThrowable, message: String) {
        Crashlytics.crashlytics().log(message)
        let error = NSError(
            domain: "com.markduenas.homesteader",
            code: 0,
            userInfo: [
                NSLocalizedDescriptionKey: throwable.message ?? message
            ]
        )
        Crashlytics.crashlytics().record(error: error)
    }

    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func setCustomKey(key: String, value: String) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    func setUserId(userId: String?) {
        Crashlytics.crashlytics().setUserID(userId ?? "")
    }

    func setCrashlyticsCollectionEnabled(enabled: Bool) {
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
    }
}
