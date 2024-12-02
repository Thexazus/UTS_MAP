package com.example.uts_map

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.mindrot.jbcrypt.BCrypt

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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

    // SharedPreferences methods for name management
    fun setUserName(firstName: String, lastName: String) {
        editor.apply {
            putString(KEY_CURRENT_USER_FIRST_NAME, firstName)
            putString(KEY_CURRENT_USER_LAST_NAME, lastName)
            putString(KEY_CURRENT_USER_FULL_NAME, "$firstName $lastName")
            apply()
        }
    }

    fun getCurrentUserFirstName(): String {
        return sharedPreferences.getString(KEY_CURRENT_USER_FIRST_NAME, "") ?: ""
    }

    fun getCurrentUserLastName(): String {
        return sharedPreferences.getString(KEY_CURRENT_USER_LAST_NAME, "") ?: ""
    }

    fun getCurrentUserFullName(): String {
        return sharedPreferences.getString(KEY_CURRENT_USER_FULL_NAME, "") ?: ""
    }

    // Other SharedPreferences methods
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

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUserEmail(): String? {
        return sharedPreferences.getString(KEY_CURRENT_USER_EMAIL, null)
    }

    fun setDailyWaterGoal(goal: Int) {
        editor.putInt(KEY_DAILY_WATER_GOAL, goal).apply()
    }

    fun getDailyWaterGoal(): Int {
        return sharedPreferences.getInt(KEY_DAILY_WATER_GOAL, 2000) // Default 2000ml
    }

    // Modified database methods to integrate with SharedPreferences
    fun addUser(email: String, phone: String, password: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_EMAIL, email)
        contentValues.put(COLUMN_PHONE, phone)
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        contentValues.put(COLUMN_PASSWORD, hashedPassword)

        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()

        if (result != -1L) {
            setUserLoggedIn(email)
            return true
        }
        return false
    }

    fun isUserValid(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_PASSWORD, COLUMN_FIRST_NAME, COLUMN_LAST_NAME),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        var isValid = false
        if (cursor.moveToFirst()) {
            val storedHashedPassword = cursor.getString(0)
            val firstName = cursor.getString(1)
            val lastName = cursor.getString(2)
            isValid = BCrypt.checkpw(password, storedHashedPassword)

            if (isValid) {
                setUserLoggedIn(email)
                if (firstName != null && lastName != null) {
                    setUserName(firstName, lastName)
                }
            }
        }

        cursor.close()
        db.close()
        return isValid
    }

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
            // Calculate and set recommended daily water intake based on weight
            val recommendedWaterIntake = weight * 30 // 30ml per kg of body weight
            setDailyWaterGoal(recommendedWaterIntake)
            return true
        }
        return false
    }

    fun isProfileComplete(email: String): Boolean {
        val db = this.readableDatabase
        val projection = arrayOf(
            COLUMN_FIRST_NAME,
            COLUMN_LAST_NAME,
            COLUMN_AGE,
            COLUMN_WEIGHT,
            COLUMN_HEIGHT,
            COLUMN_GENDER
        )

        val cursor = db.query(
            TABLE_USERS,
            projection,
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        var isComplete = false
        if (cursor.moveToFirst()) {
            isComplete = !cursor.isNull(0) && !cursor.isNull(1) && !cursor.isNull(2) &&
                    !cursor.isNull(3) && !cursor.isNull(4) && !cursor.isNull(5)
        }

        cursor.close()
        db.close()
        return isComplete
    }

    fun insertWaterIntake(waterIntake: WaterIntake) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, waterIntake.id)
            put(COLUMN_AMOUNT, waterIntake.amount)
            put(COLUMN_TIMESTAMP, waterIntake.timestamp)
        }
        db.insert(TABLE_WATER_INTAKE, null, values)
        db.close()
    }

    fun removeLastWaterIntake(amount: Int) {
        val db = writableDatabase
        val selection = "$COLUMN_AMOUNT = ? AND $COLUMN_TIMESTAMP = (SELECT MAX($COLUMN_TIMESTAMP) FROM $TABLE_WATER_INTAKE WHERE $COLUMN_AMOUNT = ?)"
        db.delete(TABLE_WATER_INTAKE, selection, arrayOf(amount.toString(), amount.toString()))
        db.close()
    }

    fun getWaterIntakeData(): List<WaterIntake> {
        val waterIntakeList = mutableListOf<WaterIntake>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_WATER_INTAKE,
            arrayOf(COLUMN_ID, COLUMN_AMOUNT, COLUMN_TIMESTAMP),
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )

        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(COLUMN_ID)
            val amountIndex = c.getColumnIndexOrThrow(COLUMN_AMOUNT)
            val timestampIndex = c.getColumnIndexOrThrow(COLUMN_TIMESTAMP)

            while (c.moveToNext()) {
                val id = c.getLong(idIndex)
                val amount = c.getInt(amountIndex)
                val timestamp = c.getString(timestampIndex)
                waterIntakeList.add(WaterIntake(id, amount, timestamp))
            }
        }

        db.close()
        return waterIntakeList
    }

    fun height(): Int? {
        val email = getCurrentUserEmail() ?: return null
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_HEIGHT),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val height =
            if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT)) else null
        cursor.close()
        db.close()
        return height
    }

    fun getWeight(): Int? {
        val email = getCurrentUserEmail() ?: return null
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_WEIGHT),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val weight =
            if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)) else null
        cursor.close()
        db.close()
        return weight
    }

    fun getAge(): Int? {
        val email = getCurrentUserEmail() ?: return null
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_AGE),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val age =
            if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)) else null
        cursor.close()
        db.close()
        return age
    }

    fun getSleepingTime(): String? {
        val email = getCurrentUserEmail() ?: return null
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_SLEEPING_TIME),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val sleepingTime = if (cursor.moveToFirst()) cursor.getString(
            cursor.getColumnIndexOrThrow(COLUMN_SLEEPING_TIME)
        ) else null
        cursor.close()
        db.close()
        return sleepingTime
    }

    fun getWakeUpTime(): String? {
        val email = getCurrentUserEmail() ?: return null
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_WAKE_UP_TIME),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val wakeUpTime = if (cursor.moveToFirst()) cursor.getString(
            cursor.getColumnIndexOrThrow(COLUMN_WAKE_UP_TIME)
        ) else null
        cursor.close()
        db.close()
        return wakeUpTime
    }

    fun setSleepingTime(time: String) {
        val email = getCurrentUserEmail() ?: return
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SLEEPING_TIME, time)
        }
        db.update(TABLE_USERS, contentValues, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
    }

    fun setWakeUpTime(time: String) {
        val email = getCurrentUserEmail() ?: return
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_WAKE_UP_TIME, time)
        }
        db.update(TABLE_USERS, contentValues, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
    }

    fun setGender(gender: String) {
        val email = getCurrentUserEmail() ?: return
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_GENDER, gender)
        }
        db.update(TABLE_USERS, contentValues, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
    }

    fun getGender(): String? {
        val email = getCurrentUserEmail() ?: return null
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_GENDER),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val gender =
            if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)) else null
        cursor.close()
        db.close()
        return gender
    }
}