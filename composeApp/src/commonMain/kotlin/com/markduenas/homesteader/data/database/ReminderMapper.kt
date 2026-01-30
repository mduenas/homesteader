package com.markduenas.homesteader.data.database

import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderData
import com.markduenas.homesteader.domain.model.ReminderType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        animalId = animal_id,
        title = title,
        description = description,
        reminderType = ReminderType.fromString(reminder_type),
        dueDate = LocalDate.parse(due_date),
        isCompleted = is_completed == 1L,
        isRecurring = is_recurring == 1L,
        recurrenceIntervalDays = recurrence_interval_days?.toInt(),
        sourceEventId = source_event_id,
        customData = custom_data?.let { parseReminderData(it) },
        createdAt = created_at,
        updatedAt = updated_at
    )
}

private fun parseReminderData(jsonString: String): ReminderData? {
    return try {
        json.decodeFromString<ReminderData>(jsonString)
    } catch (e: Exception) {
        null
    }
}

fun serializeReminderData(data: ReminderData?): String? {
    return data?.let {
        try {
            json.encodeToString(it)
        } catch (e: Exception) {
            null
        }
    }
}
