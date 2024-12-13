package com.example.uts_map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationManagerCompat

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Stop the AlarmService
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.stopService(serviceIntent)

        // Stop the ringtone if it's still playing
        val alarmReceiver = AlarmReceiver()
        alarmReceiver.stopRingtone()

        // Optionally, you can also cancel the notification if needed
        NotificationManagerCompat.from(context).cancelAll()
    }
}