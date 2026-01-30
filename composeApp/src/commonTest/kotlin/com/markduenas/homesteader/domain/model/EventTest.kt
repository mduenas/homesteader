package com.markduenas.homesteader.domain.model

import com.markduenas.homesteader.TestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EventTypeTest {

    @Test
    fun `all event types have display names`() {
        EventType.entries.forEach { eventType ->
            assertTrue(eventType.displayName.isNotBlank(), "EventType $eventType should have a display name")
        }
    }

    @Test
    fun `all event types have categories`() {
        EventType.entries.forEach { eventType ->
            assertNotNull(eventType.category, "EventType $eventType should have a category")
        }
    }

    @Test
    fun `health events are categorized correctly`() {
        val healthEvents = listOf(
            EventType.VACCINATION,
            EventType.TREATMENT,
            EventType.VET_VISIT,
            EventType.ILLNESS,
            EventType.INJURY,
            EventType.DEWORMING,
            EventType.HOOF_TRIM
        )

        healthEvents.forEach { eventType ->
            assertEquals(
                EventCategory.HEALTH,
                eventType.category,
                "EventType $eventType should be in HEALTH category"
            )
        }
    }

    @Test
    fun `breeding events are categorized correctly`() {
        val breedingEvents = listOf(
            EventType.HEAT_OBSERVED,
            EventType.BRED,
            EventType.PREGNANCY_CHECK,
            EventType.BIRTH,
            EventType.WEANING
        )

        breedingEvents.forEach { eventType ->
            assertEquals(
                EventCategory.BREEDING,
                eventType.category,
                "EventType $eventType should be in BREEDING category"
            )
        }
    }

    @Test
    fun `production events are categorized correctly`() {
        val productionEvents = listOf(
            EventType.MILK_RECORD,
            EventType.EGG_COLLECTION,
            EventType.SHEARING
        )

        productionEvents.forEach { eventType ->
            assertEquals(
                EventCategory.PRODUCTION,
                eventType.category,
                "EventType $eventType should be in PRODUCTION category"
            )
        }
    }

    @Test
    fun `weight event is categorized correctly`() {
        assertEquals(EventCategory.WEIGHT, EventType.WEIGHT_RECORD.category)
    }

    @Test
    fun `status events are categorized correctly`() {
        assertEquals(EventCategory.STATUS, EventType.STATUS_CHANGE.category)
    }

    @Test
    fun `movement events are categorized correctly`() {
        assertEquals(EventCategory.MOVEMENT, EventType.MOVED.category)
    }

    @Test
    fun `general events are categorized correctly`() {
        assertEquals(EventCategory.GENERAL, EventType.NOTE.category)
        assertEquals(EventCategory.GENERAL, EventType.CUSTOM.category)
    }

    @Test
    fun `fromString returns correct event type`() {
        assertEquals(EventType.VACCINATION, EventType.fromString("VACCINATION"))
        assertEquals(EventType.BIRTH, EventType.fromString("BIRTH"))
        assertEquals(EventType.WEIGHT_RECORD, EventType.fromString("WEIGHT_RECORD"))
    }

    @Test
    fun `fromString is case insensitive`() {
        assertEquals(EventType.VACCINATION, EventType.fromString("vaccination"))
        assertEquals(EventType.BIRTH, EventType.fromString("birth"))
    }

    @Test
    fun `fromString returns CUSTOM for unknown values`() {
        assertEquals(EventType.CUSTOM, EventType.fromString("unknown_event"))
        assertEquals(EventType.CUSTOM, EventType.fromString(""))
    }
}

class EventCategoryTest {

    @Test
    fun `all categories have display names`() {
        EventCategory.entries.forEach { category ->
            assertTrue(category.displayName.isNotBlank(), "Category $category should have a display name")
        }
    }

    @Test
    fun `expected categories exist`() {
        val expectedCategories = listOf(
            EventCategory.HEALTH,
            EventCategory.BREEDING,
            EventCategory.PRODUCTION,
            EventCategory.WEIGHT,
            EventCategory.MOVEMENT,
            EventCategory.STATUS,
            EventCategory.GENERAL
        )

        expectedCategories.forEach { category ->
            assertTrue(
                EventCategory.entries.contains(category),
                "Category $category should exist"
            )
        }
    }
}

class AnimalEventTest {

    @Test
    fun `event is created with correct properties`() {
        val event = TestData.createEvent(
            id = "event-1",
            animalId = "animal-1",
            eventType = EventType.VACCINATION,
            notes = "Annual vaccination"
        )

        assertEquals("event-1", event.id)
        assertEquals("animal-1", event.animalId)
        assertEquals(EventType.VACCINATION, event.eventType)
        assertEquals("Annual vaccination", event.notes)
    }

    @Test
    fun `event date is correctly stored`() {
        val event = TestData.createEvent()

        assertNotNull(event.eventDate)
        assertEquals(2024, event.eventDate.year)
        assertEquals(1, event.eventDate.monthNumber)
        assertEquals(15, event.eventDate.dayOfMonth)
    }

    @Test
    fun `event category is derived from event type`() {
        val vaccinationEvent = TestData.createEvent(eventType = EventType.VACCINATION)
        val breedingEvent = TestData.createEvent(eventType = EventType.BRED)
        val weightEvent = TestData.createEvent(eventType = EventType.WEIGHT_RECORD)

        assertEquals(EventCategory.HEALTH, vaccinationEvent.eventType.category)
        assertEquals(EventCategory.BREEDING, breedingEvent.eventType.category)
        assertEquals(EventCategory.WEIGHT, weightEvent.eventType.category)
    }
}
