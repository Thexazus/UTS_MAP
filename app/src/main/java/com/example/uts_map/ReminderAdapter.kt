package com.example.uts_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onUpdate: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit,
    private val onEdit: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        private val reminderSwitch: SwitchCompat = view.findViewById(R.id.reminderSwitch)
        private val deleteButton: View = view.findViewById(R.id.deleteButton)

        private val dayViews: List<TextView> = listOf(
            view.findViewById(R.id.tvSunday),
            view.findViewById(R.id.tvMonday),
            view.findViewById(R.id.tvTuesday),
            view.findViewById(R.id.tvWednesday),
            view.findViewById(R.id.tvThursday),
            view.findViewById(R.id.tvFriday),
            view.findViewById(R.id.tvSaturday)
        )

        fun bind(reminder: Reminder) {
            timeTextView.text = reminder.time
            reminderSwitch.isChecked = reminder.isEnabled

            // Update the background for each day
            reminder.daysOfWeek.forEachIndexed { index, isSelected ->
                updateDayViewAppearance(dayViews[index], isSelected)
            }

            reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
                reminder.isEnabled = isChecked
                onUpdate(reminder)
            }

            deleteButton.setOnClickListener {
                onDelete(reminder)
            }

            // Mengklik waktu untuk mengedit
            timeTextView.setOnClickListener {
                onEdit(reminder)
            }
        }

        private fun updateDayViewAppearance(dayView: TextView, isSelected: Boolean) {
            dayView.isSelected = isSelected
            if (isSelected) {
                dayView.setBackgroundResource(R.drawable.day_selected_background)
                dayView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            } else {
                dayView.setBackgroundResource(R.drawable.day_unselected_background)
                dayView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
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
