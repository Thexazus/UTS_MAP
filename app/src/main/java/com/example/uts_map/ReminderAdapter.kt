package com.example.uts_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val reminders: List<Reminder>,
    private val onUpdate: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        private val reminderSwitch: SwitchCompat = view.findViewById(R.id.reminderSwitch)
        private val deleteButton: View = view.findViewById(R.id.deleteButton)

        fun bind(reminder: Reminder) {
            timeTextView.text = reminder.time
            reminderSwitch.isChecked = reminder.isEnabled

            reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
                reminder.isEnabled = isChecked
                onUpdate(reminder)
            }

            deleteButton.setOnClickListener {
                onDelete(reminder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reminders[position])
    }

    override fun getItemCount() = reminders.size
}
