package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

  lateinit var databaseHelper: DatabaseHelper

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    databaseHelper = DatabaseHelper(this)

    val etEmail = findViewById<EditText>(R.id.etLoginEmail)
    val etPassword = findViewById<EditText>(R.id.etLoginPassword)

    val btnLogin = findViewById<Button>(R.id.btnLogin)

    btnLogin.setOnClickListener {
      val email = etEmail.text.toString().trim()
      val password = etPassword.text.toString().trim()

      if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
      } else {
        // Cek apakah user ada di database dan password benar
        val userExists = databaseHelper.isUserValid(email, password)
        if (userExists) {
          Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
          // Masuk ke halaman utama atau dashboard
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
          finish()
        } else {
          Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
      }
    }

    // TextView untuk pindah ke halaman Register
    val registerNow = findViewById<TextView>(R.id.tvRegisterRedirect)
    registerNow.setOnClickListener {
      val intent = Intent(this, RegisterActivity::class.java)
      startActivity(intent)
    }
  }
}
