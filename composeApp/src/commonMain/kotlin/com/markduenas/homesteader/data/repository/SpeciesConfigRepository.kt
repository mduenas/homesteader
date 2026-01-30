package com.markduenas.homesteader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.database.SpeciesConfigQueries
import com.markduenas.homesteader.data.database.serializeCustomFieldsSchema
import com.markduenas.homesteader.data.database.toDomain
import com.markduenas.homesteader.domain.model.DefaultSpeciesConfigs
import com.markduenas.homesteader.domain.model.SpeciesConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SpeciesConfigRepository(
    private val queries: SpeciesConfigQueries
) {
    private val dispatcher = Dispatchers.IO

    fun getAllConfigs(): Flow<List<SpeciesConfig>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getEnabledConfigs(): Flow<List<SpeciesConfig>> {
        return queries.selectEnabled()
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getConfigByKey(speciesKey: String): Flow<SpeciesConfig?> {
        return queries.selectByKey(speciesKey)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
    }

    fun getConfigById(id: String): Flow<SpeciesConfig?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
    }

    suspend fun insertConfig(config: SpeciesConfig): String = withContext(dispatcher) {
        val id = config.id.ifBlank { uuid4().toString() }
        val now = DateTimeUtil.nowIsoString()
        val customFieldsJson = serializeCustomFieldsSchema(config.customFieldsSchema)

        queries.insert(
            id = id,
            species_key = config.speciesKey,
            display_name = config.displayName,
            icon_name = config.iconName,
            is_enabled = if (config.isEnabled) 1L else 0L,
            track_breeding = if (config.trackBreeding) 1L else 0L,
            track_pregnancy = if (config.trackPregnancy) 1L else 0L,
            track_milk_production = if (config.trackMilkProduction) 1L else 0L,
            track_egg_production = if (config.trackEggProduction) 1L else 0L,
            track_weight = if (config.trackWeight) 1L else 0L,
            track_feed = if (config.trackFeed) 1L else 0L,
            track_health = if (config.trackHealth) 1L else 0L,
            gestation_days = config.gestationDays?.toLong(),
            heat_cycle_days = config.heatCycleDays?.toLong(),
            weaning_age_days = config.weaningAgeDays?.toLong(),
            custom_fields_schema = customFieldsJson,
            created_at = now,
            updated_at = now
        )

        id
    }

    suspend fun updateConfig(config: SpeciesConfig) = withContext(dispatcher) {
        val now = DateTimeUtil.nowIsoString()
        val customFieldsJson = serializeCustomFieldsSchema(config.customFieldsSchema)

        queries.update(
            display_name = config.displayName,
            icon_name = config.iconName,
            is_enabled = if (config.isEnabled) 1L else 0L,
            track_breeding = if (config.trackBreeding) 1L else 0L,
            track_pregnancy = if (config.trackPregnancy) 1L else 0L,
            track_milk_production = if (config.trackMilkProduction) 1L else 0L,
            track_egg_production = if (config.trackEggProduction) 1L else 0L,
            track_weight = if (config.trackWeight) 1L else 0L,
            track_feed = if (config.trackFeed) 1L else 0L,
            track_health = if (config.trackHealth) 1L else 0L,
            gestation_days = config.gestationDays?.toLong(),
            heat_cycle_days = config.heatCycleDays?.toLong(),
            weaning_age_days = config.weaningAgeDays?.toLong(),
            custom_fields_schema = customFieldsJson,
            updated_at = now,
            id = config.id
        )
    }

    suspend fun toggleEnabled(id: String, enabled: Boolean) = withContext(dispatcher) {
        val now = DateTimeUtil.nowIsoString()
        queries.toggleEnabled(
            is_enabled = if (enabled) 1L else 0L,
            updated_at = now,
            id = id
        )
    }

    suspend fun deleteConfig(id: String) = withContext(dispatcher) {
        queries.delete(id)
    }

    /**
     * Check if configs have been initialized
     */
    suspend fun hasConfigs(): Boolean = withContext(dispatcher) {
        val configs = getAllConfigs().first()
        configs.isNotEmpty()
    }

    /**
     * Initialize default species configs if not already present
     */
    suspend fun initializeDefaultConfigs() = withContext(dispatcher) {
        if (!hasConfigs()) {
            DefaultSpeciesConfigs.ALL_DEFAULTS.forEach { defaultConfig ->
                insertConfig(defaultConfig.copy(isEnabled = false))
            }
        }
    }

    /**
     * Enable selected species during setup wizard
     */
    suspend fun enableSpecies(speciesKeys: List<String>) = withContext(dispatcher) {
        val now = DateTimeUtil.nowIsoString()
        speciesKeys.forEach { key ->
            val config = getConfigByKey(key).first()
            config?.let {
                queries.toggleEnabled(
                    is_enabled = 1L,
                    updated_at = now,
                    id = it.id
                )
            }
        }
    }
}
