package com.example.uts_map

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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

class ReminderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addReminderButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var reminderAdapter: ReminderAdapter
    private val reminderList = mutableListOf<Reminder>()
    private lateinit var alarmManager: AlarmManager

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 123
        @JvmStatic
        fun newInstance() = ReminderFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannel()
        requestNotificationPermission()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.reminderRecyclerView)
        addReminderButton = view.findViewById(R.id.addReminderButton)
        backButton = view.findViewById(R.id.backButton)
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(reminderList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
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
        dialogFragment.setOnTimeSetListener { hour, minute ->
            val timeString = String.format("%02d:%02d", hour, minute)
            val daysOfWeek = BooleanArray(7) { false }
            val newReminder = Reminder(timeString, daysOfWeek, true)
            reminderList.add(newReminder)
            reminderAdapter.notifyItemInserted(reminderList.size - 1)

            setAlarm(hour, minute, newReminder)
            Toast.makeText(
                requireContext(),
                "Reminder set for $timeString",
                Toast.LENGTH_SHORT
            ).show()
        }
        dialogFragment.show(parentFragmentManager, "reminder_dialog")
    }

    private fun setAlarm(hour: Int, minute: Int, reminder: Reminder) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("reminderTime", reminder.time)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminderList.size,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)

            // If the time has already passed today, set it for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )
        } catch (e: SecurityException) {
            Toast.makeText(
                requireContext(),
                "Failed to set alarm. Please check permissions.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ReminderChannel"
            val descriptionText = "Channel for Reminder Alarms"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("reminder_channel", name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000)
            }

            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.SCHEDULE_EXACT_ALARM
                    ),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
}