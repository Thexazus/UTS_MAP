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
        private const val PREF_NAME = "UserPrefs"

        // SharedPreferences keys
        private const val KEY_SLEEPING_TIME = "sleeping_time"
        private const val KEY_WAKE_UP_TIME = "wake_up_time"
        private const val KEY_GENDER = "gender"
        private const val KEY_HEIGHT = "height"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_AGE = "age"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_DAILY_WATER_GOAL = "daily_water_goal"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create the Users table
        val createUserTable = """
            CREATE TABLE Users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                phone TEXT,
                password TEXT,
                first_name TEXT,
                last_name TEXT,
                age INTEGER,
                weight INTEGER,
                height INTEGER,
                gender TEXT,
                sleeping_time TEXT,
                wake_up_time TEXT
            )
        """.trimIndent()

        db?.execSQL(createUserTable)

        // Create other tables as needed
        // For example, WaterIntake table
        val createWaterIntakeTable = """
            CREATE TABLE WaterIntake (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount INTEGER NOT NULL,
                timestamp TEXT NOT NULL
            )
        """.trimIndent()

        db?.execSQL(createWaterIntakeTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database upgrade
        db?.execSQL("DROP TABLE IF EXISTS Users")
        db?.execSQL("DROP TABLE IF EXISTS WaterIntake")
        onCreate(db)
    }

    // SharedPreferences methods for sleeping and wake-up times
    fun getSleepingTime(): String {
        return sharedPreferences.getString(KEY_SLEEPING_TIME, "22:00") ?: "22:00"
    }

    fun getWakeUpTime(): String {
        return sharedPreferences.getString(KEY_WAKE_UP_TIME, "06:00") ?: "06:00"
    }

    fun setSleepingTime(time: String) {
        editor.putString(KEY_SLEEPING_TIME, time).apply()
    }

    fun setWakeUpTime(time: String) {
        editor.putString(KEY_WAKE_UP_TIME, time).apply()
    }

    // SharedPreferences methods for gender
    fun setGender(gender: String) {
        editor.putString(KEY_GENDER, gender).apply()
    }

    fun getGender(): String {
        return sharedPreferences.getString(KEY_GENDER, "Other") ?: "Other"
    }

    // Methods to get and set height
    fun getHeight(): Int {
        return sharedPreferences.getInt(KEY_HEIGHT, 0)
    }

    fun setHeight(height: Int) {
        editor.putInt(KEY_HEIGHT, height).apply()
    }

    // Methods to get and set weight
    fun getWeight(): Int {
        return sharedPreferences.getInt(KEY_WEIGHT, 0)
    }

    fun setWeight(weight: Int) {
        editor.putInt(KEY_WEIGHT, weight).apply()
    }

    // Methods to get and set age
    fun getAge(): Int {
        return sharedPreferences.getInt(KEY_AGE, 0)
    }

    fun setAge(age: Int) {
        editor.putInt(KEY_AGE, age).apply()
    }

    // Methods to get and set first name
    fun getCurrentUserFirstName(): String {
        return sharedPreferences.getString(KEY_FIRST_NAME, "") ?: ""
    }

    fun setCurrentUserFirstName(firstName: String) {
        editor.putString(KEY_FIRST_NAME, firstName).apply()
    }

    // Methods to get and set last name
    fun getCurrentUserLastName(): String {
        return sharedPreferences.getString(KEY_LAST_NAME, "") ?: ""
    }

    fun setCurrentUserLastName(lastName: String) {
        editor.putString(KEY_LAST_NAME, lastName).apply()
    }

    // Method to get and set email
    fun getCurrentUserEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun setCurrentUserEmail(email: String) {
        editor.putString(KEY_EMAIL, email).apply()
    }

    // Method to get and set daily water goal
    fun getDailyWaterGoal(): Int {
        return sharedPreferences.getInt(KEY_DAILY_WATER_GOAL, 2000)
    }

    fun setDailyWaterGoal(goal: Int) {
        editor.putInt(KEY_DAILY_WATER_GOAL, goal).apply()
    }

    // Method to set user as logged in
    fun setUserLoggedIn(email: String) {
        editor.apply {
            putBoolean("is_logged_in", true)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    // Method to add a new user to the database
    fun addUser(email: String, phone: String, password: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("email", email)
        contentValues.put("phone", phone)
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        contentValues.put("password", hashedPassword)

        val result = db.insert("Users", null, contentValues)
        db.close()

        if (result != -1L) {
            // Set the current user email in shared preferences
            setCurrentUserEmail(email)
            return true
        }
        return false
    }

    // Method to validate user credentials
    fun isUserValid(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            "Users",
            arrayOf("password", "first_name", "last_name"),
            "email = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        var isValid = false
        if (cursor.moveToFirst()) {
            val storedHashedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name")) ?: ""
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name")) ?: ""
            isValid = BCrypt.checkpw(password, storedHashedPassword)

            if (isValid) {
                setUserLoggedIn(email)
                setCurrentUserFirstName(firstName)
                setCurrentUserLastName(lastName)
            }
        }

        cursor.close()
        db.close()
        return isValid
    }

    // Method to update user profile
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
            put("first_name", firstName)
            put("last_name", lastName)
            put("age", age)
            put("weight", weight)
            put("height", height)
            put("gender", gender)
            put("sleeping_time", sleepingTime)
            put("wake_up_time", wakeUpTime)
        }

        val result = db.update("Users", contentValues, "email = ?", arrayOf(email))
        db.close()

        if (result > 0) {
            // Update SharedPreferences with the latest data
            setCurrentUserFirstName(firstName)
            setCurrentUserLastName(lastName)
            setAge(age)
            setWeight(weight)
            setHeight(height)
            setGender(gender)
            setSleepingTime(sleepingTime)
            setWakeUpTime(wakeUpTime)
            // Update daily water goal based on weight
            setDailyWaterGoal(weight * 30)
            return true
        }
        return false
    }

    // Method to check if the user is logged in
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    // Additional methods as needed for your app...
}
