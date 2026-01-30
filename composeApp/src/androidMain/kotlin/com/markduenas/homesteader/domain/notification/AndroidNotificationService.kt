package com.markduenas.homesteader.domain.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.markduenas.homesteader.MainActivity
import com.markduenas.homesteader.R
import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Android implementation of NotificationService using WorkManager.
 */
class AndroidNotificationService(
    private val context: Context
) : NotificationService {

    private val workManager = WorkManager.getInstance(context)
    private val notificationManager = NotificationManagerCompat.from(context)

    private val _hasPermission = MutableStateFlow(checkPermission())
    override val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        createNotificationChannels()
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Reminders channel
            val remindersChannel = NotificationChannel(
                NotificationChannels.REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General reminder notifications"
            }

            // Breeding channel (higher priority)
            val breedingChannel = NotificationChannel(
                NotificationChannels.BREEDING,
                "Breeding Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Breeding-related notifications like births and heat cycles"
            }

            // Health channel
            val healthChannel = NotificationChannel(
                NotificationChannels.HEALTH,
                "Health Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Health-related notifications like vaccinations"
            }

            manager.createNotificationChannels(listOf(remindersChannel, breedingChannel, healthChannel))
        }
    }

    override suspend fun requestPermission(): Boolean {
        // Permission request is handled by the Activity
        // This just rechecks the current status
        _hasPermission.value = checkPermission()
        return _hasPermission.value
    }

    override suspend fun scheduleNotification(reminder: Reminder, notifyTime: LocalTime) {
        if (!_hasPermission.value) return

        val now = kotlinx.datetime.Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val nowLocal = now.toLocalDateTime(timeZone)

        // Calculate the notification time
        val notificationDateTime = reminder.dueDate.atTime(notifyTime)
        val notificationInstant = notificationDateTime.toInstant(timeZone)

        // Calculate delay in milliseconds
        val delayMillis = notificationInstant.toEpochMilliseconds() - now.toEpochMilliseconds()

        // Don't schedule if it's in the past
        if (delayMillis <= 0) return

        val channelId = getChannelForReminderType(reminder.reminderType)

        val data = Data.Builder()
            .putString(KEY_REMINDER_ID, reminder.id)
            .putString(KEY_TITLE, reminder.title)
            .putString(KEY_BODY, reminder.description ?: getDefaultBody(reminder))
            .putString(KEY_CHANNEL_ID, channelId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(WORK_TAG_PREFIX + reminder.id)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME_PREFIX + reminder.id,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override suspend fun cancelNotification(reminderId: String) {
        workManager.cancelUniqueWork(WORK_NAME_PREFIX + reminderId)
    }

    override suspend fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(WORK_TAG_PREFIX)
    }

    override suspend fun rescheduleAllReminders(reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            if (!reminder.isCompleted) {
                scheduleNotification(reminder)
            }
        }
    }

    override suspend fun isNotificationScheduled(reminderId: String): Boolean {
        val workInfo = workManager.getWorkInfosForUniqueWork(WORK_NAME_PREFIX + reminderId).get()
        return workInfo.isNotEmpty() && !workInfo.first().state.isFinished
    }

    private fun getChannelForReminderType(type: ReminderType): String {
        return when (type) {
            ReminderType.HEAT_EXPECTED,
            ReminderType.PREGNANCY_CHECK,
            ReminderType.BIRTH_DUE,
            ReminderType.WEANING_DUE -> NotificationChannels.BREEDING

            ReminderType.VACCINATION_DUE,
            ReminderType.DEWORMING_DUE,
            ReminderType.VET_FOLLOWUP,
            ReminderType.MEDICATION_DUE,
            ReminderType.HOOF_TRIM_DUE -> NotificationChannels.HEALTH

            ReminderType.CUSTOM,
            ReminderType.RECURRING_TASK -> NotificationChannels.REMINDERS
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

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_CHANNEL_ID = "channel_id"
        const val WORK_TAG_PREFIX = "reminder_notification_"
        const val WORK_NAME_PREFIX = "reminder_"
    }
}

/**
 * WorkManager worker that shows the notification.
 */
class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val reminderId = inputData.getString(AndroidNotificationService.KEY_REMINDER_ID) ?: return Result.failure()
        val title = inputData.getString(AndroidNotificationService.KEY_TITLE) ?: "Reminder"
        val body = inputData.getString(AndroidNotificationService.KEY_BODY) ?: ""
        val channelId = inputData.getString(AndroidNotificationService.KEY_CHANNEL_ID) ?: NotificationChannels.REMINDERS

        showNotification(reminderId, title, body, channelId)
        return Result.success()
    }

    private fun showNotification(id: String, title: String, body: String, channelId: String) {
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", id)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notification)
    }
}
