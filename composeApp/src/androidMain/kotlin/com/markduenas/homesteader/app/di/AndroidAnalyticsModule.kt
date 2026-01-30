package com.markduenas.homesteader.app.di

import com.markduenas.homesteader.core.analytics.AnalyticsService
import com.markduenas.homesteader.core.analytics.AppInsights
import com.markduenas.homesteader.core.analytics.AppInsightsProvider
import com.markduenas.homesteader.core.analytics.CrashReporter
import com.markduenas.homesteader.core.analytics.CrashReportingService
import com.markduenas.homesteader.core.analytics.FirebaseAnalyticsService
import com.markduenas.homesteader.core.analytics.FirebaseCrashReportingService
import org.koin.dsl.module

/**
 * Android-specific analytics module that provides Firebase implementations.
 * This module overrides the default debug implementations from commonMain.
 */
val androidAnalyticsModule = module {
    single<AnalyticsService> { FirebaseAnalyticsService() }
    single<CrashReportingService> { FirebaseCrashReportingService() }
    single { AppInsights(get(), get()) }

    single {
        val analytics: AnalyticsService = get()
        val crashReporting: CrashReportingService = get()
        AppInsightsProvider.initialize(analytics, crashReporting)
        CrashReporter.initialize(crashReporting)
        Unit
    }
}
