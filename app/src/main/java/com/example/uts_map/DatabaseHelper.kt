package com.example.uts_map

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.mindrot.jbcrypt.BCrypt

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "Users"
        private const val TABLE_WATER_INTAKE = "WaterIntake"
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

    fun addUser(email: String, phone: String, password: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_EMAIL, email)
        contentValues.put(COLUMN_PHONE, phone)
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        contentValues.put(COLUMN_PASSWORD, hashedPassword)

        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()
        return result != -1L
    }

    fun isUserValid(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_PASSWORD),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        var isValid = false
        if (cursor.moveToFirst()) {
            val storedHashedPassword = cursor.getString(0)
            isValid = BCrypt.checkpw(password, storedHashedPassword)
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
        return result > 0
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
        // Menghapus entry terakhir dengan amount tertentu
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
}