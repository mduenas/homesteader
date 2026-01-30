package com.markduenas.homesteader.data.repository

import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.WeightEventData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for EventRepository using a real in-memory SQLite database.
 */
class EventRepositoryIntegrationTest {

    private lateinit var repository: EventRepository

    @Before
    fun setup() {
        val database = TestDatabaseHelper.createInMemoryDatabase()
        repository = EventRepository(database.animalEventQueries)
    }

    private fun createTestEvent(
        id: String = "test-event-1",
        animalId: String = "animal-1",
        eventType: EventType = EventType.VACCINATION,
        eventDate: LocalDate = LocalDate(2024, 1, 15),
        notes: String? = "Test notes"
    ): AnimalEvent = AnimalEvent(
        id = id,
        animalId = animalId,
        eventType = eventType,
        eventDate = eventDate,
        notes = notes,
        eventData = null
    )

    @Test
    fun `insert and retrieve event by id`() = runTest {
        val event = createTestEvent(id = "event-1")

        val insertedId = repository.insertEvent(event)
        assertEquals("event-1", insertedId)

        val retrieved = repository.getEventById("event-1").first()
        assertNotNull(retrieved)
        assertEquals("event-1", retrieved.id)
        assertEquals("animal-1", retrieved.animalId)
        assertEquals(EventType.VACCINATION, retrieved.eventType)
    }

