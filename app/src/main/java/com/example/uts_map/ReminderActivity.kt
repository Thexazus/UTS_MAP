package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.DialogFragment

class ReminderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addReminderButton: FloatingActionButton
    private lateinit var backButton: ImageButton
    private lateinit var reminderAdapter: ReminderAdapter
    private val reminderList = mutableListOf<Reminder>()
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        }
        onBackPressedCallback.isEnabled = true
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)


        initializeViews()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.reminderRecyclerView)
        addReminderButton = findViewById(R.id.addReminderButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(reminderList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReminderActivity)
            adapter = reminderAdapter
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            navigateToHome()
        }

        addReminderButton.setOnClickListener {
            showReminderDialog()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeFragment::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }


    private fun showReminderDialog() {
        val dialogFragment = ReminderDialogFragment.newInstance()
        dialogFragment.setOnTimeSetListener { hour, minute ->
            val timeString = String.format("%02d:%02d", hour, minute)
            // Membuat array boolean untuk 7 hari (Senin-Minggu)
            val daysOfWeek = BooleanArray(7) { false }
            // Membuat reminder baru
            val newReminder = Reminder(timeString, daysOfWeek)
            reminderList.add(newReminder)
            reminderAdapter.notifyItemInserted(reminderList.size - 1)

            Toast.makeText(
                this,
                "Reminder set for $timeString",
                Toast.LENGTH_SHORT
            ).show()
        }
        dialogFragment.show(supportFragmentManager, "reminder_dialog")
    }

    private fun editReminder(position: Int) {
        val reminder = reminderList[position]
        val dialogFragment = ReminderDialogFragment.newInstance()
        dialogFragment.setOnTimeSetListener { hour, minute ->
            val timeString = String.format("%02d:%02d", hour, minute)
            reminderList[position] = Reminder(timeString, reminder.daysOfWeek)
            reminderAdapter.notifyItemChanged(position)
        }
        dialogFragment.show(supportFragmentManager, "reminder_dialog")
    }
}