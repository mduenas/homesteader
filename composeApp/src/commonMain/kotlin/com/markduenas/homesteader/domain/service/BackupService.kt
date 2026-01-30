package com.markduenas.homesteader.domain.service

import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.data.repository.ReminderRepository
import com.markduenas.homesteader.data.repository.SpeciesConfigRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.SpeciesConfig
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val version: Int = CURRENT_BACKUP_VERSION,
    val createdAt: String,
    val animals: List<Animal>,
    val events: List<AnimalEvent>,
    val reminders: List<Reminder>,
    val speciesConfigs: List<SpeciesConfig>
) {
    companion object {
        const val CURRENT_BACKUP_VERSION = 1
    }
}

class BackupService(
    private val animalRepository: AnimalRepository,
    private val eventRepository: EventRepository,
    private val reminderRepository: ReminderRepository,
    private val speciesConfigRepository: SpeciesConfigRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun createBackup(): String {
        val animals = animalRepository.getAllAnimals().first()
        val events = eventRepository.getRecentEvents(10000).first()
        val reminders = reminderRepository.getAllReminders().first()
        val speciesConfigs = speciesConfigRepository.getAllConfigs().first()

        val backupData = BackupData(
            createdAt = DateTimeUtil.nowIsoString(),
            animals = animals,
            events = events,
            reminders = reminders,
            speciesConfigs = speciesConfigs
        )

        return json.encodeToString(backupData)
    }

    suspend fun restoreBackup(backupJson: String): RestoreResult {
        return try {
            val backupData = json.decodeFromString<BackupData>(backupJson)

            if (backupData.version > BackupData.CURRENT_BACKUP_VERSION) {
                return RestoreResult.Error("Backup version ${backupData.version} is not supported. Please update the app.")
            }

            // Restore species configs first (animals depend on them)
            var speciesRestored = 0
            backupData.speciesConfigs.forEach { config ->
                try {
                    speciesConfigRepository.insertConfig(config)
                    speciesRestored++
                } catch (e: Exception) {
                    // Skip on error (might be duplicate)
                }
            }

            // Restore animals
            var animalsRestored = 0
            backupData.animals.forEach { animal ->
                try {
                    animalRepository.insertAnimal(animal)
                    animalsRestored++
                } catch (e: Exception) {
                    // Skip on error (might be duplicate)
                }
            }

            // Restore events
            var eventsRestored = 0
            backupData.events.forEach { event ->
                try {
                    eventRepository.insertEvent(event)
                    eventsRestored++
                } catch (e: Exception) {
                    // Skip on error
                }
            }

            // Restore reminders
            var remindersRestored = 0
            backupData.reminders.forEach { reminder ->
                try {
                    reminderRepository.insertReminder(
                        animalId = reminder.animalId,
                        title = reminder.title,
                        description = reminder.description,
                        reminderType = reminder.reminderType,
                        dueDate = reminder.dueDate,
                        isRecurring = reminder.isRecurring,
                        recurrenceIntervalDays = reminder.recurrenceIntervalDays,
                        sourceEventId = reminder.sourceEventId,
                        customData = reminder.customData
                    )
                    remindersRestored++
                } catch (e: Exception) {
                    // Skip on error
                }
            }

            RestoreResult.Success(
                animalsRestored = animalsRestored,
                eventsRestored = eventsRestored,
                remindersRestored = remindersRestored,
                speciesConfigsRestored = speciesRestored
            )
        } catch (e: Exception) {
            RestoreResult.Error("Failed to restore backup: ${e.message}")
        }
    }

    fun generateBackupFilename(): String {
        val timestamp = DateTimeUtil.nowIsoString()
            .replace(":", "-")
            .replace("T", "_")
            .substringBefore(".")
        return "homesteader_backup_$timestamp.json"
    }
}

sealed class RestoreResult {
    data class Success(
        val animalsRestored: Int,
        val eventsRestored: Int,
        val remindersRestored: Int,
        val speciesConfigsRestored: Int
    ) : RestoreResult()

    data class Error(val message: String) : RestoreResult()
}
