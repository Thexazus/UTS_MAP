// DatabaseHelper.kt
package com.example.uts_map

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "Users"
        private const val TABLE_WATER_INTAKE = "WaterIntake"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_GOAL = "goal"
        private const val COLUMN_AMOUNT = "amount"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_EMAIL TEXT, "
                + "$COLUMN_PHONE TEXT, "
                + "$COLUMN_PASSWORD TEXT)")
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
        contentValues.put(COLUMN_PASSWORD, password)

        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()
        return result != -1L // Jika -1, berarti insert gagal
    }

    // Metode untuk mengecek apakah email atau phone sudah ada
    fun isUserExists(email: String, phone: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? OR $COLUMN_PHONE = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email, phone))

        val exists = cursor.count > 0
        cursor.close()
        db.close()

        return exists // Jika count > 0, berarti user sudah ada
    }

    fun isUserValid(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email, password))

        val exists = cursor.count > 0
        cursor.close()
        db.close()

        return exists // Jika count > 0, berarti user valid
    }

    fun addOrUpdateWaterIntake(date: String, amount: Int, goal: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_DATE, date)
        contentValues.put(COLUMN_AMOUNT, amount)
        contentValues.put(COLUMN_GOAL, goal)

        db.insertWithOnConflict(TABLE_WATER_INTAKE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getWaterIntake(date: String): Pair<Int, Int> {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_AMOUNT, $COLUMN_GOAL FROM $TABLE_WATER_INTAKE WHERE $COLUMN_DATE = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(date))
        var amount = 0
        var goal = 0
        if (cursor.moveToFirst()) {
            val amountIndex = cursor.getColumnIndex(COLUMN_AMOUNT)
            val goalIndex = cursor.getColumnIndex(COLUMN_GOAL)
            if (amountIndex >= 0 && goalIndex >= 0) {
                amount = cursor.getInt(amountIndex)
                goal = cursor.getInt(goalIndex)
            }
        }
        cursor.close()
        db.close()
        return Pair(amount, goal)
    }

    fun getWaterIntakeHistory(): List<WaterIntake> {
        val intakeList = mutableListOf<WaterIntake>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_WATER_INTAKE ORDER BY $COLUMN_DATE DESC"
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val dateIndex = cursor.getColumnIndex(COLUMN_DATE)
                val amountIndex = cursor.getColumnIndex(COLUMN_AMOUNT)
                if (idIndex >= 0 && dateIndex >= 0 && amountIndex >= 0) {
                    val id = cursor.getLong(idIndex)
                    val date = cursor.getString(dateIndex)
                    val amount = cursor.getInt(amountIndex)
                    intakeList.add(WaterIntake(id, amount, date))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return intakeList
    }
}

