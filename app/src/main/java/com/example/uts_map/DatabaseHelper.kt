// DatabaseHelper.kt
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
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_FIRST_NAME TEXT,
                $COLUMN_LAST_NAME TEXT,
                $COLUMN_AGE TEXT,
                $COLUMN_WEIGHT TEXT,
                $COLUMN_HEIGHT TEXT,
                $COLUMN_GENDER TEXT,
                $COLUMN_SLEEPING_TIME TEXT,
                $COLUMN_WAKE_UP_TIME TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Tambahkan user baru ke database
    fun addUser(email: String, phone: String, password: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_EMAIL, email)
        contentValues.put(COLUMN_PHONE, phone)

        // Hash password sebelum menyimpannya
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        contentValues.put(COLUMN_PASSWORD, hashedPassword)

        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()
        return result != -1L // Jika -1, berarti insert gagal
    }

    fun isUserValid(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_PASSWORD FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email))

        var isValid = false
        if (cursor.moveToFirst()) {
            val storedHashedPassword = cursor.getString(0)
            // Cek apakah password yang dimasukkan cocok dengan hash yang disimpan
            isValid = BCrypt.checkpw(password, storedHashedPassword)
        }

        cursor.close()
        db.close()

        return isValid
    }

    fun updateUserProfile(email: String, firstName: String, lastName: String, age: String, weight: String, height: String, gender: String, sleepingTime: String, wakeUpTime: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_FIRST_NAME, firstName)
        contentValues.put(COLUMN_LAST_NAME, lastName)
        contentValues.put(COLUMN_AGE, age)
        contentValues.put(COLUMN_WEIGHT, weight)
        contentValues.put(COLUMN_HEIGHT, height)
        contentValues.put(COLUMN_GENDER, gender)
        contentValues.put(COLUMN_SLEEPING_TIME, sleepingTime)
        contentValues.put(COLUMN_WAKE_UP_TIME, wakeUpTime)

        val result = db.update(TABLE_USERS, contentValues, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
        return result > 0
    }

    fun isProfileComplete(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_FIRST_NAME, $COLUMN_LAST_NAME, $COLUMN_AGE, $COLUMN_WEIGHT, $COLUMN_HEIGHT, $COLUMN_GENDER FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email))

        var isComplete = false
        if (cursor.moveToFirst()) {
            isComplete = !cursor.isNull(0) && !cursor.isNull(1) && !cursor.isNull(2) &&
                    !cursor.isNull(3) && !cursor.isNull(4) && !cursor.isNull(5)
        }

        cursor.close()
        db.close()
        return isComplete
    }
}
