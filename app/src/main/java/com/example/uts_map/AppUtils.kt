package com.example.uts_map
import android.os.Build
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AppUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun getCurrentDate(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.now().format(dateFormatter)
    } else {
        TODO("VERSION.SDK_INT < O")
    }
}