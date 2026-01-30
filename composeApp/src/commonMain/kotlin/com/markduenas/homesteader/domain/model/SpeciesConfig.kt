package com.markduenas.homesteader.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SpeciesConfig(
    val id: String = "",
    val speciesKey: String,
    val displayName: String,
    val iconName: String? = null,
    val isEnabled: Boolean = true,
    val trackBreeding: Boolean = false,
    val trackPregnancy: Boolean = false,
    val trackMilkProduction: Boolean = false,
    val trackEggProduction: Boolean = false,
    val trackWeight: Boolean = false,
    val trackFeed: Boolean = false,
    val trackHealth: Boolean = true,
    val gestationDays: Int? = null,
    val heatCycleDays: Int? = null,
    val weaningAgeDays: Int? = null,
    val customFieldsSchema: List<CustomFieldDefinition> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class CustomFieldDefinition(
    val key: String,
    val label: String,
    val type: CustomFieldType,
    val required: Boolean = false,
    val options: List<String>? = null,
    val defaultValue: String? = null
)

@Serializable
enum class CustomFieldType {
    TEXT,
    NUMBER,
    DATE,
    SELECT,
    BOOLEAN
}

/**
 * Default species configurations with recommended settings
 */
object DefaultSpeciesConfigs {

    val CATTLE_BEEF = SpeciesConfig(
        speciesKey = "cattle_beef",
        displayName = "Cattle (Beef)",
        trackBreeding = true,
        trackPregnancy = true,
        trackWeight = true,
        trackHealth = true,
        gestationDays = 283,
        heatCycleDays = 21,
        weaningAgeDays = 205
    )

    val CATTLE_DAIRY = SpeciesConfig(
        speciesKey = "cattle_dairy",
        displayName = "Cattle (Dairy)",
        trackBreeding = true,
        trackPregnancy = true,
        trackMilkProduction = true,
        trackWeight = true,
        trackHealth = true,
        gestationDays = 283,
        heatCycleDays = 21,
        weaningAgeDays = 60
    )

    val CHICKENS_LAYERS = SpeciesConfig(
        speciesKey = "chickens_layers",
        displayName = "Chickens (Layers)",
        trackEggProduction = true,
        trackHealth = true
    )

    val CHICKENS_BROILERS = SpeciesConfig(
        speciesKey = "chickens_broilers",
        displayName = "Chickens (Broilers)",
        trackWeight = true,
        trackFeed = true,
        trackHealth = true
    )

    val PIGS = SpeciesConfig(
        speciesKey = "pigs",
        displayName = "Pigs",
        trackBreeding = true,
        trackPregnancy = true,
        trackWeight = true,
        trackHealth = true,
        gestationDays = 114,
        heatCycleDays = 21,
        weaningAgeDays = 28
    )

    val GOATS_MEAT = SpeciesConfig(
        speciesKey = "goats_meat",
        displayName = "Goats (Meat)",
        trackBreeding = true,
        trackPregnancy = true,
        trackWeight = true,
        trackHealth = true,
        gestationDays = 150,
        heatCycleDays = 21,
        weaningAgeDays = 90
    )

    val GOATS_DAIRY = SpeciesConfig(
        speciesKey = "goats_dairy",
        displayName = "Goats (Dairy)",
        trackBreeding = true,
        trackPregnancy = true,
        trackMilkProduction = true,
        trackHealth = true,
        gestationDays = 150,
        heatCycleDays = 21,
        weaningAgeDays = 60
    )

    val SHEEP = SpeciesConfig(
        speciesKey = "sheep",
        displayName = "Sheep",
        trackBreeding = true,
        trackPregnancy = true,
        trackWeight = true,
        trackHealth = true,
        gestationDays = 147,
        heatCycleDays = 17,
        weaningAgeDays = 90
    )

    val TURKEYS = SpeciesConfig(
        speciesKey = "turkeys",
        displayName = "Turkeys",
        trackWeight = true,
        trackHealth = true
    )

    val DUCKS = SpeciesConfig(
        speciesKey = "ducks",
        displayName = "Ducks",
        trackEggProduction = true,
        trackHealth = true
    )

    val RABBITS = SpeciesConfig(
        speciesKey = "rabbits",
        displayName = "Rabbits",
        trackBreeding = true,
        trackPregnancy = true,
        trackWeight = true,
        trackHealth = true,
        gestationDays = 31,
        weaningAgeDays = 56
    )

    val QUAIL = SpeciesConfig(
        speciesKey = "quail",
        displayName = "Quail",
        trackEggProduction = true,
        trackHealth = true
    )

    val HORSES = SpeciesConfig(
        speciesKey = "horses",
        displayName = "Horses",
        trackBreeding = true,
        trackPregnancy = true,
        trackWeight = true,
        trackHealth = true,
        gestationDays = 340,
        heatCycleDays = 21,
        weaningAgeDays = 180
    )

    val LLAMAS_ALPACAS = SpeciesConfig(
        speciesKey = "llamas_alpacas",
        displayName = "Llamas/Alpacas",
        trackBreeding = true,
        trackPregnancy = true,
        trackHealth = true,
        gestationDays = 350,
        weaningAgeDays = 180
    )

    val BEES = SpeciesConfig(
        speciesKey = "bees",
        displayName = "Bees",
        trackHealth = true
    )

    val ALL_DEFAULTS = listOf(
        CATTLE_BEEF,
        CATTLE_DAIRY,
        CHICKENS_LAYERS,
        CHICKENS_BROILERS,
        PIGS,
        GOATS_MEAT,
        GOATS_DAIRY,
        SHEEP,
        TURKEYS,
        DUCKS,
        RABBITS,
        QUAIL,
        HORSES,
        LLAMAS_ALPACAS,
        BEES
    )
}
