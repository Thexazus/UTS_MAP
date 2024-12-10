package com.example.uts_map

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class ReminderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addReminderButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var reminderAdapter: ReminderAdapter
    private val reminderList = mutableListOf<Reminder>()
    private val reminderRepository = ReminderRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        fetchReminders()

        // Request notification permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.reminderRecyclerView)
        addReminderButton = view.findViewById(R.id.addReminderButton)
        backButton = view.findViewById(R.id.backButton)
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(
            reminders = reminderList,
            onUpdate = { reminder ->
                updateReminderInFirestore(reminder)
                setAlarm(reminder) // Set alarm for updated reminder
            },
            onDelete = { reminder ->
                deleteReminderFromFirestore(reminder)
                cancelAlarm(reminder) // Cancel alarm for deleted reminder
            },
            onEdit = { reminder ->
                showEditReminderDialog(reminder)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = reminderAdapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        addReminderButton.setOnClickListener {
            showReminderDialog()
        }
    }

    private fun showReminderDialog() {
        val dialogFragment = ReminderDialogFragment.newInstance()
        dialogFragment.setOnTimeSetListener { hour, minute, daysOfWeek ->
            val timeString = String.format("%02d:%02d", hour, minute)
            val newReminder = Reminder(id = "", time = timeString, daysOfWeek = daysOfWeek, isEnabled = true)
            addReminderToFirestore(newReminder)
        }
        dialogFragment.show(parentFragmentManager, "reminder_dialog")
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        val dialogFragment = ReminderDialogFragment.newInstance()
        dialogFragment.setOnTimeSetListener { hour, minute, daysOfWeek ->
            val updatedTimeString = String.format("%02d:%02d", hour, minute)
            val updatedReminder = reminder.copy(time = updatedTimeString, daysOfWeek = daysOfWeek)
            updateReminderInFirestore(updatedReminder)
        }
        dialogFragment.show(parentFragmentManager, "edit_reminder_dialog")
    }

    private fun fetchReminders() {
        reminderRepository.getReminders(
            onSuccess = { reminders ->
                reminderList.clear()
                reminderList.addAll(reminders)
                reminderAdapter.notifyDataSetChanged()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to fetch reminders: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun addReminderToFirestore(reminder: Reminder) {
        reminderRepository.addReminder(
            reminder,
            onSuccess = {
                setAlarm(reminder) // Set alarm for new reminder
                fetchReminders()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to add reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateReminderInFirestore(reminder: Reminder) {
        reminderRepository.updateReminder(
            reminder.id,
            reminder,
            onSuccess = {
                setAlarm(reminder) // Update alarm for the reminder
                fetchReminders()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to update reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun deleteReminderFromFirestore(reminder: Reminder) {
        reminderRepository.deleteReminder(
            reminder.id,
            onSuccess = {
                cancelAlarm(reminder) // Cancel alarm for deleted reminder
                fetchReminders()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to delete reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("reminderTime", reminder.time) // Mengirim data ke AlarmReceiver
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (hour, minute) = reminder.time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Jika waktu telah berlalu, atur untuk hari berikutnya
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        fun newInstance() = ReminderFragment()
    }
}
