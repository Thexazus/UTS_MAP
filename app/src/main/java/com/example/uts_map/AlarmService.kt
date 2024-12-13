package com.example.uts_map

import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.IBinder

class AlarmService : Service() {

    private var ringtone: Ringtone? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Play alarm sound using the singleton
        RingtoneManagerSingleton.playRingtone(applicationContext)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        RingtoneManagerSingleton.stopRingtone()
    }

    private fun stopRingtone() {
        ringtone?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}