    @Test
    fun `get events by animal id returns all events for that animal`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1"))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1"))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-2"))

        val events = repository.getEventsByAnimalId("animal-1").first()
        assertEquals(2, events.size)
        assertTrue(events.all { it.animalId == "animal-1" })
    }

    @Test
    fun `get events by type returns all events of that type`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", eventType = EventType.VACCINATION))
        repository.insertEvent(createTestEvent(id = "e2", eventType = EventType.VACCINATION))
        repository.insertEvent(createTestEvent(id = "e3", eventType = EventType.TREATMENT))

        val vaccinations = repository.getEventsByType(EventType.VACCINATION).first()
        assertEquals(2, vaccinations.size)
        assertTrue(vaccinations.all { it.eventType == EventType.VACCINATION })
    }

    @Test
    fun `get events by animal and type filters correctly`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1", eventType = EventType.VACCINATION))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1", eventType = EventType.TREATMENT))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-2", eventType = EventType.VACCINATION))

        val events = repository.getEventsByAnimalAndType("animal-1", EventType.VACCINATION).first()
        assertEquals(1, events.size)
        assertEquals("e1", events.first().id)
    }

    @Test
    fun `get recent events returns limited results in date order`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", eventDate = LocalDate(2024, 1, 1)))
        repository.insertEvent(createTestEvent(id = "e2", eventDate = LocalDate(2024, 1, 15)))
        repository.insertEvent(createTestEvent(id = "e3", eventDate = LocalDate(2024, 1, 10)))

        val recent = repository.getRecentEvents(2).first()
        assertEquals(2, recent.size)
        assertEquals("e2", recent[0].id) // Most recent first
    }

    @Test
    fun `get events by date range returns matching events`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", eventDate = LocalDate(2024, 1, 1)))
        repository.insertEvent(createTestEvent(id = "e2", eventDate = LocalDate(2024, 1, 15)))
        repository.insertEvent(createTestEvent(id = "e3", eventDate = LocalDate(2024, 2, 1)))

        val events = repository.getEventsByDateRange(
            LocalDate(2024, 1, 10),
            LocalDate(2024, 1, 20)
        ).first()
        assertEquals(1, events.size)
        assertEquals("e2", events.first().id)
    }

    @Test
    fun `get latest event by animal and type returns most recent`() = runTest {
        repository.insertEvent(createTestEvent(
            id = "e1",
            animalId = "animal-1",
            eventType = EventType.WEIGHT_RECORD,
            eventDate = LocalDate(2024, 1, 1)
        ))
        repository.insertEvent(createTestEvent(
            id = "e2",
            animalId = "animal-1",
            eventType = EventType.WEIGHT_RECORD,
            eventDate = LocalDate(2024, 2, 1)
        ))
        repository.insertEvent(createTestEvent(
            id = "e3",
            animalId = "animal-1",
            eventType = EventType.WEIGHT_RECORD,
            eventDate = LocalDate(2024, 1, 15)
        ))

        val latest = repository.getLatestEventByAnimalAndType("animal-1", EventType.WEIGHT_RECORD).first()
        assertNotNull(latest)
        assertEquals("e2", latest.id)
    }

    @Test
    fun `get breeding events returns only breeding types`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1", eventType = EventType.HEAT_OBSERVED))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1", eventType = EventType.BRED))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-1", eventType = EventType.VACCINATION))
        repository.insertEvent(createTestEvent(id = "e4", animalId = "animal-1", eventType = EventType.BIRTH))

        val breedingEvents = repository.getBreedingEvents("animal-1").first()
        assertEquals(3, breedingEvents.size)
        assertTrue(breedingEvents.none { it.eventType == EventType.VACCINATION })
    }

    @Test
    fun `get health events returns only health types`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1", eventType = EventType.VACCINATION))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1", eventType = EventType.TREATMENT))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-1", eventType = EventType.HEAT_OBSERVED))

        val healthEvents = repository.getHealthEvents("animal-1").first()
        assertEquals(2, healthEvents.size)
        assertTrue(healthEvents.none { it.eventType == EventType.HEAT_OBSERVED })
    }

    @Test
    fun `get weight events returns only weight records`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1", eventType = EventType.WEIGHT_RECORD))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1", eventType = EventType.WEIGHT_RECORD))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-1", eventType = EventType.VACCINATION))

        val weightEvents = repository.getWeightEvents("animal-1").first()
        assertEquals(2, weightEvents.size)
        assertTrue(weightEvents.all { it.eventType == EventType.WEIGHT_RECORD })
    }

    @Test
    fun `update event persists changes`() = runTest {
        val event = createTestEvent(id = "event-1", notes = "Original notes")
        repository.insertEvent(event)

        val updated = event.copy(notes = "Updated notes", eventType = EventType.TREATMENT)
        repository.updateEvent(updated)

        val retrieved = repository.getEventById("event-1").first()
        assertNotNull(retrieved)
        assertEquals("Updated notes", retrieved.notes)
        assertEquals(EventType.TREATMENT, retrieved.eventType)
    }

    @Test
    fun `delete event removes from database`() = runTest {
        repository.insertEvent(createTestEvent(id = "event-1"))

        val beforeDelete = repository.getEventById("event-1").first()
        assertNotNull(beforeDelete)

        repository.deleteEvent("event-1")

        val afterDelete = repository.getEventById("event-1").first()
        assertNull(afterDelete)
    }

    @Test
    fun `delete events by animal removes all events for that animal`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1"))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1"))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-2"))

        repository.deleteEventsByAnimal("animal-1")

        val animal1Events = repository.getEventsByAnimalId("animal-1").first()
        assertTrue(animal1Events.isEmpty())

        val animal2Events = repository.getEventsByAnimalId("animal-2").first()
        assertEquals(1, animal2Events.size)
    }

    @Test
    fun `get event count by animal returns correct count`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1"))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1"))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-2"))

        val count = repository.getEventCountByAnimal("animal-1")
        assertEquals(2L, count)
    }

    @Test
    fun `get event count by animal and type returns correct count`() = runTest {
        repository.insertEvent(createTestEvent(id = "e1", animalId = "animal-1", eventType = EventType.VACCINATION))
        repository.insertEvent(createTestEvent(id = "e2", animalId = "animal-1", eventType = EventType.VACCINATION))
        repository.insertEvent(createTestEvent(id = "e3", animalId = "animal-1", eventType = EventType.TREATMENT))

        val count = repository.getEventCountByAnimalAndType("animal-1", EventType.VACCINATION)
        assertEquals(2L, count)
    }

    @Test
    fun `insert event with weight data preserves it`() = runTest {
        val event = AnimalEvent(
            id = "event-1",
            animalId = "animal-1",
            eventType = EventType.WEIGHT_RECORD,
            eventDate = LocalDate(2024, 1, 15),
            notes = "Weight check",
            eventData = WeightEventData(weight = 450.5, weightUnit = "kg")
        )
        repository.insertEvent(event)

        val retrieved = repository.getEventById("event-1").first()
        assertNotNull(retrieved)
        val weightData = retrieved.eventData as? WeightEventData
        assertNotNull(weightData)
        assertEquals(450.5, weightData.weight)
        assertEquals("kg", weightData.weightUnit)
    }

    @Test
    fun `insert generates id when blank`() = runTest {
        val event = createTestEvent(id = "")
        val generatedId = repository.insertEvent(event)

        assertTrue(generatedId.isNotBlank())

        val retrieved = repository.getEventById(generatedId).first()
        assertNotNull(retrieved)
    }
}
