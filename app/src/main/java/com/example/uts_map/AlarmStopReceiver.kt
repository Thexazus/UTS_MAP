package com.example.uts_map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Log untuk debugging
        Log.d("AlarmStopReceiver", "Stop alarm received")

        try {
            // Stop ringtone
            RingtoneManagerSingleton.stopRingtone()

            // Cancel notification
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancelAll()

        } catch (e: Exception) {
            Log.e("AlarmStopReceiver", "Error stopping alarm", e)
            Toast.makeText(context, "Error stopping alarm", Toast.LENGTH_SHORT).show()
        }
    }
}