package com.example.uts_map

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager

object RingtoneManagerSingleton {
    @Volatile
    private var ringtone: Ringtone? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmSound)
            isInitialized = true
        }
    }

    fun playRingtone(context: Context) {
        initialize(context)
        ringtone?.play()
    }

    fun stopRingtone() {
        ringtone?.let {
            if (it.isPlaying) {
                try {
                    it.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}