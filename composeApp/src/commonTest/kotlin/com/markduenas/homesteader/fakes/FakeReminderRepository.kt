package com.markduenas.homesteader.fakes

import com.markduenas.homesteader.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/**
 * Fake implementation of ReminderRepository for testing.
 */
class FakeReminderRepository {

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())

    val reminders: Flow<List<Reminder>> = _reminders

    fun getAllReminders(): Flow<List<Reminder>> = _reminders

    fun getPendingReminders(): Flow<List<Reminder>> =
        _reminders.map { reminders ->
            reminders.filter { !it.isCompleted }
        }

    fun getCompletedReminders(): Flow<List<Reminder>> =
        _reminders.map { reminders ->
            reminders.filter { it.isCompleted }
        }

    fun getRemindersForAnimal(animalId: String): Flow<List<Reminder>> =
        _reminders.map { reminders -> reminders.filter { it.animalId == animalId } }

    fun getReminderById(id: String): Flow<Reminder?> =
        _reminders.map { reminders -> reminders.find { it.id == id } }

    fun getRemindersInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Reminder>> =
        _reminders.map { reminders ->
            reminders.filter { reminder ->
                reminder.dueDate >= startDate && reminder.dueDate <= endDate
            }
        }

    fun getOverdueReminders(currentDate: LocalDate): Flow<List<Reminder>> =
        _reminders.map { reminders ->
            reminders.filter { reminder ->
                !reminder.isCompleted && reminder.dueDate < currentDate
            }
        }

    fun getUpcomingReminders(fromDate: LocalDate, days: Int = 7): Flow<List<Reminder>> =
        _reminders.map { reminders ->
            val toDate = LocalDate.fromEpochDays(fromDate.toEpochDays() + days)
            reminders.filter { reminder ->
                !reminder.isCompleted &&
                        reminder.dueDate >= fromDate &&
                        reminder.dueDate <= toDate
            }.sortedBy { it.dueDate }
        }

    suspend fun insertReminder(reminder: Reminder) {
        _reminders.update { currentList ->
            currentList + reminder
        }
    }

    suspend fun updateReminder(reminder: Reminder) {
        _reminders.update { currentList ->
            currentList.map { if (it.id == reminder.id) reminder else it }
        }
    }

    suspend fun markAsCompleted(id: String) {
        _reminders.update { currentList ->
            currentList.map { reminder ->
                if (reminder.id == id) reminder.copy(isCompleted = true) else reminder
            }
        }
    }

    suspend fun deleteReminder(id: String) {
        _reminders.update { currentList ->
            currentList.filter { it.id != id }
        }
    }

    fun getPendingReminderCount(): Flow<Long> =
        _reminders.map { reminders ->
            reminders.count { !it.isCompleted }.toLong()
        }

    // Test helpers
    fun setReminders(reminders: List<Reminder>) {
        _reminders.value = reminders
    }

    fun clear() {
        _reminders.value = emptyList()
    }
}
