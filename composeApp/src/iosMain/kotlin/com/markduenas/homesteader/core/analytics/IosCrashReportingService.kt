package com.markduenas.homesteader.core.analytics

/**
 * iOS Crash Reporting service that delegates to platform-provided implementation.
 *
 * The actual Firebase Crashlytics calls are made from Swift code.
 * This class acts as a bridge that can be configured from the iOS app.
 *
 * Usage from Swift:
 * ```swift
 * // In AppDelegate or similar
 * IosCrashReportingServiceCompanion.shared.setDelegate(FirebaseCrashlyticsDelegate())
 * ```
 */
class IosCrashReportingService : CrashReportingService {

    companion object {
        private var delegate: CrashReportingService = DebugCrashReportingService()

        /**
         * Set the platform implementation.
         * Call this from Swift to provide the real Firebase Crashlytics implementation.
         */
        fun setDelegate(crashReportingService: CrashReportingService) {
            delegate = crashReportingService
        }
    }

    override fun recordException(throwable: Throwable) {
        delegate.recordException(throwable)
    }

    override fun recordException(throwable: Throwable, message: String) {
        delegate.recordException(throwable, message)
    }

    override fun log(message: String) {
        delegate.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        delegate.setCustomKey(key, value)
    }

    override fun setUserId(userId: String?) {
        delegate.setUserId(userId)
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        delegate.setCrashlyticsCollectionEnabled(enabled)
    }
}

/**
 * Protocol for Swift to implement for Firebase Crashlytics integration.
 * This allows the iOS app to provide a native Firebase implementation.
 */
interface IosCrashReportingDelegate : CrashReportingService
