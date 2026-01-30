package com.markduenas.homesteader.core.analytics

/**
 * Crash reporting service interface.
 * Platform-specific implementations will use Firebase Crashlytics or similar.
 */
interface CrashReportingService {
    /**
     * Record a non-fatal exception for crash reporting.
     */
    fun recordException(throwable: Throwable)

    /**
     * Record a non-fatal exception with a custom message.
     */
    fun recordException(throwable: Throwable, message: String)

    /**
     * Log a message for crash context (breadcrumb).
     */
    fun log(message: String)

    /**
     * Set a custom key-value pair for crash context.
     */
    fun setCustomKey(key: String, value: String)

    /**
     * Set the user identifier for crash reports.
     */
    fun setUserId(userId: String?)

    /**
     * Enable or disable crash collection.
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
}

/**
 * Common crash context keys.
 */
object CrashContextKeys {
    const val SCREEN_NAME = "current_screen"
    const val ANIMAL_ID = "animal_id"
    const val EVENT_TYPE = "event_type"
    const val OPERATION = "operation"
    const val IS_PREMIUM = "is_premium"
    const val TOTAL_ANIMALS = "total_animals"
    const val APP_STATE = "app_state"
}

/**
 * Default no-op implementation for development/testing.
 */
class NoOpCrashReportingService : CrashReportingService {
    override fun recordException(throwable: Throwable) {
        // No-op
    }

    override fun recordException(throwable: Throwable, message: String) {
        // No-op
    }

    override fun log(message: String) {
        // No-op
    }

    override fun setCustomKey(key: String, value: String) {
        // No-op
    }

    override fun setUserId(userId: String?) {
        // No-op
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        // No-op
    }
}

/**
 * Debug implementation that logs crashes to console.
 */
class DebugCrashReportingService : CrashReportingService {
    private val customKeys = mutableMapOf<String, String>()
    private var userId: String? = null

    override fun recordException(throwable: Throwable) {
        println("[CrashReporting] Exception: ${throwable.message}")
        throwable.printStackTrace()
        printContext()
    }

    override fun recordException(throwable: Throwable, message: String) {
        println("[CrashReporting] Exception ($message): ${throwable.message}")
        throwable.printStackTrace()
        printContext()
    }

    override fun log(message: String) {
        println("[CrashReporting] Log: $message")
    }

    override fun setCustomKey(key: String, value: String) {
        customKeys[key] = value
        println("[CrashReporting] CustomKey: $key = $value")
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
        println("[CrashReporting] UserId: $userId")
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        println("[CrashReporting] Collection enabled: $enabled")
    }

    private fun printContext() {
        if (customKeys.isNotEmpty()) {
            println("[CrashReporting] Context: $customKeys")
        }
        userId?.let { println("[CrashReporting] UserId: $it") }
    }
}

/**
 * Helper object for safe exception recording throughout the app.
 */
object CrashReporter {
    @PublishedApi
    internal var service: CrashReportingService = NoOpCrashReportingService()

    fun initialize(crashReportingService: CrashReportingService) {
        service = crashReportingService
    }

    fun recordException(throwable: Throwable) {
        service.recordException(throwable)
    }

    fun recordException(throwable: Throwable, message: String) {
        service.recordException(throwable, message)
    }

    fun log(message: String) {
        service.log(message)
    }

    fun setCustomKey(key: String, value: String) {
        service.setCustomKey(key, value)
    }

    fun setScreen(screenName: String) {
        service.setCustomKey(CrashContextKeys.SCREEN_NAME, screenName)
    }

    /**
     * Execute a block safely, recording any exceptions.
     */
    inline fun <T> runCatching(
        operation: String,
        block: () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            service.setCustomKey(CrashContextKeys.OPERATION, operation)
            service.recordException(e)
            Result.failure(e)
        }
    }
}
