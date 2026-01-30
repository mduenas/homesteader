package com.markduenas.homesteader.domain.notification

import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalTime
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * iOS implementation of NotificationService using UNUserNotificationCenter.
 */
class IosNotificationService : NotificationService {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    private val _hasPermission = MutableStateFlow(false)
    override val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        checkPermission()
    }

    private fun checkPermission() {
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            _hasPermission.value = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
        }
    }

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        notificationCenter.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            _hasPermission.value = granted
            if (continuation.isActive) {
                continuation.resume(granted)
            }
        }
    }

    override suspend fun scheduleNotification(reminder: Reminder, notifyTime: LocalTime) {
        if (!_hasPermission.value) return

        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title)
            setBody(reminder.description ?: getDefaultBody(reminder))
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
            setCategoryIdentifier(getCategoryForReminderType(reminder.reminderType))
        }

        // Create date components for the trigger
        val dateComponents = NSDateComponents().apply {
            year = reminder.dueDate.year.toLong()
            month = reminder.dueDate.monthNumber.toLong()
            day = reminder.dueDate.dayOfMonth.toLong()
            hour = notifyTime.hour.toLong()
            minute = notifyTime.minute.toLong()
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = reminder.id,
            content = content,
            trigger = trigger
        )

        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Failed to schedule notification: ${error.localizedDescription}")
            }
        }
    }

    override suspend fun cancelNotification(reminderId: String) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(reminderId))
    }

    override suspend fun cancelAllNotifications() {
        notificationCenter.removeAllPendingNotificationRequests()
    }

    override suspend fun rescheduleAllReminders(reminders: List<Reminder>) {
        // Cancel all existing
        cancelAllNotifications()

        // Reschedule active reminders
        reminders.forEach { reminder ->
            if (!reminder.isCompleted) {
                scheduleNotification(reminder)
            }
        }
    }

    override suspend fun isNotificationScheduled(reminderId: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            notificationCenter.getPendingNotificationRequestsWithCompletionHandler { requests ->
                val isScheduled = requests?.any { request ->
                    (request as? UNNotificationRequest)?.identifier == reminderId
                } ?: false
                if (continuation.isActive) {
                    continuation.resume(isScheduled)
                }
            }
        }

    private fun getCategoryForReminderType(type: ReminderType): String {
        return when (type) {
            ReminderType.HEAT_EXPECTED,
            ReminderType.PREGNANCY_CHECK,
            ReminderType.BIRTH_DUE,
            ReminderType.WEANING_DUE -> "breeding"

            ReminderType.VACCINATION_DUE,
            ReminderType.DEWORMING_DUE,
            ReminderType.VET_FOLLOWUP,
            ReminderType.MEDICATION_DUE,
            ReminderType.HOOF_TRIM_DUE -> "health"

            ReminderType.CUSTOM,
            ReminderType.RECURRING_TASK -> "general"
        }
    }

    private fun getDefaultBody(reminder: Reminder): String {
        return when (reminder.reminderType) {
            ReminderType.HEAT_EXPECTED -> "Check for signs of heat"
            ReminderType.PREGNANCY_CHECK -> "Time to check for pregnancy"
            ReminderType.BIRTH_DUE -> "Prepare for expected birth"
            ReminderType.WEANING_DUE -> "Offspring ready for weaning"
            ReminderType.VACCINATION_DUE -> "Vaccination is due"
            ReminderType.DEWORMING_DUE -> "Deworming treatment due"
            ReminderType.VET_FOLLOWUP -> "Vet follow-up scheduled"
            ReminderType.MEDICATION_DUE -> "Medication is due"
            ReminderType.HOOF_TRIM_DUE -> "Hoof trim needed"
            ReminderType.CUSTOM, ReminderType.RECURRING_TASK -> "You have a reminder"
        }
    }
}
