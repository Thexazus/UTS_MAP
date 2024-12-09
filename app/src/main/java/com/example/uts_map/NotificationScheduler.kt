package com.example.uts_map

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class NotificationScheduler {
    companion object {
        fun scheduleNotifications(context: Context) {
            val notificationTimes = listOf(9, 12, 15, 18) // Jam dalam format 24 jam

            for (hour in notificationTimes) {
                val delay = calculateInitialDelay(hour)
                val workRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .addTag("WaterReminder_$hour")
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "WaterReminder_$hour",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }

        private fun calculateInitialDelay(hour: Int): Long {
            val now = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = now
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) {
                    add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
            }
            return calendar.timeInMillis - now
        }
    }
}
