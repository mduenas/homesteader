package com.markduenas.homesteader

import android.app.Application
import com.markduenas.homesteader.app.di.androidAnalyticsModule
import com.markduenas.homesteader.app.di.coreModules
import com.markduenas.homesteader.data.database.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class HomesteaderApplication : Application() {

    override fun onCreate() {
        super.onCreate()

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
    }
}
