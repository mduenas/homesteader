package com.markduenas.homesteader

import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderType
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import com.markduenas.homesteader.domain.model.SpeciesConfig
import kotlinx.datetime.LocalDate

/**
 * Test utilities and fake data for unit tests.
 */
object TestData {

    fun createAnimal(
        id: String = "test-animal-1",
        name: String? = "Test Cow",
        tagId: String? = "TAG001",
        species: Species = Species.CATTLE_BEEF,
        breed: String? = "Angus",
        sex: Sex = Sex.FEMALE,
        birthDate: LocalDate? = LocalDate(2022, 1, 15),
        status: AnimalStatus = AnimalStatus.ACTIVE,
        notes: String? = null,
        motherId: String? = null,
        fatherId: String? = null,
        acquisitionDate: LocalDate? = null
    ): Animal = Animal(
        id = id,
        name = name,
        tagId = tagId,
        species = species,
        breed = breed,
        sex = sex,
        birthDate = birthDate,
        status = status,
        notes = notes,
        motherId = motherId,
        fatherId = fatherId,
        acquisitionDate = acquisitionDate
    )

    fun createAnimalList(count: Int = 5): List<Animal> = (1..count).map { index ->
        createAnimal(
            id = "animal-$index",
            name = "Animal $index",
            tagId = "TAG00$index"
        )
    }

    fun createEvent(
        id: String = "test-event-1",
        animalId: String = "test-animal-1",
        eventType: EventType = EventType.VACCINATION,
        eventDate: LocalDate = LocalDate(2024, 1, 15),
        notes: String? = "Test event notes"
    ): AnimalEvent = AnimalEvent(
        id = id,
        animalId = animalId,
        eventType = eventType,
        eventDate = eventDate,
        notes = notes,
        eventData = null
    )

    fun createReminder(
        id: String = "test-reminder-1",
        animalId: String? = "test-animal-1",
        title: String = "Test Reminder",
        description: String? = "Test description",
        dueDate: LocalDate = LocalDate(2024, 2, 1),
        reminderType: ReminderType = ReminderType.CUSTOM,
        isCompleted: Boolean = false
    ): Reminder = Reminder(
        id = id,
        animalId = animalId,
        title = title,
        description = description,
        dueDate = dueDate,
        reminderType = reminderType,
        isCompleted = isCompleted,
        sourceEventId = null
    )

    fun createSpeciesConfig(
        speciesKey: String = "cattle_beef",
        displayName: String = "Cattle (Beef)",
        isEnabled: Boolean = true,
        trackBreeding: Boolean = true,
        trackPregnancy: Boolean = true,
        trackHealth: Boolean = true,
        trackMilkProduction: Boolean = false,
        trackEggProduction: Boolean = false,
        trackWeight: Boolean = true,
        trackFeed: Boolean = false,
        gestationDays: Int? = 283,
        heatCycleDays: Int? = 21,
        weaningAgeDays: Int? = 205
    ): SpeciesConfig = SpeciesConfig(
        speciesKey = speciesKey,
        displayName = displayName,
        isEnabled = isEnabled,
        trackBreeding = trackBreeding,
        trackPregnancy = trackPregnancy,
        trackHealth = trackHealth,
        trackMilkProduction = trackMilkProduction,
        trackEggProduction = trackEggProduction,
        trackWeight = trackWeight,
        trackFeed = trackFeed,
        gestationDays = gestationDays,
        heatCycleDays = heatCycleDays,
        weaningAgeDays = weaningAgeDays
    )
}
