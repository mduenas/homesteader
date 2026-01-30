package com.markduenas.homesteader.app.di

import com.markduenas.homesteader.core.analytics.AnalyticsService
import com.markduenas.homesteader.core.analytics.AppInsights
import com.markduenas.homesteader.core.analytics.AppInsightsProvider
import com.markduenas.homesteader.core.analytics.CrashReporter
import com.markduenas.homesteader.core.analytics.CrashReportingService
import com.markduenas.homesteader.core.analytics.IosAnalyticsService
import com.markduenas.homesteader.core.analytics.IosCrashReportingService
import org.koin.dsl.module

/**
 * iOS-specific analytics module.
 *
 * By default, uses debug implementations that log to console.
 * To enable Firebase, call the following from Swift before starting Koin:
 *
 * ```swift
 * IosAnalyticsServiceCompanion.shared.setDelegate(YourFirebaseAnalyticsDelegate())
 * IosCrashReportingServiceCompanion.shared.setDelegate(YourFirebaseCrashlyticsDelegate())
 * ```
 */
val iosAnalyticsModule = module {
    single<AnalyticsService> { IosAnalyticsService() }
    single<CrashReportingService> { IosCrashReportingService() }
    single { AppInsights(get(), get()) }

    single {
        val analytics: AnalyticsService = get()
        val crashReporting: CrashReportingService = get()
        AppInsightsProvider.initialize(analytics, crashReporting)
        CrashReporter.initialize(crashReporting)
        Unit
    }
}
