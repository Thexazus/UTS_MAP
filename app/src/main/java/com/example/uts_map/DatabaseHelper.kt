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
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
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
}
