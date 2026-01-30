package com.markduenas.homesteader.domain.notification

import com.markduenas.homesteader.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Platform-agnostic notification service interface.
 * Handles scheduling and managing local notifications for reminders.
 */
interface NotificationService {
    /**
     * Whether notification permissions have been granted.
     */
    val hasPermission: StateFlow<Boolean>

    /**
     * Request notification permissions from the user.
     * @return true if permissions were granted
     */
    suspend fun requestPermission(): Boolean

    /**
     * Schedule a notification for a reminder.
     * @param reminder The reminder to schedule a notification for
     * @param notifyTime The time of day to show the notification (default: 8:00 AM)
     */
    suspend fun scheduleNotification(
        reminder: Reminder,
        notifyTime: LocalTime = LocalTime(8, 0)
    )

    /**
     * Cancel a scheduled notification.
     * @param reminderId The ID of the reminder whose notification should be cancelled
     */
    suspend fun cancelNotification(reminderId: String)

    /**
     * Cancel all scheduled notifications.
     */
    suspend fun cancelAllNotifications()

    /**
     * Reschedule all pending reminders.
     * Called on app startup to ensure notifications are scheduled.
     */
    suspend fun rescheduleAllReminders(reminders: List<Reminder>)

    /**
     * Check if a notification is scheduled for a reminder.
     */
    suspend fun isNotificationScheduled(reminderId: String): Boolean
}

/**
 * Notification data for display.
 */
data class NotificationData(
    val id: String,
    val title: String,
    val body: String,
    val scheduledDate: LocalDate,
    val scheduledTime: LocalTime
)

/**
 * Notification channel IDs for Android.
 */
object NotificationChannels {
    const val REMINDERS = "homesteader_reminders"
    const val BREEDING = "homesteader_breeding"
    const val HEALTH = "homesteader_health"
}
