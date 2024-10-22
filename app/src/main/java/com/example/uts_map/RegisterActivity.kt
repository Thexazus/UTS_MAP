package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        databaseHelper = DatabaseHelper(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else if (!phone.matches(Regex("^[0-9]{10,}$"))) {  // Validasi nomor telepon hanya angka, minimal 10 digit
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                // Validasi panjang password minimal 6 karakter
                Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                if (databaseHelper.isUserValid(email, phone)) {
                    Toast.makeText(this, "Email or Phone already registered", Toast.LENGTH_SHORT).show()
                } else {
                    val success = databaseHelper.addUser(email, phone, password)
                    if (success) {
                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                        SessionManager.setLogin(this, email)
                        SessionManager.setProfileCompleted(this, false)
                        val intent = Intent(this, ProfileDetailActivity::class.java)
                        startActivity(intent)
                        finish()  // Tutup RegisterActivity
                    } else {
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val tvLoginRedirect = findViewById<TextView>(R.id.tvLoginRedirect)
        tvLoginRedirect.setOnClickListener {
            // Intent untuk berpindah ke halaman Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Tutup RegisterActivity setelah berpindah ke Login
        }
    }
}
