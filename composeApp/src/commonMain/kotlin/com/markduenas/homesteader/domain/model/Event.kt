package com.markduenas.homesteader.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Types of events that can be recorded for animals
 */
enum class EventType(val displayName: String, val category: EventCategory) {
    // Health events
    VACCINATION("Vaccination", EventCategory.HEALTH),
    TREATMENT("Treatment", EventCategory.HEALTH),
    VET_VISIT("Vet Visit", EventCategory.HEALTH),
    ILLNESS("Illness", EventCategory.HEALTH),
    INJURY("Injury", EventCategory.HEALTH),
    DEWORMING("Deworming", EventCategory.HEALTH),
    HOOF_TRIM("Hoof Trim", EventCategory.HEALTH),

    // Breeding events
    HEAT_OBSERVED("Heat Observed", EventCategory.BREEDING),
    BRED("Bred", EventCategory.BREEDING),
    PREGNANCY_CHECK("Pregnancy Check", EventCategory.BREEDING),
    BIRTH("Birth", EventCategory.BREEDING),
    WEANING("Weaning", EventCategory.BREEDING),

    // Production events
    MILK_RECORD("Milk Record", EventCategory.PRODUCTION),
    EGG_COLLECTION("Egg Collection", EventCategory.PRODUCTION),
    SHEARING("Shearing", EventCategory.PRODUCTION),

    // Weight/Growth events
    WEIGHT_RECORD("Weight Record", EventCategory.WEIGHT),

    // Movement events
    MOVED("Moved", EventCategory.MOVEMENT),

    // Status events
    STATUS_CHANGE("Status Change", EventCategory.STATUS),

    // General
    NOTE("Note", EventCategory.GENERAL),
    CUSTOM("Custom Event", EventCategory.GENERAL);

    companion object {
        fun fromString(value: String): EventType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CUSTOM
        }
    }
}

enum class EventCategory(val displayName: String) {
    HEALTH("Health"),
    BREEDING("Breeding"),
    PRODUCTION("Production"),
    WEIGHT("Weight"),
    MOVEMENT("Movement"),
    STATUS("Status"),
    GENERAL("General")
}

/**
 * Base event data common to all event types
 */
@Serializable
data class AnimalEvent(
    val id: String = "",
    val animalId: String,
    val eventType: EventType,
    val eventDate: LocalDate,
    val notes: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    // Type-specific data stored as JSON
    val eventData: EventData? = null
)

/**
 * Sealed class for type-specific event data
 */
@Serializable
sealed class EventData

@Serializable
data class HealthEventData(
    val medicationName: String? = null,
    val dosage: String? = null,
    val administrationRoute: String? = null, // oral, injection, topical
    val veterinarian: String? = null,
    val diagnosis: String? = null,
    val followUpDate: String? = null,
    val cost: Double? = null
) : EventData()

@Serializable
data class BreedingEventData(
    val sireId: String? = null,
    val sireName: String? = null,
    val breedingMethod: String? = null, // natural, AI
    val isConfirmedPregnant: Boolean? = null,
    val expectedDueDate: String? = null,
    val actualBirthDate: String? = null,
    val offspringCount: Int? = null,
    val offspringIds: List<String> = emptyList(),
    val heatCycleDay: Int? = null,
    val weaningWeight: Double? = null
) : EventData()

@Serializable
data class ProductionEventData(
    // Milk production
    val milkQuantity: Double? = null,
    val milkUnit: String? = null, // gallons, liters, pounds
    val milkFatPercent: Double? = null,
    val milkProteinPercent: Double? = null,
    val somaticCellCount: Int? = null,

    // Egg production
    val eggCount: Int? = null,
    val eggGrade: String? = null, // AA, A, B
    val eggSize: String? = null, // jumbo, extra-large, large, medium, small

    // Fiber/Shearing
    val fiberWeight: Double? = null,
    val fiberUnit: String? = null, // pounds, kg
    val fiberQuality: String? = null
) : EventData()

@Serializable
data class WeightEventData(
    val weight: Double,
    val weightUnit: String = "lbs", // lbs, kg
    val condition: String? = null, // body condition score
    val notes: String? = null
) : EventData()

@Serializable
data class MovementEventData(
    val fromLocation: String? = null,
    val toLocation: String? = null,
    val reason: String? = null
) : EventData()

@Serializable
data class StatusChangeEventData(
    val previousStatus: String? = null,
    val newStatus: String,
    val reason: String? = null,
    val salePrice: Double? = null,
    val buyer: String? = null
) : EventData()

@Serializable
data class GeneralEventData(
    val customType: String? = null,
    val customData: Map<String, String> = emptyMap()
) : EventData()

/**
 * Pregnancy check results
 */
enum class PregnancyResult(val displayName: String) {
    POSITIVE("Pregnant"),
    NEGATIVE("Not Pregnant"),
    INCONCLUSIVE("Inconclusive"),
    RECHECK("Needs Recheck")
}

/**
 * Breeding methods
 */
enum class BreedingMethod(val displayName: String) {
    NATURAL("Natural"),
    ARTIFICIAL_INSEMINATION("Artificial Insemination"),
    EMBRYO_TRANSFER("Embryo Transfer")
}
