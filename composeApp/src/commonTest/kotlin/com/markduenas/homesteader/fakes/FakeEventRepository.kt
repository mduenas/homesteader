package com.markduenas.homesteader.fakes

import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.EventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/**
 * Fake implementation of EventRepository for testing.
 */
class FakeEventRepository {

    private val _events = MutableStateFlow<List<AnimalEvent>>(emptyList())

    val events: Flow<List<AnimalEvent>> = _events

    fun getAllEvents(): Flow<List<AnimalEvent>> = _events

    fun getEventsForAnimal(animalId: String): Flow<List<AnimalEvent>> =
        _events.map { events -> events.filter { it.animalId == animalId } }

    fun getEventsByType(eventType: EventType): Flow<List<AnimalEvent>> =
        _events.map { events -> events.filter { it.eventType == eventType } }

    fun getEventById(id: String): Flow<AnimalEvent?> =
        _events.map { events -> events.find { it.id == id } }

    fun getEventsInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<AnimalEvent>> =
        _events.map { events ->
            events.filter { event ->
                event.eventDate >= startDate && event.eventDate <= endDate
            }
        }

    fun getRecentEvents(limit: Int = 10): Flow<List<AnimalEvent>> =
        _events.map { events ->
            events.sortedByDescending { it.eventDate }.take(limit)
        }

    suspend fun insertEvent(event: AnimalEvent) {
        _events.update { currentList ->
            currentList + event
        }
    }

    suspend fun updateEvent(event: AnimalEvent) {
        _events.update { currentList ->
            currentList.map { if (it.id == event.id) event else it }
        }
    }

    suspend fun deleteEvent(id: String) {
        _events.update { currentList ->
            currentList.filter { it.id != id }
        }
    }

    fun getEventCountForAnimal(animalId: String): Flow<Long> =
        _events.map { events -> events.count { it.animalId == animalId }.toLong() }

    // Test helpers
    fun setEvents(events: List<AnimalEvent>) {
        _events.value = events
    }

    fun clear() {
        _events.value = emptyList()
    }
}
