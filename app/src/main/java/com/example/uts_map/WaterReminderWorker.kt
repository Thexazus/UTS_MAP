package com.example.uts_map

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WaterReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        // Tampilkan notifikasi
        NotificationUtils.showNotification(
            applicationContext,
            "Water Reminder",
            "Don't forget to drink water to stay hydrated!"
        )
        return Result.success()
    }
}
