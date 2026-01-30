package com.markduenas.homesteader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.database.AnimalEventQueries
import com.markduenas.homesteader.data.database.serializeEventData
import com.markduenas.homesteader.data.database.toDomain
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class EventRepository(
    private val queries: AnimalEventQueries
) {
    private val dispatcher = Dispatchers.IO

    fun getEventsByAnimalId(animalId: String): Flow<List<AnimalEvent>> {
        return queries.selectByAnimalId(animalId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getEventsByType(eventType: EventType): Flow<List<AnimalEvent>> {
        return queries.selectByType(eventType.name)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getEventsByAnimalAndType(animalId: String, eventType: EventType): Flow<List<AnimalEvent>> {
        return queries.selectByAnimalAndType(animalId, eventType.name)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getEventById(id: String): Flow<AnimalEvent?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
    }

    fun getRecentEvents(limit: Long = 20): Flow<List<AnimalEvent>> {
        return queries.selectRecent(limit)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getEventsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<AnimalEvent>> {
        return queries.selectByDateRange(startDate.toString(), endDate.toString())
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getEventsByAnimalAndDateRange(
        animalId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<AnimalEvent>> {
        return queries.selectByAnimalAndDateRange(animalId, startDate.toString(), endDate.toString())
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getLatestEventByAnimalAndType(animalId: String, eventType: EventType): Flow<AnimalEvent?> {
        return queries.selectLatestByAnimalAndType(animalId, eventType.name)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
    }

    fun getBreedingEvents(animalId: String): Flow<List<AnimalEvent>> {
        return queries.selectBreedingEvents(animalId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getHealthEvents(animalId: String): Flow<List<AnimalEvent>> {
        return queries.selectHealthEvents(animalId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getProductionEvents(animalId: String): Flow<List<AnimalEvent>> {
        return queries.selectProductionEvents(animalId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getWeightEvents(animalId: String): Flow<List<AnimalEvent>> {
        return queries.selectWeightEvents(animalId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun insertEvent(event: AnimalEvent): String = withContext(dispatcher) {
        val id = event.id.ifBlank { uuid4().toString() }
        val now = DateTimeUtil.nowIsoString()
        val eventDataJson = serializeEventData(event.eventData)

        queries.insert(
            id = id,
            animal_id = event.animalId,
            event_type = event.eventType.name,
            event_date = event.eventDate.toString(),
            notes = event.notes,
            event_data = eventDataJson,
            created_at = now,
            updated_at = now
        )

        id
    }

    suspend fun updateEvent(event: AnimalEvent) = withContext(dispatcher) {
        val now = DateTimeUtil.nowIsoString()
        val eventDataJson = serializeEventData(event.eventData)

        queries.update(
            event_type = event.eventType.name,
            event_date = event.eventDate.toString(),
            notes = event.notes,
            event_data = eventDataJson,
            updated_at = now,
            id = event.id
        )
    }

    suspend fun deleteEvent(id: String) = withContext(dispatcher) {
        queries.delete(id)
    }

    suspend fun deleteEventsByAnimal(animalId: String) = withContext(dispatcher) {
        queries.deleteByAnimal(animalId)
    }

    suspend fun getEventCountByAnimal(animalId: String): Long = withContext(dispatcher) {
        queries.countByAnimal(animalId).executeAsOne()
    }

    suspend fun getEventCountByAnimalAndType(animalId: String, eventType: EventType): Long =
        withContext(dispatcher) {
            queries.countByAnimalAndType(animalId, eventType.name).executeAsOne()
        }
}
