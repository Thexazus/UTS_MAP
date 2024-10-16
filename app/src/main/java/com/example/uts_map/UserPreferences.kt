package com.example.uts_map

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREF_NAME = "UserPreferences"
    private const val KEY_HEIGHT = "height"
    private const val KEY_WEIGHT = "weight"
    private const val KEY_AGE = "age"
    private const val KEY_GENDER = "gender"
    private const val KEY_DAILY_INTAKE = "daily_intake"
    private const val KEY_SLEEPING_TIME = "sleeping_time"
    private const val KEY_WAKE_UP_TIME = "wake_up_time"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getHeight(context: Context): Int = getPreferences(context).getInt(KEY_HEIGHT, 170)
    fun setHeight(context: Context, height: Int) = getPreferences(context).edit().putInt(KEY_HEIGHT, height).apply()

    fun getWeight(context: Context): Int = getPreferences(context).getInt(KEY_WEIGHT, 70)
    fun setWeight(context: Context, weight: Int) = getPreferences(context).edit().putInt(KEY_WEIGHT, weight).apply()

    fun getAge(context: Context): Int = getPreferences(context).getInt(KEY_AGE, 25)
    fun setAge(context: Context, age: Int) = getPreferences(context).edit().putInt(KEY_AGE, age).apply()

    fun getGender(context: Context): String = getPreferences(context).getString(KEY_GENDER, "Other") ?: "Other"
    fun setGender(context: Context, gender: String) = getPreferences(context).edit().putString(KEY_GENDER, gender).apply()

    fun getDailyIntakeGoal(context: Context): Int = getPreferences(context).getInt(KEY_DAILY_INTAKE, 2000)
    fun setDailyIntakeGoal(context: Context, intake: Int) = getPreferences(context).edit().putInt(KEY_DAILY_INTAKE, intake).apply()

    fun getSleepingTime(context: Context): String = getPreferences(context).getString(KEY_SLEEPING_TIME, "22:00") ?: "22:00"
    fun setSleepingTime(context: Context, time: String) = getPreferences(context).edit().putString(KEY_SLEEPING_TIME, time).apply()

    fun getWakeUpTime(context: Context): String = getPreferences(context).getString(KEY_WAKE_UP_TIME, "06:00") ?: "06:00"
    fun setWakeUpTime(context: Context, time: String) = getPreferences(context).edit().putString(KEY_WAKE_UP_TIME, time).apply()
}