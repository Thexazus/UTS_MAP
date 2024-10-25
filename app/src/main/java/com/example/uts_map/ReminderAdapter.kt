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
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val mondayTextView: TextView = view.findViewById(R.id.tvMonday)
        val tuesdayTextView: TextView = view.findViewById(R.id.tvTuesday)
        val wednesdayTextView: TextView = view.findViewById(R.id.tvWednesday)
        val thursdayTextView: TextView = view.findViewById(R.id.tvThursday)
        val fridayTextView: TextView = view.findViewById(R.id.tvFriday)
        val saturdayTextView: TextView = view.findViewById(R.id.tvSaturday)
        val sundayTextView: TextView = view.findViewById(R.id.tvSunday)
        val reminderSwitch: SwitchCompat = view.findViewById(R.id.reminderSwitch)

        // List to hold all day TextViews for easier iteration
        val dayTextViews = listOf(
            mondayTextView, tuesdayTextView, wednesdayTextView,
            thursdayTextView, fridayTextView, saturdayTextView, sundayTextView
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]
        val context = holder.itemView.context

        // Set time
        holder.timeTextView.text = reminder.time

        // Set switch state
        holder.reminderSwitch.isChecked = reminder.isActive

        // Update colors for each day based on selection
        holder.dayTextViews.forEachIndexed { index, textView ->
            val isSelected = reminder.daysOfWeek[index]
            val textColor = if (isSelected) {
                ContextCompat.getColor(context, R.color.orange)
            } else {
                ContextCompat.getColor(context, android.R.color.black)
            }
            textView.setTextColor(textColor)
        }

        // Handle switch changes
        holder.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            reminder.isActive = isChecked
            // Tambahkan callback untuk menyimpan perubahan jika diperlukan
        }

        // Optional: Handle day text click events if needed
        holder.dayTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                reminder.daysOfWeek[index] = !reminder.daysOfWeek[index]
                notifyItemChanged(position)
                // Tambahkan callback untuk menyimpan perubahan jika diperlukan
            }
        }
    }

    override fun getItemCount() = reminders.size
}



