package com.example.uts_map

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.uts_map.R

class AlarmReceiver : BroadcastReceiver() {

    private val channelId = "reminder_channel"
    private val channelName = "Reminder Notifications"

    override fun onReceive(context: Context, intent: Intent) {
        // Get the reminder time from intent
        val reminderTime = intent.getStringExtra("reminderTime") ?: "Time not set"

        // Create notification channel for Android 8.0 (API 26) and above
        createNotificationChannel(context)

        // Get default alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Create notification builder
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.set_notification) // Replace with your own icon
            .setContentTitle("Reminder Alarm")
            .setContentText("It's time for your reminder at $reminderTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))

        try {
            // Check for notification permission (especially on Android 13+)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Post notification with unique ID
                NotificationManagerCompat.from(context).notify(
                    System.currentTimeMillis().toInt(),
                    builder.build()
                )

                // Play alarm sound
                val ringtone = RingtoneManager.getRingtone(context, alarmSound)
                ringtone.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function to create notification channel for Android 8.0 and above
    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for reminder alarms"
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
