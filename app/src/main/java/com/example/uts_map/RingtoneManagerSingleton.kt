package com.example.uts_map

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager

object RingtoneManagerSingleton {
    private var ringtone: Ringtone? = null

    fun playRingtone(context: Context) {
        if (ringtone == null) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmSound)
        }
        ringtone?.play()
    }

    fun stopRingtone() {
        ringtone?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
    }
}