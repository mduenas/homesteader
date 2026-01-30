package com.markduenas.homesteader.core.analytics

import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.Species

/**
 * Unified app insights manager combining analytics and crash reporting.
 * Provides convenient methods for common tracking scenarios.
 */
class AppInsights(
    private val analytics: AnalyticsService,
    private val crashReporting: CrashReportingService
) {
    // Screen tracking
    fun trackScreenView(screenName: String) {
        analytics.logScreenView(screenName)
        crashReporting.setCustomKey(CrashContextKeys.SCREEN_NAME, screenName)
    }

    // Animal tracking
    fun trackAnimalCreated(species: Species) {
        analytics.logEvent(
            AnalyticsEvents.ANIMAL_CREATED,
            mapOf(AnalyticsParams.SPECIES to species.key)
        )
    }

    fun trackAnimalUpdated(species: Species) {
        analytics.logEvent(
            AnalyticsEvents.ANIMAL_UPDATED,
            mapOf(AnalyticsParams.SPECIES to species.key)
        )
    }

    fun trackAnimalDeleted(species: Species) {
        analytics.logEvent(
            AnalyticsEvents.ANIMAL_DELETED,
            mapOf(AnalyticsParams.SPECIES to species.key)
        )
    }

    fun trackAnimalStatusChanged(species: Species, newStatus: AnimalStatus) {
        analytics.logEvent(
            AnalyticsEvents.ANIMAL_STATUS_CHANGED,
            mapOf(
                AnalyticsParams.SPECIES to species.key,
                "new_status" to newStatus.name
            )
        )
    }

    // Event tracking
    fun trackEventRecorded(eventType: EventType, species: Species) {
        analytics.logEvent(
            AnalyticsEvents.EVENT_RECORDED,
            mapOf(
                AnalyticsParams.EVENT_TYPE to eventType.name,
                AnalyticsParams.SPECIES to species.key
            )
        )
    }

    fun trackReminderCreated(type: String) {
        analytics.logEvent(
            AnalyticsEvents.REMINDER_CREATED,
            mapOf(AnalyticsParams.EVENT_TYPE to type)
        )
    }

    fun trackReminderCompleted(type: String) {
        analytics.logEvent(
            AnalyticsEvents.REMINDER_COMPLETED,
            mapOf(AnalyticsParams.EVENT_TYPE to type)
        )
    }

    // Feature usage
    fun trackSearch(queryLength: Int) {
        analytics.logEvent(
            AnalyticsEvents.SEARCH_PERFORMED,
            mapOf(AnalyticsParams.SEARCH_QUERY_LENGTH to queryLength)
        )
    }

    fun trackFilterApplied(filterType: String) {
        analytics.logEvent(
            AnalyticsEvents.FILTER_APPLIED,
            mapOf(AnalyticsParams.FILTER_TYPE to filterType)
        )
    }

    fun trackReportGenerated(reportType: String) {
        analytics.logEvent(
            AnalyticsEvents.REPORT_GENERATED,
            mapOf(AnalyticsParams.REPORT_TYPE to reportType)
        )
    }

    fun trackReportExported(reportType: String, format: String) {
        analytics.logEvent(
            AnalyticsEvents.REPORT_EXPORTED,
            mapOf(
                AnalyticsParams.REPORT_TYPE to reportType,
                AnalyticsParams.EXPORT_FORMAT to format
            )
        )
    }

    fun trackBackupCreated(success: Boolean) {
        analytics.logEvent(
            AnalyticsEvents.BACKUP_CREATED,
            mapOf(AnalyticsParams.SUCCESS to success)
        )
    }

    fun trackBackupRestored(success: Boolean) {
        analytics.logEvent(
            AnalyticsEvents.BACKUP_RESTORED,
            mapOf(AnalyticsParams.SUCCESS to success)
        )
    }

    fun trackDataImported(animalCount: Int, success: Boolean) {
        analytics.logEvent(
            AnalyticsEvents.DATA_IMPORTED,
            mapOf(
                AnalyticsParams.ANIMAL_COUNT to animalCount,
                AnalyticsParams.SUCCESS to success
            )
        )
    }

    // Premium/Monetization
    fun trackPremiumScreenViewed() {
        analytics.logEvent(AnalyticsEvents.PREMIUM_SCREEN_VIEWED)
    }

    fun trackPurchaseInitiated(productId: String) {
        analytics.logEvent(
            AnalyticsEvents.PURCHASE_INITIATED,
            mapOf(AnalyticsParams.PRODUCT_ID to productId)
        )
    }

    fun trackPurchaseCompleted(productId: String, price: Double) {
        analytics.logEvent(
            AnalyticsEvents.PURCHASE_COMPLETED,
            mapOf(
                AnalyticsParams.PRODUCT_ID to productId,
                AnalyticsParams.PRICE to price
            )
        )
    }

    fun trackPurchaseFailed(productId: String, errorMessage: String) {
        analytics.logEvent(
            AnalyticsEvents.PURCHASE_FAILED,
            mapOf(
                AnalyticsParams.PRODUCT_ID to productId,
                AnalyticsParams.ERROR_MESSAGE to errorMessage
            )
        )
    }

    fun trackPurchaseRestored(success: Boolean) {
        analytics.logEvent(
            AnalyticsEvents.PURCHASE_RESTORED,
            mapOf(AnalyticsParams.SUCCESS to success)
        )
    }

    fun trackAdDisplayed() {
        analytics.logEvent(AnalyticsEvents.AD_DISPLAYED)
    }

    fun trackAdClicked() {
        analytics.logEvent(AnalyticsEvents.AD_CLICKED)
    }

    // User properties
    fun updateUserProperties(
        isPremium: Boolean,
        totalAnimals: Int,
        speciesCount: Int
    ) {
        analytics.setUserProperty(UserProperties.PREMIUM_STATUS, if (isPremium) "premium" else "free")
        analytics.setUserProperty(UserProperties.TOTAL_ANIMALS, totalAnimals.toString())
        analytics.setUserProperty(UserProperties.SPECIES_COUNT, speciesCount.toString())

        crashReporting.setCustomKey(CrashContextKeys.IS_PREMIUM, isPremium.toString())
        crashReporting.setCustomKey(CrashContextKeys.TOTAL_ANIMALS, totalAnimals.toString())
    }

    // Error tracking
    fun trackError(errorType: String, message: String, throwable: Throwable? = null) {
        analytics.logError(errorType, message)
        throwable?.let { crashReporting.recordException(it, message) }
    }

    fun recordException(throwable: Throwable, context: String? = null) {
        if (context != null) {
            crashReporting.recordException(throwable, context)
        } else {
            crashReporting.recordException(throwable)
        }
    }

    // Breadcrumbs for crash context
    fun log(message: String) {
        crashReporting.log(message)
    }

    fun setContext(key: String, value: String) {
        crashReporting.setCustomKey(key, value)
    }
}

/**
 * Singleton accessor for AppInsights.
 * Initialize in Application class with platform-specific implementations.
 */
object AppInsightsProvider {
    private var _instance: AppInsights? = null

    val instance: AppInsights
        get() = _instance ?: AppInsights(
            NoOpAnalyticsService(),
            NoOpCrashReportingService()
        )

    fun initialize(analytics: AnalyticsService, crashReporting: CrashReportingService) {
        _instance = AppInsights(analytics, crashReporting)
    }

    fun initializeDebug() {
        _instance = AppInsights(
            DebugAnalyticsService(),
            DebugCrashReportingService()
        )
    }
}
