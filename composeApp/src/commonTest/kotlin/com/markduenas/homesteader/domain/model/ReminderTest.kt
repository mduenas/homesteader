package com.markduenas.homesteader.domain.model

import com.markduenas.homesteader.TestData
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReminderTest {

    @Test
    fun `reminder is created with correct properties`() {
        val reminder = TestData.createReminder(
            id = "reminder-1",
            animalId = "animal-1",
            title = "Vaccination Due",
            description = "Annual vaccination reminder"
        )

        assertEquals("reminder-1", reminder.id)
        assertEquals("animal-1", reminder.animalId)
        assertEquals("Vaccination Due", reminder.title)
        assertEquals("Annual vaccination reminder", reminder.description)
    }

    @Test
    fun `reminder due date is correctly stored`() {
        val reminder = TestData.createReminder(
            dueDate = LocalDate(2024, 6, 15)
        )

        assertEquals(2024, reminder.dueDate.year)
        assertEquals(6, reminder.dueDate.monthNumber)
        assertEquals(15, reminder.dueDate.dayOfMonth)
    }

    @Test
    fun `reminder isCompleted defaults to false`() {
        val reminder = TestData.createReminder()
        assertFalse(reminder.isCompleted)
    }

    @Test
    fun `reminder can be marked as completed`() {
        val reminder = TestData.createReminder(isCompleted = true)
        assertTrue(reminder.isCompleted)
    }

    @Test
    fun `reminder without animal has null animalId`() {
        val reminder = TestData.createReminder(animalId = null)
        assertNull(reminder.animalId)
    }

    @Test
    fun `reminder types are correctly assigned`() {
        val customReminder = TestData.createReminder(reminderType = ReminderType.CUSTOM)
        val birthReminder = TestData.createReminder(reminderType = ReminderType.BIRTH_DUE)
        val vaccinationReminder = TestData.createReminder(reminderType = ReminderType.VACCINATION_DUE)

        assertEquals(ReminderType.CUSTOM, customReminder.reminderType)
        assertEquals(ReminderType.BIRTH_DUE, birthReminder.reminderType)
        assertEquals(ReminderType.VACCINATION_DUE, vaccinationReminder.reminderType)
    }
}

class ReminderTypeTest {

    @Test
    fun `all reminder types have display names`() {
        ReminderType.entries.forEach { reminderType ->
            assertTrue(reminderType.displayName.isNotBlank(), "ReminderType $reminderType should have a display name")
        }
    }

    @Test
    fun `breeding related reminder types exist`() {
        assertNotNull(ReminderType.entries.find { it == ReminderType.HEAT_EXPECTED })
        assertNotNull(ReminderType.entries.find { it == ReminderType.PREGNANCY_CHECK })
        assertNotNull(ReminderType.entries.find { it == ReminderType.BIRTH_DUE })
        assertNotNull(ReminderType.entries.find { it == ReminderType.WEANING_DUE })
    }

    @Test
    fun `health related reminder types exist`() {
        assertNotNull(ReminderType.entries.find { it == ReminderType.VACCINATION_DUE })
        assertNotNull(ReminderType.entries.find { it == ReminderType.VET_FOLLOWUP })
    }

    @Test
    fun `custom reminder type exists`() {
        assertNotNull(ReminderType.entries.find { it == ReminderType.CUSTOM })
    }

    @Test
    fun `fromString returns correct reminder type`() {
        assertEquals(ReminderType.VACCINATION_DUE, ReminderType.fromString("VACCINATION_DUE"))
        assertEquals(ReminderType.BIRTH_DUE, ReminderType.fromString("BIRTH_DUE"))
        assertEquals(ReminderType.CUSTOM, ReminderType.fromString("CUSTOM"))
    }

    @Test
    fun `fromString is case insensitive`() {
        assertEquals(ReminderType.VACCINATION_DUE, ReminderType.fromString("vaccination_due"))
        assertEquals(ReminderType.BIRTH_DUE, ReminderType.fromString("birth_due"))
    }

    @Test
    fun `fromString returns CUSTOM for unknown values`() {
        assertEquals(ReminderType.CUSTOM, ReminderType.fromString("unknown_type"))
        assertEquals(ReminderType.CUSTOM, ReminderType.fromString(""))
    }
}

class ReminderCalculatorTest {

    @Test
    fun `calculatePregnancyCheckDate adds correct days`() {
        val breedingDate = LocalDate(2024, 1, 1)
        val checkDate = ReminderCalculator.calculatePregnancyCheckDate(breedingDate, 35)

        assertEquals(LocalDate(2024, 2, 5), checkDate)
    }

    @Test
    fun `calculateBirthDueDate adds gestation days`() {
        val breedingDate = LocalDate(2024, 1, 1)
        // Cattle gestation ~283 days
        val birthDate = ReminderCalculator.calculateBirthDueDate(breedingDate, 283)

        assertEquals(LocalDate(2024, 10, 10), birthDate)
    }

    @Test
    fun `calculateWeaningDueDate adds weaning days`() {
        val birthDate = LocalDate(2024, 1, 1)
        // 60 days for dairy calves
        val weaningDate = ReminderCalculator.calculateWeaningDueDate(birthDate, 60)

        assertEquals(LocalDate(2024, 3, 1), weaningDate)
    }

    @Test
    fun `calculateNextHeatDate adds cycle days`() {
        val lastHeatDate = LocalDate(2024, 1, 1)
        // 21 day cycle for cattle
        val nextHeatDate = ReminderCalculator.calculateNextHeatDate(lastHeatDate, 21)

        assertEquals(LocalDate(2024, 1, 22), nextHeatDate)
    }

    @Test
    fun `calculateNextVaccinationDate adds interval days`() {
        val lastVaccinationDate = LocalDate(2023, 1, 1)
        // Annual vaccination (365 days) - using 2023 (non-leap year)
        val nextVaccinationDate = ReminderCalculator.calculateNextVaccinationDate(lastVaccinationDate, 365)

        assertEquals(LocalDate(2024, 1, 1), nextVaccinationDate)
    }
}
