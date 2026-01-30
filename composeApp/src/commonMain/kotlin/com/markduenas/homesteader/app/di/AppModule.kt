package com.markduenas.homesteader.app.di

import com.markduenas.homesteader.core.analytics.AnalyticsService
import com.markduenas.homesteader.core.analytics.AppInsights
import com.markduenas.homesteader.core.analytics.AppInsightsProvider
import com.markduenas.homesteader.core.analytics.CrashReporter
import com.markduenas.homesteader.core.analytics.CrashReportingService
import com.markduenas.homesteader.core.analytics.DebugAnalyticsService
import com.markduenas.homesteader.core.analytics.DebugCrashReportingService
import com.markduenas.homesteader.data.database.DatabaseDriverFactory
import com.markduenas.homesteader.data.database.HomesteaderDatabase
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.data.repository.ReminderRepository
import com.markduenas.homesteader.data.repository.SpeciesConfigRepository
import com.markduenas.homesteader.domain.monetization.AdManager
import com.markduenas.homesteader.domain.monetization.PremiumManager
import com.markduenas.homesteader.domain.service.BackupService
import com.markduenas.homesteader.domain.service.CsvImportService
import com.markduenas.homesteader.domain.service.ReminderService
import com.markduenas.homesteader.domain.service.ReportGenerator
import com.markduenas.homesteader.feature.animal.detail.AnimalDetailViewModel
import com.markduenas.homesteader.feature.backup.BackupViewModel
import com.markduenas.homesteader.feature.animal.edit.AnimalEditViewModel
import com.markduenas.homesteader.feature.animal.list.AnimalListViewModel
import com.markduenas.homesteader.feature.calendar.CalendarViewModel
import com.markduenas.homesteader.feature.dashboard.DashboardViewModel
import com.markduenas.homesteader.feature.event.EventAddViewModel
import com.markduenas.homesteader.feature.import.ImportViewModel
import com.markduenas.homesteader.feature.premium.PremiumViewModel
import com.markduenas.homesteader.feature.reports.ReportsViewModel
import com.markduenas.homesteader.feature.setup.SpeciesSetupViewModel
import org.koin.dsl.module

val analyticsModule = module {
    // Default debug implementations - platform modules can override with Firebase
    single<AnalyticsService> { DebugAnalyticsService() }
    single<CrashReportingService> { DebugCrashReportingService() }
    single { AppInsights(get(), get()) }

    // Initialize singletons
    single {
        val analytics: AnalyticsService = get()
        val crashReporting: CrashReportingService = get()
        AppInsightsProvider.initialize(analytics, crashReporting)
        CrashReporter.initialize(crashReporting)
        Unit
    }
}

val databaseModule = module {
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        HomesteaderDatabase(driver)
    }

    single { get<HomesteaderDatabase>().animalQueries }
    single { get<HomesteaderDatabase>().speciesConfigQueries }
    single { get<HomesteaderDatabase>().animalEventQueries }
    single { get<HomesteaderDatabase>().reminderQueries }
}

val repositoryModule = module {
    single { AnimalRepository(get()) }
    single { EventRepository(get()) }
    single { ReminderRepository(get()) }
    single { SpeciesConfigRepository(get()) }
}

val serviceModule = module {
    single { ReminderService(get(), get()) }
    single { ReportGenerator(get(), get()) }
    single { BackupService(get(), get(), get(), get()) }
    single { CsvImportService(get()) }
    // PremiumManager requires BillingService which is provided by platform modules
    single { PremiumManager(get()) }
    single { AdManager(get()) }
}

val viewModelModule = module {
    factory { AnimalListViewModel(get()) }
    factory { (animalId: String) -> AnimalDetailViewModel(animalId, get(), get()) }
    factory { (animalId: String?) -> AnimalEditViewModel(animalId, get()) }
    factory { (animalId: String, animalName: String) -> EventAddViewModel(animalId, animalName, get(), get(), get()) }
    factory { SpeciesSetupViewModel(get()) }
    factory { DashboardViewModel(get(), get(), get()) }
    factory { CalendarViewModel(get(), get(), get()) }
    factory { ReportsViewModel(get()) }
    factory { BackupViewModel(get()) }
    factory { ImportViewModel(get()) }
    factory { PremiumViewModel(get()) }
}

/**
 * Core app modules without analytics.
 * Analytics is provided separately by platform-specific modules.
 */
val coreModules = listOf(
    databaseModule,
    repositoryModule,
    serviceModule,
    viewModelModule
)

/**
 * All app modules including debug analytics.
 * Use this for testing or when platform-specific analytics is not available.
 */
val appModules = listOf(
    analyticsModule,
    databaseModule,
    repositoryModule,
    serviceModule,
    viewModelModule
)
