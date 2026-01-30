package com.markduenas.homesteader.domain.service

import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.repository.ReminderRepository
import com.markduenas.homesteader.data.repository.SpeciesConfigRepository
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.BreedingEventData
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.HealthEventData
import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderCalculator
import com.markduenas.homesteader.domain.model.ReminderData
import com.markduenas.homesteader.domain.model.ReminderType
import com.markduenas.homesteader.domain.notification.NotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val speciesConfigRepository: SpeciesConfigRepository,
    private val notificationService: NotificationService? = null
) {

    /**
     * Get upcoming reminders for the next N days
     */
    fun getUpcomingReminders(days: Int = 7, limit: Int = 10): Flow<List<Reminder>> {
        val today = DateTimeUtil.today()
        val endDate = today.plus(DatePeriod(days = days))
        return reminderRepository.getUpcomingReminders(endDate, limit)
    }

    /**
     * Get overdue reminders
     */
    fun getOverdueReminders(): Flow<List<Reminder>> {
        val today = DateTimeUtil.today()
        return reminderRepository.getOverdueReminders(today)
    }

    /**
     * Get pending reminders for an animal
     */
    fun getPendingRemindersForAnimal(animalId: String): Flow<List<Reminder>> {
        return reminderRepository.getPendingRemindersForAnimal(animalId)
    }

    /**
     * Generate automatic reminders from a breeding event
     */
    suspend fun generateBreedingReminders(
        event: AnimalEvent,
        animalName: String,
        speciesKey: String
    ) {
        if (event.eventType != EventType.BRED) return

        val config = speciesConfigRepository.getConfigByKey(speciesKey).first() ?: return
        val breedingDate = event.eventDate

        // Delete any existing reminders from this event
        reminderRepository.deleteRemindersBySourceEvent(event.id)

        val sireInfo = event.eventData?.let {
            if (it is BreedingEventData) {
                Pair(it.sireId, it.sireName)
            } else null
        }

        // 1. Pregnancy Check Reminder (35 days after breeding by default)
        val pregnancyCheckDate = ReminderCalculator.calculatePregnancyCheckDate(breedingDate)
        reminderRepository.insertReminder(
            animalId = event.animalId,
            title = "Pregnancy Check Due",
            description = "Check $animalName for pregnancy - bred on $breedingDate",
            reminderType = ReminderType.PREGNANCY_CHECK,
            dueDate = pregnancyCheckDate,
            sourceEventId = event.id,
            customData = ReminderData(
                sireId = sireInfo?.first,
                sireName = sireInfo?.second,
                notes = "Days since breeding: 35"
            )
        )

        // 2. Birth Due Reminder (based on species gestation period)
        val gestationDays = config.gestationDays
        if (gestationDays != null && gestationDays > 0) {
            val birthDueDate = ReminderCalculator.calculateBirthDueDate(breedingDate, gestationDays)
            reminderRepository.insertReminder(
                animalId = event.animalId,
                title = "Birth Due",
                description = "$animalName expected to give birth",
                reminderType = ReminderType.BIRTH_DUE,
                dueDate = birthDueDate,
                sourceEventId = event.id,
                customData = ReminderData(
                    sireId = sireInfo?.first,
                    sireName = sireInfo?.second,
                    notes = "Gestation: $gestationDays days"
                )
            )
        }
    }

    /**
     * Generate automatic reminders from a birth event
     */
    suspend fun generateBirthReminders(
        event: AnimalEvent,
        animalName: String,
        speciesKey: String
    ) {
        if (event.eventType != EventType.BIRTH) return

        val config = speciesConfigRepository.getConfigByKey(speciesKey).first() ?: return
        val birthDate = event.eventDate

        // Delete any existing reminders from this event
        reminderRepository.deleteRemindersBySourceEvent(event.id)

        // 1. Weaning Due Reminder (based on species weaning age)
        val weaningAgeDays = config.weaningAgeDays
        if (weaningAgeDays != null && weaningAgeDays > 0) {
            val weaningDueDate = ReminderCalculator.calculateWeaningDueDate(birthDate, weaningAgeDays)
            reminderRepository.insertReminder(
                animalId = event.animalId,
                title = "Weaning Due",
                description = "Offspring of $animalName ready to wean",
                reminderType = ReminderType.WEANING_DUE,
                dueDate = weaningDueDate,
                sourceEventId = event.id,
                customData = ReminderData(
                    notes = "Birth date: $birthDate, Weaning age: $weaningAgeDays days"
                )
            )
        }
    }

    /**
     * Generate automatic reminders from a heat event
     */
    suspend fun generateHeatReminders(
        event: AnimalEvent,
        animalName: String,
        speciesKey: String
    ) {
        if (event.eventType != EventType.HEAT_OBSERVED) return

        val config = speciesConfigRepository.getConfigByKey(speciesKey).first() ?: return
        val heatDate = event.eventDate

        // Delete any existing reminders from this event
        reminderRepository.deleteRemindersBySourceEvent(event.id)

        // Next Heat Expected Reminder (based on species heat cycle)
        val heatCycleDays = config.heatCycleDays
        if (heatCycleDays != null && heatCycleDays > 0) {
            val nextHeatDate = ReminderCalculator.calculateNextHeatDate(heatDate, heatCycleDays)
            reminderRepository.insertReminder(
                animalId = event.animalId,
                title = "Heat Expected",
                description = "$animalName expected to be in heat",
                reminderType = ReminderType.HEAT_EXPECTED,
                dueDate = nextHeatDate,
                sourceEventId = event.id,
                customData = ReminderData(
                    notes = "Last heat: $heatDate, Cycle: $heatCycleDays days"
                )
            )
        }
    }

    /**
     * Generate automatic reminders from a vaccination event
     */
    suspend fun generateVaccinationReminders(
        event: AnimalEvent,
        animalName: String,
        nextDueInDays: Int? = null
    ) {
        if (event.eventType != EventType.VACCINATION) return

        // Only create follow-up if interval is provided
        val intervalDays = nextDueInDays ?: return

        val vaccinationDate = event.eventDate

        // Delete any existing reminders from this event
        reminderRepository.deleteRemindersBySourceEvent(event.id)

        val vaccineInfo = event.eventData?.let {
            if (it is HealthEventData) {
                it.medicationName
            } else null
        }

        val nextVaccinationDate = ReminderCalculator.calculateNextVaccinationDate(
            vaccinationDate,
            intervalDays
        )
        reminderRepository.insertReminder(
            animalId = event.animalId,
            title = "Vaccination Due",
            description = vaccineInfo?.let { "Revaccinate $animalName with $it" }
                ?: "Revaccination due for $animalName",
            reminderType = ReminderType.VACCINATION_DUE,
            dueDate = nextVaccinationDate,
            isRecurring = true,
            recurrenceIntervalDays = intervalDays,
            sourceEventId = event.id,
            customData = ReminderData(
                vaccineName = vaccineInfo,
                notes = "Last vaccination: $vaccinationDate"
            )
        )
    }

    /**
     * Create a custom reminder
     */
    suspend fun createCustomReminder(
        animalId: String?,
        title: String,
        description: String?,
        dueDate: LocalDate,
        isRecurring: Boolean = false,
        recurrenceIntervalDays: Int? = null
    ): String {
        return reminderRepository.insertReminder(
            animalId = animalId,
            title = title,
            description = description,
            reminderType = ReminderType.CUSTOM,
            dueDate = dueDate,
            isRecurring = isRecurring,
            recurrenceIntervalDays = recurrenceIntervalDays
        )
    }

    /**
     * Mark a reminder as completed
     * If recurring, create the next occurrence
     */
    suspend fun completeReminder(reminder: Reminder) {
        reminderRepository.markCompleted(reminder.id)

        // If recurring, create next occurrence
        if (reminder.isRecurring && reminder.recurrenceIntervalDays != null) {
            val nextDueDate = reminder.dueDate.plus(DatePeriod(days = reminder.recurrenceIntervalDays))
            reminderRepository.insertReminder(
                animalId = reminder.animalId,
                title = reminder.title,
                description = reminder.description,
                reminderType = reminder.reminderType,
                dueDate = nextDueDate,
                isRecurring = true,
                recurrenceIntervalDays = reminder.recurrenceIntervalDays,
                sourceEventId = reminder.sourceEventId,
                customData = reminder.customData
            )
        }
    }

    /**
     * Delete a reminder
     */
    suspend fun deleteReminder(id: String) {
        reminderRepository.deleteReminder(id)
    }

    /**
     * Cleanup old completed reminders
     */
    suspend fun cleanupOldReminders(daysOld: Int = 30) {
        val cutoffDate = DateTimeUtil.today().plus(DatePeriod(days = -daysOld))
        reminderRepository.deleteCompletedOlderThan(cutoffDate)
    }

    /**
     * Schedule notifications for all pending reminders.
     * Should be called on app startup.
     */
    suspend fun scheduleAllNotifications() {
        val pendingReminders = reminderRepository.getPendingReminders().first()
        notificationService?.rescheduleAllReminders(pendingReminders)
    }

    /**
     * Schedule a notification for a specific reminder.
     */
    private suspend fun scheduleNotification(reminder: Reminder) {
        notificationService?.scheduleNotification(reminder)
    }

    /**
     * Cancel notification for a specific reminder.
     */
    private suspend fun cancelNotification(reminderId: String) {
        notificationService?.cancelNotification(reminderId)
    }

    /**
     * Request notification permissions.
     * @return true if permissions were granted
     */
    suspend fun requestNotificationPermission(): Boolean {
        return notificationService?.requestPermission() ?: false
    }

    /**
     * Check if notification permissions are granted.
     */
    fun hasNotificationPermission(): Boolean {
        return notificationService?.hasPermission?.value ?: false
    }
}
