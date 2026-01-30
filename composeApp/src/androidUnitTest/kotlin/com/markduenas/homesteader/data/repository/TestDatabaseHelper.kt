package com.markduenas.homesteader.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.markduenas.homesteader.data.database.HomesteaderDatabase

/**
 * Test helper for creating in-memory databases for integration tests.
 */
object TestDatabaseHelper {

    /**
     * Creates an in-memory SQLite database for testing.
     * Each call creates a fresh database instance.
     */
    fun createInMemoryDatabase(): HomesteaderDatabase {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HomesteaderDatabase.Schema.create(driver)
        return HomesteaderDatabase(driver)
    }

    /**
     * Creates a driver for an in-memory SQLite database.
     * Useful when you need direct driver access.
     */
    fun createInMemoryDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HomesteaderDatabase.Schema.create(driver)
        return driver
    }
}
