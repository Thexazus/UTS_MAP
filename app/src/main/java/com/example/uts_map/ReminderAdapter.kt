package com.example.uts_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(private val reminders: List<Reminder>) :
    RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val daysTextView: TextView = view.findViewById(R.id.daysTextView)
        val reminderSwitch: SwitchCompat = view.findViewById(R.id.reminderSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.timeTextView.text = reminder.time
        holder.daysTextView.text = getDaysString(reminder.daysOfWeek)
        holder.reminderSwitch.isChecked = reminder.isActive

        holder.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            reminder.isActive = isChecked
        }
    }

    private fun getDaysString(daysOfWeek: BooleanArray): String {
        val days = listOf("M", "T", "W", "Th", "F", "St", "S")
        return days.filterIndexed { index, _ -> daysOfWeek[index] }.joinToString(" ")
    }

    override fun getItemCount() = reminders.size
}