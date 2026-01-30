package com.markduenas.homesteader.data.repository

import com.markduenas.homesteader.domain.model.ReminderType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for ReminderRepository using a real in-memory SQLite database.
 */
class ReminderRepositoryIntegrationTest {

    private lateinit var repository: ReminderRepository

    @Before
    fun setup() {
        val database = TestDatabaseHelper.createInMemoryDatabase()
        repository = ReminderRepository(database.reminderQueries)
    }

    @Test
    fun `insert and retrieve reminder by id`() = runTest {
        val id = repository.insertReminder(
            animalId = "animal-1",
            title = "Vaccination Due",
            description = "Annual vaccination reminder",
            reminderType = ReminderType.VACCINATION_DUE,
            dueDate = LocalDate(2024, 6, 15)
        )

        val retrieved = repository.getReminderById(id).first()
        assertNotNull(retrieved)
        assertEquals(id, retrieved.id)
        assertEquals("animal-1", retrieved.animalId)
        assertEquals("Vaccination Due", retrieved.title)
        assertEquals(ReminderType.VACCINATION_DUE, retrieved.reminderType)
        assertFalse(retrieved.isCompleted)
    }

    @Test
    fun `get all reminders returns inserted reminders`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Reminder 1",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )
        repository.insertReminder(
            animalId = "animal-2",
            title = "Reminder 2",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 2)
        )

        val reminders = repository.getAllReminders().first()
        assertEquals(2, reminders.size)
    }

    @Test
    fun `get pending reminders excludes completed`() = runTest {
        val id1 = repository.insertReminder(
            animalId = "animal-1",
            title = "Pending",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )
        val id2 = repository.insertReminder(
            animalId = "animal-1",
            title = "Completed",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 2)
        )
        repository.markCompleted(id2)

        val pending = repository.getPendingReminders().first()
        assertEquals(1, pending.size)
        assertEquals("Pending", pending.first().title)
    }

    @Test
    fun `get pending reminders in range filters by date`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Before range",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 5, 1)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "In range",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 15)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "After range",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 7, 1)
        )

        val inRange = repository.getPendingRemindersInRange(
            LocalDate(2024, 6, 1),
            LocalDate(2024, 6, 30)
        ).first()
        assertEquals(1, inRange.size)
        assertEquals("In range", inRange.first().title)
    }

    @Test
    fun `get reminders for animal filters by animal id`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Animal 1 reminder",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )
        repository.insertReminder(
            animalId = "animal-2",
            title = "Animal 2 reminder",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )

        val animal1Reminders = repository.getRemindersForAnimal("animal-1").first()
        assertEquals(1, animal1Reminders.size)
        assertEquals("animal-1", animal1Reminders.first().animalId)
    }

    @Test
    fun `get reminders by type filters correctly`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Vaccination",
            description = null,
            reminderType = ReminderType.VACCINATION_DUE,
            dueDate = LocalDate(2024, 6, 1)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "Birth due",
            description = null,
            reminderType = ReminderType.BIRTH_DUE,
            dueDate = LocalDate(2024, 6, 1)
        )

        val vaccinations = repository.getRemindersByType(ReminderType.VACCINATION_DUE).first()
        assertEquals(1, vaccinations.size)
        assertEquals(ReminderType.VACCINATION_DUE, vaccinations.first().reminderType)
    }

    @Test
    fun `get upcoming reminders returns limited pending reminders by due date`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Soon",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 5)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "Later",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 20)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "After max",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 7, 1)
        )

        val upcoming = repository.getUpcomingReminders(
            maxDueDate = LocalDate(2024, 6, 25),
            limit = 10
        ).first()
        assertEquals(2, upcoming.size)
    }

    @Test
    fun `get overdue reminders returns pending reminders before today`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Overdue",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 1, 1)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "Future",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 12, 31)
        )

        val overdue = repository.getOverdueReminders(LocalDate(2024, 6, 15)).first()
        assertEquals(1, overdue.size)
        assertEquals("Overdue", overdue.first().title)
    }

    @Test
    fun `mark completed updates reminder status`() = runTest {
        val id = repository.insertReminder(
            animalId = "animal-1",
            title = "Test",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )

        val beforeComplete = repository.getReminderById(id).first()
        assertNotNull(beforeComplete)
        assertFalse(beforeComplete.isCompleted)

        repository.markCompleted(id)

        val afterComplete = repository.getReminderById(id).first()
        assertNotNull(afterComplete)
        assertTrue(afterComplete.isCompleted)
    }

    @Test
    fun `mark pending resets completed status`() = runTest {
        val id = repository.insertReminder(
            animalId = "animal-1",
            title = "Test",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )
        repository.markCompleted(id)

        val completed = repository.getReminderById(id).first()
        assertTrue(completed?.isCompleted == true)

        repository.markPending(id)

        val pending = repository.getReminderById(id).first()
        assertFalse(pending?.isCompleted == true)
    }

    @Test
    fun `update reminder persists changes`() = runTest {
        val id = repository.insertReminder(
            animalId = "animal-1",
            title = "Original title",
            description = "Original description",
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )

        repository.updateReminder(
            id = id,
            title = "Updated title",
            description = "Updated description",
            reminderType = ReminderType.VACCINATION_DUE,
            dueDate = LocalDate(2024, 7, 1),
            isRecurring = true,
            recurrenceIntervalDays = 365,
            customData = null
        )

        val updated = repository.getReminderById(id).first()
        assertNotNull(updated)
        assertEquals("Updated title", updated.title)
        assertEquals("Updated description", updated.description)
        assertEquals(ReminderType.VACCINATION_DUE, updated.reminderType)
        assertEquals(LocalDate(2024, 7, 1), updated.dueDate)
        assertTrue(updated.isRecurring)
        assertEquals(365, updated.recurrenceIntervalDays)
    }

    @Test
    fun `delete reminder removes from database`() = runTest {
        val id = repository.insertReminder(
            animalId = "animal-1",
            title = "Test",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )

        val beforeDelete = repository.getReminderById(id).first()
        assertNotNull(beforeDelete)

        repository.deleteReminder(id)

        val afterDelete = repository.getReminderById(id).first()
        assertNull(afterDelete)
    }

    @Test
    fun `delete reminders for animal removes all for that animal`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Reminder 1",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "Reminder 2",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 2)
        )
        repository.insertReminder(
            animalId = "animal-2",
            title = "Other animal",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )

        repository.deleteRemindersForAnimal("animal-1")

        val animal1Reminders = repository.getRemindersForAnimal("animal-1").first()
        assertTrue(animal1Reminders.isEmpty())

        val animal2Reminders = repository.getRemindersForAnimal("animal-2").first()
        assertEquals(1, animal2Reminders.size)
    }

    @Test
    fun `count pending returns correct count`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Pending 1",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "Pending 2",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 2)
        )
        val completedId = repository.insertReminder(
            animalId = "animal-1",
            title = "Completed",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 3)
        )
        repository.markCompleted(completedId)

        val count = repository.countPending()
        assertEquals(2L, count)
    }

    @Test
    fun `count overdue returns correct count`() = runTest {
        repository.insertReminder(
            animalId = "animal-1",
            title = "Overdue 1",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 1, 1)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "Overdue 2",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 1, 15)
        )
        repository.insertReminder(
            animalId = "animal-1",
            title = "Future",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 12, 31)
        )

        val count = repository.countOverdue(LocalDate(2024, 6, 15))
        assertEquals(2L, count)
    }

    @Test
    fun `insert recurring reminder preserves recurrence settings`() = runTest {
        val id = repository.insertReminder(
            animalId = "animal-1",
            title = "Recurring reminder",
            description = null,
            reminderType = ReminderType.VACCINATION_DUE,
            dueDate = LocalDate(2024, 6, 1),
            isRecurring = true,
            recurrenceIntervalDays = 30
        )

        val reminder = repository.getReminderById(id).first()
        assertNotNull(reminder)
        assertTrue(reminder.isRecurring)
        assertEquals(30, reminder.recurrenceIntervalDays)
    }

    @Test
    fun `reminder without animal has null animal id`() = runTest {
        val id = repository.insertReminder(
            animalId = null,
            title = "General reminder",
            description = null,
            reminderType = ReminderType.CUSTOM,
            dueDate = LocalDate(2024, 6, 1)
        )

        val reminder = repository.getReminderById(id).first()
        assertNotNull(reminder)
        assertNull(reminder.animalId)
    }
}
