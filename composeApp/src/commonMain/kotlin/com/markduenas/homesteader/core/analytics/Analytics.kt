package com.markduenas.homesteader.core.analytics

/**
 * Analytics event names used throughout the app.
 */
object AnalyticsEvents {
    // Screen views
    const val SCREEN_VIEW = "screen_view"
    const val SCREEN_DASHBOARD = "dashboard"
    const val SCREEN_ANIMAL_LIST = "animal_list"
    const val SCREEN_ANIMAL_DETAIL = "animal_detail"
    const val SCREEN_ANIMAL_EDIT = "animal_edit"
    const val SCREEN_EVENT_ADD = "event_add"
    const val SCREEN_CALENDAR = "calendar"
    const val SCREEN_REPORTS = "reports"
    const val SCREEN_SETTINGS = "settings"
    const val SCREEN_BACKUP = "backup"
    const val SCREEN_PREMIUM = "premium"

    // Animal events
    const val ANIMAL_CREATED = "animal_created"
    const val ANIMAL_UPDATED = "animal_updated"
    const val ANIMAL_DELETED = "animal_deleted"
    const val ANIMAL_STATUS_CHANGED = "animal_status_changed"

    // Event tracking
    const val EVENT_RECORDED = "event_recorded"
    const val REMINDER_CREATED = "reminder_created"
    const val REMINDER_COMPLETED = "reminder_completed"

    // Feature usage
    const val SEARCH_PERFORMED = "search_performed"
    const val FILTER_APPLIED = "filter_applied"
    const val REPORT_GENERATED = "report_generated"
    const val REPORT_EXPORTED = "report_exported"
    const val BACKUP_CREATED = "backup_created"
    const val BACKUP_RESTORED = "backup_restored"
    const val DATA_IMPORTED = "data_imported"

    // Premium/Monetization
    const val PREMIUM_SCREEN_VIEWED = "premium_screen_viewed"
    const val PURCHASE_INITIATED = "purchase_initiated"
    const val PURCHASE_COMPLETED = "purchase_completed"
    const val PURCHASE_FAILED = "purchase_failed"
    const val PURCHASE_RESTORED = "purchase_restored"
    const val AD_DISPLAYED = "ad_displayed"
    const val AD_CLICKED = "ad_clicked"

    // Errors
    const val ERROR_OCCURRED = "error_occurred"
}

/**
 * Analytics parameter names.
 */
object AnalyticsParams {
    const val SCREEN_NAME = "screen_name"
    const val SPECIES = "species"
    const val EVENT_TYPE = "event_type"
    const val ANIMAL_COUNT = "animal_count"
    const val REPORT_TYPE = "report_type"
    const val EXPORT_FORMAT = "export_format"
    const val SEARCH_QUERY_LENGTH = "search_query_length"
    const val FILTER_TYPE = "filter_type"
    const val ERROR_MESSAGE = "error_message"
    const val ERROR_TYPE = "error_type"
    const val PRODUCT_ID = "product_id"
    const val PRICE = "price"
    const val SUCCESS = "success"
}

/**
 * User properties for analytics segmentation.
 */
object UserProperties {
    const val PREMIUM_STATUS = "premium_status"
    const val TOTAL_ANIMALS = "total_animals"
    const val SPECIES_COUNT = "species_count"
    const val APP_VERSION = "app_version"
    const val PLATFORM = "platform"
}

/**
 * Analytics service interface for tracking events and user properties.
 * Platform-specific implementations will use Firebase Analytics or similar.
 */
interface AnalyticsService {
    /**
     * Log an analytics event with optional parameters.
     */
    fun logEvent(eventName: String, params: Map<String, Any>? = null)

    /**
     * Set a user property for segmentation.
     */
    fun setUserProperty(name: String, value: String)

    /**
     * Log a screen view event.
     */
    fun logScreenView(screenName: String) {
        logEvent(AnalyticsEvents.SCREEN_VIEW, mapOf(AnalyticsParams.SCREEN_NAME to screenName))
    }

    /**
     * Log an error event.
     */
    fun logError(errorType: String, message: String) {
        logEvent(
            AnalyticsEvents.ERROR_OCCURRED,
            mapOf(
                AnalyticsParams.ERROR_TYPE to errorType,
                AnalyticsParams.ERROR_MESSAGE to message
            )
        )
    }
}

/**
 * Default no-op implementation for development/testing.
 */
class NoOpAnalyticsService : AnalyticsService {
    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        // No-op - events are not tracked
    }

    override fun setUserProperty(name: String, value: String) {
        // No-op - properties are not set
    }
}

/**
 * Debug implementation that logs events to console.
 */
class DebugAnalyticsService : AnalyticsService {
    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        val paramsStr = params?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
        println("[Analytics] Event: $eventName ${if (paramsStr.isNotEmpty()) "($paramsStr)" else ""}")
    }

    override fun setUserProperty(name: String, value: String) {
        println("[Analytics] UserProperty: $name = $value")
    }
}
