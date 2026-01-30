package com.markduenas.homesteader

import com.markduenas.homesteader.app.di.coreModules
import com.markduenas.homesteader.app.di.iosAnalyticsModule
import com.markduenas.homesteader.data.database.DatabaseDriverFactory
import com.markduenas.homesteader.domain.monetization.AdManager
import com.markduenas.homesteader.domain.monetization.PremiumManager
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin

fun initKoin() {
    startKoin {
        modules(
            module {
                single { DatabaseDriverFactory() }
            },
            iosAnalyticsModule,
            *coreModules.toTypedArray()
        )
    }

    // Initialize AdManager after Koin is ready
    val adManager: AdManager = getKoin().get()
    adManager.initialize()

    // Initialize PremiumManager (billing)
    val premiumManager: PremiumManager = getKoin().get()
    premiumManager.initialize()
}
