package com.markduenas.homesteader.app.di

import com.markduenas.homesteader.core.analytics.AnalyticsService
import com.markduenas.homesteader.core.analytics.AppInsights
import com.markduenas.homesteader.core.analytics.AppInsightsProvider
import com.markduenas.homesteader.core.analytics.CrashReporter
import com.markduenas.homesteader.core.analytics.CrashReportingService
import com.markduenas.homesteader.core.analytics.IosAnalyticsService
import com.markduenas.homesteader.core.analytics.IosCrashReportingService
import com.markduenas.homesteader.domain.monetization.BillingService
import com.markduenas.homesteader.domain.monetization.IosBillingService
import com.markduenas.homesteader.domain.notification.IosNotificationService
import com.markduenas.homesteader.domain.notification.NotificationService
import org.koin.dsl.module

/**
 * iOS-specific module for analytics and billing.
 *
 * Firebase delegates should be set from Swift before starting Koin:
 *
 * ```swift
 * IosAnalyticsService.Companion.shared.setDelegate(analyticsService: FirebaseAnalyticsDelegate())
 * IosCrashReportingService.Companion.shared.setDelegate(crashReportingService: FirebaseCrashlyticsDelegate())
 * IosStoreKitProvider.shared.setDelegate(delegate: StoreKitDelegateWrapper())
 * ```
 */
val iosAnalyticsModule = module {
    // Analytics
    single<AnalyticsService> { IosAnalyticsService() }
    single<CrashReportingService> { IosCrashReportingService() }
    single { AppInsights(get(), get()) }

    // Billing
    single<BillingService> { IosBillingService() }

    // Notifications
    single<NotificationService> { IosNotificationService() }

    single {
        val analytics: AnalyticsService = get()
        val crashReporting: CrashReportingService = get()
        AppInsightsProvider.initialize(analytics, crashReporting)
        CrashReporter.initialize(crashReporting)
        Unit
    }
}
