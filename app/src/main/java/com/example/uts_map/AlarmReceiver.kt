package com.example.uts_map

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.uts_map.R

class AlarmReceiver : BroadcastReceiver() {
    private val channelId = "reminder_channel"
    private val channelName = "Reminder Notifications"

    override fun onReceive(context: Context, intent: Intent) {
        val reminderTime = intent.getStringExtra("reminderTime") ?: "Time not set"

        // Create notification channel
        createNotificationChannel(context)

        // Play alarm sound
        RingtoneManagerSingleton.playRingtone(context)

        // Create and show notification
        showNotification(context, reminderTime)
    }

    // Di AlarmReceiver.kt
    private fun showNotification(context: Context, reminderTime: String) {
        val stopIntent = Intent(context, AlarmStopReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderTime", reminderTime)
            putExtra("alarmId", System.currentTimeMillis().toInt())
        }

        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            stopIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // Gunakan FLAG_ONE_SHOT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.set_notification)
            .setContentTitle("Reminder Alarm")
            .setContentText("It's time for your reminder at $reminderTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true) // Tambahkan ini agar notifikasi tidak bisa di-swipe
            .addAction(
                R.drawable.ic_stop,
                "Stop Alarm",
                stopPendingIntent
            )

        // Tambahkan full screen intent untuk memastikan notifikasi bisa diinteraksi
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(channelId)
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(
                    System.currentTimeMillis().toInt(),
                    builder.build()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Pastikan channel notification dibuat dengan benar
    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for reminder alarms"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Tambahkan ini
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}