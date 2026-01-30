package com.markduenas.homesteader.core.analytics

/**
 * iOS Analytics service that delegates to platform-provided implementation.
 *
 * The actual Firebase Analytics calls are made from Swift code.
 * This class acts as a bridge that can be configured from the iOS app.
 *
 * Usage from Swift:
 * ```swift
 * // In AppDelegate or similar
 * IosAnalyticsServiceCompanion.shared.setDelegate(FirebaseAnalyticsDelegate())
 * ```
 */
class IosAnalyticsService : AnalyticsService {

    companion object {
        private var delegate: AnalyticsService = DebugAnalyticsService()

        /**
         * Set the platform implementation.
         * Call this from Swift to provide the real Firebase Analytics implementation.
         */
        fun setDelegate(analyticsService: AnalyticsService) {
            delegate = analyticsService
        }
    }

    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        delegate.logEvent(eventName, params)
    }

    override fun setUserProperty(name: String, value: String) {
        delegate.setUserProperty(name, value)
    }

    override fun logScreenView(screenName: String) {
        delegate.logScreenView(screenName)
    }
}

/**
 * Protocol for Swift to implement for Firebase Analytics integration.
 * This allows the iOS app to provide a native Firebase implementation.
 */
interface IosAnalyticsDelegate : AnalyticsService
