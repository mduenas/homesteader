package com.markduenas.homesteader.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * A reminder/task for animal management
 */
@Serializable
data class Reminder(
    val id: String = "",
    val animalId: String? = null,
    val title: String,
    val description: String? = null,
    val reminderType: ReminderType,
    val dueDate: LocalDate,
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceIntervalDays: Int? = null,
    val sourceEventId: String? = null,
    val customData: ReminderData? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Types of reminders
 */
enum class ReminderType(val displayName: String) {
    // Breeding related
    HEAT_EXPECTED("Heat Expected"),
    PREGNANCY_CHECK("Pregnancy Check Due"),
    BIRTH_DUE("Birth Due"),
    WEANING_DUE("Weaning Due"),

    // Health related
    VACCINATION_DUE("Vaccination Due"),
    DEWORMING_DUE("Deworming Due"),
    VET_FOLLOWUP("Vet Follow-up"),
    MEDICATION_DUE("Medication Due"),
    HOOF_TRIM_DUE("Hoof Trim Due"),

    // General
    CUSTOM("Custom Reminder"),
    RECURRING_TASK("Recurring Task");

    companion object {
        fun fromString(value: String): ReminderType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CUSTOM
        }
    }
}

/**
 * Additional data for specific reminder types
 */
@Serializable
data class ReminderData(
    val sireId: String? = null,
    val sireName: String? = null,
    val expectedOffspring: Int? = null,
    val vaccineName: String? = null,
    val medicationName: String? = null,
    val notes: String? = null
)

/**
 * Helper object for calculating reminder dates from events
 */
object ReminderCalculator {

    /**
     * Calculate pregnancy check date (typically 30-45 days after breeding)
     */
    fun calculatePregnancyCheckDate(breedingDate: LocalDate, daysAfterBreeding: Int = 35): LocalDate {
        return LocalDate.fromEpochDays(breedingDate.toEpochDays() + daysAfterBreeding)
    }

    /**
     * Calculate expected birth date based on species gestation period
     */
    fun calculateBirthDueDate(breedingDate: LocalDate, gestationDays: Int): LocalDate {
        return LocalDate.fromEpochDays(breedingDate.toEpochDays() + gestationDays)
    }

    /**
     * Calculate weaning due date based on birth date and species weaning age
     */
    fun calculateWeaningDueDate(birthDate: LocalDate, weaningAgeDays: Int): LocalDate {
        return LocalDate.fromEpochDays(birthDate.toEpochDays() + weaningAgeDays)
    }

    /**
     * Calculate expected next heat date based on last heat and cycle length
     */
    fun calculateNextHeatDate(lastHeatDate: LocalDate, cycleDays: Int): LocalDate {
        return LocalDate.fromEpochDays(lastHeatDate.toEpochDays() + cycleDays)
    }

    /**
     * Calculate next vaccination due date
     */
    fun calculateNextVaccinationDate(lastVaccinationDate: LocalDate, intervalDays: Int): LocalDate {
        return LocalDate.fromEpochDays(lastVaccinationDate.toEpochDays() + intervalDays)
    }
}
