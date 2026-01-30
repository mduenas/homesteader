package com.markduenas.homesteader.data.database

import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.BreedingEventData
import com.markduenas.homesteader.domain.model.EventData
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.GeneralEventData
import com.markduenas.homesteader.domain.model.HealthEventData
import com.markduenas.homesteader.domain.model.MovementEventData
import com.markduenas.homesteader.domain.model.ProductionEventData
import com.markduenas.homesteader.domain.model.StatusChangeEventData
import com.markduenas.homesteader.domain.model.WeightEventData
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val eventDataModule = SerializersModule {
    polymorphic(EventData::class) {
        subclass(HealthEventData::class)
        subclass(BreedingEventData::class)
        subclass(ProductionEventData::class)
        subclass(WeightEventData::class)
        subclass(MovementEventData::class)
        subclass(StatusChangeEventData::class)
        subclass(GeneralEventData::class)
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    serializersModule = eventDataModule
}

fun AnimalEventEntity.toDomain(): AnimalEvent {
    return AnimalEvent(
        id = id,
        animalId = animal_id,
        eventType = EventType.fromString(event_type),
        eventDate = DateTimeUtil.parseIsoDate(event_date) ?: DateTimeUtil.today(),
        notes = notes,
        createdAt = created_at,
        updatedAt = updated_at,
        eventData = parseEventData(event_type, event_data)
    )
}

private fun parseEventData(eventType: String, jsonString: String?): EventData? {
    if (jsonString.isNullOrBlank()) return null

    return try {
        val type = EventType.fromString(eventType)
        when (type.category) {
            com.markduenas.homesteader.domain.model.EventCategory.HEALTH ->
                json.decodeFromString<HealthEventData>(jsonString)
            com.markduenas.homesteader.domain.model.EventCategory.BREEDING ->
                json.decodeFromString<BreedingEventData>(jsonString)
            com.markduenas.homesteader.domain.model.EventCategory.PRODUCTION ->
                json.decodeFromString<ProductionEventData>(jsonString)
            com.markduenas.homesteader.domain.model.EventCategory.WEIGHT ->
                json.decodeFromString<WeightEventData>(jsonString)
            com.markduenas.homesteader.domain.model.EventCategory.MOVEMENT ->
                json.decodeFromString<MovementEventData>(jsonString)
            com.markduenas.homesteader.domain.model.EventCategory.STATUS ->
                json.decodeFromString<StatusChangeEventData>(jsonString)
            com.markduenas.homesteader.domain.model.EventCategory.GENERAL ->
                json.decodeFromString<GeneralEventData>(jsonString)
        }
    } catch (e: Exception) {
        null
    }
}

fun serializeEventData(eventData: EventData?): String? {
    if (eventData == null) return null

    return try {
        when (eventData) {
            is HealthEventData -> json.encodeToString(HealthEventData.serializer(), eventData)
            is BreedingEventData -> json.encodeToString(BreedingEventData.serializer(), eventData)
            is ProductionEventData -> json.encodeToString(ProductionEventData.serializer(), eventData)
            is WeightEventData -> json.encodeToString(WeightEventData.serializer(), eventData)
            is MovementEventData -> json.encodeToString(MovementEventData.serializer(), eventData)
            is StatusChangeEventData -> json.encodeToString(StatusChangeEventData.serializer(), eventData)
            is GeneralEventData -> json.encodeToString(GeneralEventData.serializer(), eventData)
        }
    } catch (e: Exception) {
        null
    }
}
