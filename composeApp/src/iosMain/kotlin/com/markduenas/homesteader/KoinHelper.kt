package com.markduenas.homesteader

import com.markduenas.homesteader.app.di.coreModules
import com.markduenas.homesteader.app.di.iosAnalyticsModule
import com.markduenas.homesteader.data.database.DatabaseDriverFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module

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
}
