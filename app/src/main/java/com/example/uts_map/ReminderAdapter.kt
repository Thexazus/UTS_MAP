package com.example.uts_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(private val reminders: List<Reminder>) :
    RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        private val mondayTextView: TextView = view.findViewById(R.id.tvMonday)
        private val tuesdayTextView: TextView = view.findViewById(R.id.tvTuesday)
        private val wednesdayTextView: TextView = view.findViewById(R.id.tvWednesday)
        private val thursdayTextView: TextView = view.findViewById(R.id.tvThursday)
        private val fridayTextView: TextView = view.findViewById(R.id.tvFriday)
        private val saturdayTextView: TextView = view.findViewById(R.id.tvSaturday)
        private val sundayTextView: TextView = view.findViewById(R.id.tvSunday)
        private val reminderSwitch: SwitchCompat = view.findViewById(R.id.reminderSwitch)

        private val dayTextViews = listOf(
            mondayTextView, tuesdayTextView, wednesdayTextView,
            thursdayTextView, fridayTextView, saturdayTextView, sundayTextView
        )

        fun bind(reminder: Reminder, position: Int) {
            timeTextView.text = reminder.time
            reminderSwitch.isChecked = reminder.isEnabled

            // Update colors for each day based on selection
            dayTextViews.forEachIndexed { index, textView ->
                val isSelected = reminder.daysOfWeek[index]
                val textColor = if (isSelected) {
                    ContextCompat.getColor(itemView.context, R.color.orange)
                } else {
                    ContextCompat.getColor(itemView.context, android.R.color.black)
                }
                textView.setTextColor(textColor)
            }

            // Handle switch changes
            reminderSwitch.setOnClickListener {
                reminder.isEnabled = !reminder.isEnabled
                notifyItemChanged(position)
            }

            // Handle day text click events
            dayTextViews.forEachIndexed { index, textView ->
                textView.setOnClickListener {
                    reminder.daysOfWeek[index] = !reminder.daysOfWeek[index]
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reminders[position], position)
    }

    override fun getItemCount() = reminders.size
}