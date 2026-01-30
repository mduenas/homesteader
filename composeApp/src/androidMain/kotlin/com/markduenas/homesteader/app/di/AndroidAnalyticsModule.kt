package com.markduenas.homesteader.app.di

import com.markduenas.homesteader.core.analytics.AnalyticsService
import com.markduenas.homesteader.core.analytics.AppInsights
import com.markduenas.homesteader.core.analytics.AppInsightsProvider
import com.markduenas.homesteader.core.analytics.CrashReporter
import com.markduenas.homesteader.core.analytics.CrashReportingService
import com.markduenas.homesteader.core.analytics.FirebaseAnalyticsService
import com.markduenas.homesteader.core.analytics.FirebaseCrashReportingService
import com.markduenas.homesteader.domain.monetization.BillingService
import com.markduenas.homesteader.domain.monetization.GooglePlayBillingService
import com.markduenas.homesteader.domain.notification.AndroidNotificationService
import com.markduenas.homesteader.domain.notification.NotificationService
import org.koin.dsl.module

/**
 * Android-specific module that provides Firebase and billing implementations.
 */
val androidAnalyticsModule = module {
    // Analytics
    single<AnalyticsService> { FirebaseAnalyticsService() }
    single<CrashReportingService> { FirebaseCrashReportingService() }
    single { AppInsights(get(), get()) }

    // Billing
    single<BillingService> { GooglePlayBillingService(get()) }

    // Notifications
    single<NotificationService> { AndroidNotificationService(get()) }

    single {
        val analytics: AnalyticsService = get()
        val crashReporting: CrashReportingService = get()
        AppInsightsProvider.initialize(analytics, crashReporting)
        CrashReporter.initialize(crashReporting)
        Unit
    }
}
