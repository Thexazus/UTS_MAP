package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import android.app.TimePickerDialog
import androidx.core.content.ContextCompat
import java.util.Locale

class ReminderDialogFragment : DialogFragment() {
    private var onTimeSetListener: ((Int, Int, List<Boolean>) -> Unit)? = null
    private var selectedHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    private var selectedMinute = Calendar.getInstance().get(Calendar.MINUTE)
    private var selectedDays = List(7) { false }

    // Tambahkan parameter untuk menerima Reminder
    private var reminder: Reminder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Jika reminder tidak null, ambil data dari reminder
        reminder?.let {
            selectedHour = it.time.split(":")[0].toInt()
            selectedMinute = it.time.split(":")[1].toInt()
            selectedDays = it.daysOfWeek.toMutableList()
        }

        val timePickerText = view.findViewById<TextView>(R.id.timePicker)
        val closeButton = view.findViewById<ImageButton>(R.id.btnClose)
        val saveButton = view.findViewById<MaterialButton>(R.id.saveButton)

        val dayViews = listOf(
            view.findViewById<TextView>(R.id.tvSunday),
            view.findViewById<TextView>(R.id.tvMonday),
            view.findViewById<TextView>(R.id.tvTuesday),
            view.findViewById<TextView>(R.id.tvWednesday),
            view.findViewById<TextView>(R.id.tvThursday),
            view.findViewById<TextView>(R.id.tvFriday),
            view.findViewById<TextView>(R.id.tvSaturday)
        )

        // Set text untuk time picker
        timePickerText.text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)

        // Set status hari-hari berdasarkan selectedDays
        dayViews.forEachIndexed { index, textView ->
            updateDayViewAppearance(textView, selectedDays[index])
            textView.setOnClickListener {
                selectedDays = selectedDays.toMutableList().apply {
                    this[index] = !this[index]
                }
                updateDayViewAppearance(textView, selectedDays[index])
            }
        }

        timePickerText.setOnClickListener {
            showTimePickerDialog()
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            onTimeSetListener?.invoke(selectedHour, selectedMinute, selectedDays)
            dismiss()
        }
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                view?.findViewById<TextView>(R.id.timePicker)?.text =
                    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            },
            selectedHour,
            selectedMinute,
            true
        ).show()
    }

    private fun updateDayViewAppearance(dayView: TextView, isSelected: Boolean) {
        dayView.isSelected = isSelected
        if (isSelected) {
            dayView.setBackgroundResource(R.drawable.day_selected_background)
            dayView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            dayView.setBackgroundResource(R.drawable.day_unselected_background)
            dayView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

    fun setOnTimeSetListener(listener: (Int, Int, List<Boolean>) -> Unit) {
        onTimeSetListener = listener
    }

    // Tambahkan fungsi untuk menerima Reminder
    fun setReminder(reminder: Reminder) {
        this.reminder = reminder
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    companion object {
        fun newInstance() = ReminderDialogFragment()
    }
}