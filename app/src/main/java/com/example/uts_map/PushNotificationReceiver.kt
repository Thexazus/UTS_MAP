package com.example.uts_map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.uts_map.NotificationUtils.showNotification

class PushNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Tampilkan notifikasi
        showNotification(
            context,
            "Water Reminder",
            "Don't forget to drink water to stay hydrated!"
        )
    }
}
