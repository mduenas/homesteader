package com.markduenas.homesteader.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Animal(
    val id: String,
    val tagId: String? = null,
    val name: String? = null,
    val species: Species,
    val breed: String? = null,
    val sex: Sex,
    val birthDate: LocalDate? = null,
    val acquisitionDate: LocalDate? = null,
    val status: AnimalStatus = AnimalStatus.ACTIVE,
    val motherId: String? = null,
    val fatherId: String? = null,
    val notes: String? = null,
    val photoUri: String? = null,
    val customFields: Map<String, String> = emptyMap()
) {
    val displayName: String
        get() = name ?: tagId ?: "Unnamed"
}

@Serializable
enum class Sex {
    MALE,
    FEMALE,
    UNKNOWN;

    val displayName: String
        get() = when (this) {
            MALE -> "Male"
            FEMALE -> "Female"
            UNKNOWN -> "Unknown"
        }
}

@Serializable
enum class AnimalStatus {
    ACTIVE,
    SOLD,
    DECEASED,
    TRANSFERRED;

    val displayName: String
        get() = when (this) {
            ACTIVE -> "Active"
            SOLD -> "Sold"
            DECEASED -> "Deceased"
            TRANSFERRED -> "Transferred"
        }
}

@Serializable
enum class Species(
    val key: String,
    val displayName: String,
    val defaultGestationDays: Int? = null,
    val defaultHeatCycleDays: Int? = null
) {
    CATTLE_BEEF("cattle_beef", "Beef Cattle", 283, 21),
    CATTLE_DAIRY("cattle_dairy", "Dairy Cattle", 283, 21),
    GOAT_MEAT("goat_meat", "Meat Goat", 150, 21),
    GOAT_DAIRY("goat_dairy", "Dairy Goat", 150, 21),
    SHEEP("sheep", "Sheep", 147, 17),
    PIG("pig", "Pig", 114, 21),
    CHICKEN_LAYER("chicken_layer", "Layer Chicken"),
    CHICKEN_BROILER("chicken_broiler", "Broiler Chicken"),
    TURKEY("turkey", "Turkey", 28),
    DUCK("duck", "Duck", 28),
    QUAIL("quail", "Quail", 17),
    RABBIT("rabbit", "Rabbit", 31),
    HORSE("horse", "Horse", 340, 21),
    DONKEY("donkey", "Donkey", 365, 25),
    ALPACA("alpaca", "Alpaca", 345),
    LLAMA("llama", "Llama", 350),
    CUSTOM("custom", "Custom");

    companion object {
        fun fromKey(key: String): Species {
            return entries.find { it.key == key } ?: CUSTOM
        }
    }
}
