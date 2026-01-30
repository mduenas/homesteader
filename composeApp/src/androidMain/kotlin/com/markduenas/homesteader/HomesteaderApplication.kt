package com.markduenas.homesteader

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.markduenas.homesteader.app.di.androidAnalyticsModule
import com.markduenas.homesteader.app.di.coreModules
import com.markduenas.homesteader.data.database.DatabaseDriverFactory
import com.markduenas.homesteader.domain.monetization.AdManager
import com.markduenas.homesteader.domain.monetization.PremiumManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class HomesteaderApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob SDK
        MobileAds.initialize(this) {}

        startKoin {
            androidLogger()
            androidContext(this@HomesteaderApplication)
            modules(
                module {
                    single { DatabaseDriverFactory(get()) }
                },
                androidAnalyticsModule,
                *coreModules.toTypedArray()
            )
        }

        // Initialize AdManager after Koin is ready
        val adManager: AdManager by inject()
        adManager.initialize()

        // Initialize PremiumManager (billing)
        val premiumManager: PremiumManager by inject()
        premiumManager.initialize()
    }
}
