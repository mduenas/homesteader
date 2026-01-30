package com.markduenas.homesteader.data.database

import com.markduenas.homesteader.domain.model.CustomFieldDefinition
import com.markduenas.homesteader.domain.model.SpeciesConfig
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun SpeciesConfigEntity.toDomain(): SpeciesConfig {
    return SpeciesConfig(
        id = id,
        speciesKey = species_key,
        displayName = display_name,
        iconName = icon_name,
        isEnabled = is_enabled == 1L,
        trackBreeding = track_breeding == 1L,
        trackPregnancy = track_pregnancy == 1L,
        trackMilkProduction = track_milk_production == 1L,
        trackEggProduction = track_egg_production == 1L,
        trackWeight = track_weight == 1L,
        trackFeed = track_feed == 1L,
        trackHealth = track_health == 1L,
        gestationDays = gestation_days?.toInt(),
        heatCycleDays = heat_cycle_days?.toInt(),
        weaningAgeDays = weaning_age_days?.toInt(),
        customFieldsSchema = parseCustomFieldsSchema(custom_fields_schema),
        createdAt = created_at,
        updatedAt = updated_at
    )
}

private fun parseCustomFieldsSchema(jsonString: String?): List<CustomFieldDefinition> {
    if (jsonString.isNullOrBlank()) return emptyList()
    return try {
        json.decodeFromString(ListSerializer(CustomFieldDefinition.serializer()), jsonString)
    } catch (e: Exception) {
        emptyList()
    }
}

fun serializeCustomFieldsSchema(fields: List<CustomFieldDefinition>): String? {
    if (fields.isEmpty()) return null
    return try {
        json.encodeToString(ListSerializer(CustomFieldDefinition.serializer()), fields)
    } catch (e: Exception) {
        null
    }
}
