package com.markduenas.homesteader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.database.ReminderQueries
import com.markduenas.homesteader.data.database.serializeReminderData
import com.markduenas.homesteader.data.database.toDomain
import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderData
import com.markduenas.homesteader.domain.model.ReminderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class ReminderRepository(private val queries: ReminderQueries) {

    fun getAllReminders(): Flow<List<Reminder>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getPendingReminders(): Flow<List<Reminder>> {
        return queries.selectPending()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getPendingRemindersInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Reminder>> {
        return queries.selectPendingInRange(startDate.toString(), endDate.toString())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getRemindersForAnimal(animalId: String): Flow<List<Reminder>> {
        return queries.selectByAnimalId(animalId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getPendingRemindersForAnimal(animalId: String): Flow<List<Reminder>> {
        return queries.selectPendingByAnimalId(animalId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getReminderById(id: String): Flow<Reminder?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity -> entity?.toDomain() }
    }

    fun getRemindersByType(type: ReminderType): Flow<List<Reminder>> {
        return queries.selectByType(type.name)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getUpcomingReminders(maxDueDate: LocalDate, limit: Int = 10): Flow<List<Reminder>> {
        return queries.selectUpcoming(maxDueDate.toString(), limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getOverdueReminders(today: LocalDate): Flow<List<Reminder>> {
        return queries.selectOverdue(today.toString())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getRemindersBySourceEvent(eventId: String): Flow<List<Reminder>> {
        return queries.selectBySourceEvent(eventId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun insertReminder(
        animalId: String?,
        title: String,
        description: String?,
        reminderType: ReminderType,
        dueDate: LocalDate,
        isRecurring: Boolean = false,
        recurrenceIntervalDays: Int? = null,
        sourceEventId: String? = null,
        customData: ReminderData? = null
    ): String = withContext(Dispatchers.IO) {
        val id = uuid4().toString()
        val now = DateTimeUtil.nowIsoString()

        queries.insert(
            id = id,
            animal_id = animalId,
            title = title,
            description = description,
            reminder_type = reminderType.name,
            due_date = dueDate.toString(),
            is_completed = 0L,
            is_recurring = if (isRecurring) 1L else 0L,
            recurrence_interval_days = recurrenceIntervalDays?.toLong(),
            source_event_id = sourceEventId,
            custom_data = serializeReminderData(customData),
            created_at = now,
            updated_at = now
        )
        id
    }

    suspend fun updateReminder(
        id: String,
        title: String,
        description: String?,
        reminderType: ReminderType,
        dueDate: LocalDate,
        isRecurring: Boolean,
        recurrenceIntervalDays: Int?,
        customData: ReminderData?
    ) = withContext(Dispatchers.IO) {
        val now = DateTimeUtil.nowIsoString()

        queries.update(
            title = title,
            description = description,
            reminder_type = reminderType.name,
            due_date = dueDate.toString(),
            is_recurring = if (isRecurring) 1L else 0L,
            recurrence_interval_days = recurrenceIntervalDays?.toLong(),
            custom_data = serializeReminderData(customData),
            updated_at = now,
            id = id
        )
    }

    suspend fun markCompleted(id: String) = withContext(Dispatchers.IO) {
        val now = DateTimeUtil.nowIsoString()
        queries.markCompleted(now, id)
    }

    suspend fun markPending(id: String) = withContext(Dispatchers.IO) {
        val now = DateTimeUtil.nowIsoString()
        queries.markPending(now, id)
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        queries.delete(id)
    }

    suspend fun deleteRemindersForAnimal(animalId: String) = withContext(Dispatchers.IO) {
        queries.deleteByAnimal(animalId)
    }

    suspend fun deleteRemindersBySourceEvent(eventId: String) = withContext(Dispatchers.IO) {
        queries.deleteBySourceEvent(eventId)
    }

    suspend fun deleteCompletedOlderThan(date: LocalDate) = withContext(Dispatchers.IO) {
        queries.deleteCompletedOlderThan(date.toString())
    }

    suspend fun countPending(): Long = withContext(Dispatchers.IO) {
        queries.countPending().executeAsOne()
    }

    suspend fun countOverdue(today: LocalDate): Long = withContext(Dispatchers.IO) {
        queries.countOverdue(today.toString()).executeAsOne()
    }
}
