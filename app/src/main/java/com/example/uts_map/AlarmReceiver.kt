package com.example.uts_map

import android.Manifest
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
    override fun onReceive(context: Context, intent: Intent) {
        // Get the reminder time from intent
        val reminderTime = intent.getStringExtra("reminderTime") ?: "Time not set"

        // Get default alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Create notification builder
        val builder = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.set_notification)
            .setContentTitle("Reminder Alarm")
            .setContentText("It's time for your reminder at $reminderTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))

        try {
            // Check for notification permission
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
}