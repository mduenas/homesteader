package com.markduenas.homesteader.core.analytics

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * Firebase Crashlytics implementation of CrashReportingService for Android.
 */
class FirebaseCrashReportingService : CrashReportingService {

    private val crashlytics by lazy {
        Firebase.crashlytics
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun recordException(throwable: Throwable, message: String) {
        crashlytics.log(message)
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setUserId(userId: String?) {
        crashlytics.setUserId(userId ?: "")
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
}
