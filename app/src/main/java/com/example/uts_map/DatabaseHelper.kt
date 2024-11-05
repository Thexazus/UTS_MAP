package com.example.uts_map

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.mindrot.jbcrypt.BCrypt

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "Users"
        private const val TABLE_WATER_INTAKE = "WaterIntake"

        // Database columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_FIRST_NAME = "first_name"
        private const val COLUMN_LAST_NAME = "last_name"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_SLEEPING_TIME = "sleeping_time"
        private const val COLUMN_WAKE_UP_TIME = "wake_up_time"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_TIMESTAMP = "timestamp"

        // SharedPreferences
        private const val PREF_NAME = "UserPrefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
        private const val KEY_CURRENT_USER_FIRST_NAME = "current_user_first_name"
        private const val KEY_CURRENT_USER_LAST_NAME = "current_user_last_name"
        private const val KEY_CURRENT_USER_FULL_NAME = "current_user_full_name"
        private const val KEY_DAILY_WATER_GOAL = "daily_water_goal"
        private const val KEY_LAST_LOGIN = "last_login"
        private const val KEY_SLEEPING_TIME = "sleeping_time"
        private const val KEY_WAKE_UP_TIME = "wake_up_time"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_FIRST_NAME TEXT,
                $COLUMN_LAST_NAME TEXT,
                $COLUMN_AGE INTEGER,
                $COLUMN_WEIGHT INTEGER,
                $COLUMN_HEIGHT INTEGER,
                $COLUMN_GENDER TEXT,
                $COLUMN_SLEEPING_TIME TEXT,
                $COLUMN_WAKE_UP_TIME TEXT
            )
        """.trimIndent()

        val createWaterIntakeTable = """
            CREATE TABLE $TABLE_WATER_INTAKE (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_AMOUNT INTEGER NOT NULL,
                $COLUMN_TIMESTAMP TEXT NOT NULL
            )
        """.trimIndent()

        db?.execSQL(createUserTable)
        db?.execSQL(createWaterIntakeTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_WATER_INTAKE")
        onCreate(db)
    }

    // SharedPreferences helper methods
    fun setUserName(firstName: String, lastName: String) {
        editor.apply {
            putString(KEY_CURRENT_USER_FIRST_NAME, firstName)
            putString(KEY_CURRENT_USER_LAST_NAME, lastName)
            putString(KEY_CURRENT_USER_FULL_NAME, "$firstName $lastName")
            apply()
        }
    }

    fun getCurrentUserFirstName(): String = sharedPreferences.getString(KEY_CURRENT_USER_FIRST_NAME, "") ?: ""

    fun getCurrentUserLastName(): String = sharedPreferences.getString(KEY_CURRENT_USER_LAST_NAME, "") ?: ""

    fun getCurrentUserFullName(): String = sharedPreferences.getString(KEY_CURRENT_USER_FULL_NAME, "") ?: ""

    fun setUserLoggedIn(email: String) {
        editor.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_CURRENT_USER_EMAIL, email)
            putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            apply()
        }
        // Fetch and save user name when logging in
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_FIRST_NAME, COLUMN_LAST_NAME),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME))
            if (firstName != null && lastName != null) {
                setUserName(firstName, lastName)
            }
        }
        cursor.close()
        db.close()
    }

    fun setUserLoggedOut() {
        editor.apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_CURRENT_USER_EMAIL)
            remove(KEY_CURRENT_USER_FIRST_NAME)
            remove(KEY_CURRENT_USER_LAST_NAME)
            remove(KEY_CURRENT_USER_FULL_NAME)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getCurrentUserEmail(): String? = sharedPreferences.getString(KEY_CURRENT_USER_EMAIL, null)

    fun setDailyWaterGoal(goal: Int) {
        editor.putInt(KEY_DAILY_WATER_GOAL, goal).apply()
    }

    fun getDailyWaterGoal(): Int = sharedPreferences.getInt(KEY_DAILY_WATER_GOAL, 2000) // Default 2000ml

    // Getter methods for sleeping and wake-up times
    fun getSleepingTime(): String = sharedPreferences.getString(KEY_SLEEPING_TIME, "N/A") ?: "N/A"

    fun getWakeUpTime(): String = sharedPreferences.getString(KEY_WAKE_UP_TIME, "N/A") ?: "N/A"

    // Setter methods for sleeping and wake-up times
    fun setSleepingTime(time: String) {
        editor.putString(KEY_SLEEPING_TIME, time).apply()
    }

    fun setWakeUpTime(time: String) {
        editor.putString(KEY_WAKE_UP_TIME, time).apply()
    }

    // Database methods (addUser, isUserValid, updateUserProfile, etc.) remain the same

    fun updateUserProfile(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        weight: Int,
        height: Int,
        gender: String,
        sleepingTime: String,
        wakeUpTime: String
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_AGE, age)
            put(COLUMN_WEIGHT, weight)
            put(COLUMN_HEIGHT, height)
            put(COLUMN_GENDER, gender)
            put(COLUMN_SLEEPING_TIME, sleepingTime)
            put(COLUMN_WAKE_UP_TIME, wakeUpTime)
        }

        val result = db.update(TABLE_USERS, contentValues, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()

        if (result > 0) {
            setUserName(firstName, lastName)
            setDailyWaterGoal(weight * 30)
            setSleepingTime(sleepingTime)
            setWakeUpTime(wakeUpTime)
            return true
        }
        return false
    }

    // Other methods related to water intake and profile completeness remain unchanged
}
