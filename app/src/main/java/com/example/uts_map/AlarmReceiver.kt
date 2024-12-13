package com.example.uts_map

import android.Manifest
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
    private var ringtone: Ringtone? = null // Simpan instance ringtone

    override fun onReceive(context: Context, intent: Intent) {
        // Start the AlarmService to play the alarm sound
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.startService(serviceIntent)

        // Get the reminder time from intent
        val reminderTime = intent.getStringExtra("reminderTime") ?: "Time not set"

        // Create notification channel for Android 8.0 (API 26) and above
        createNotificationChannel(context)

        // Play alarm sound using the singleton
        RingtoneManagerSingleton.playRingtone(context)

        // Get default alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Create an intent to stop the alarm
        val stopIntent = Intent(context, AlarmStopReceiver::class.java).apply {
            putExtra("reminderTime", reminderTime)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
            .addAction(R.drawable.ic_stop, "Stop Alarm", stopPendingIntent) // Add stop action
            .setDeleteIntent(stopPendingIntent) // Set delete intent for swipe-to-dismiss

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
                ringtone = RingtoneManager.getRingtone(context, alarmSound)
                ringtone?.play()
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

    // Method to stop the ringtone
    fun stopRingtone() {
        ringtone?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
    }
}