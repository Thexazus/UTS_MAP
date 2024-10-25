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

class ReminderDialogFragment : DialogFragment() {
    private var onTimeSetListener: ((Int, Int) -> Unit)? = null
    private var selectedHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    private var selectedMinute = Calendar.getInstance().get(Calendar.MINUTE)
    private val selectedDays = BooleanArray(7) { false }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Tidak perlu menggunakan onCreateDialog karena layout sudah menggunakan CardView
        return inflater.inflate(R.layout.fragment_reminder_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views sesuai dengan id di layout
        val timePickerText = view.findViewById<TextView>(R.id.timePicker)
        val closeButton = view.findViewById<ImageButton>(R.id.btnClose)
        val saveButton = view.findViewById<MaterialButton>(R.id.saveButton)

        // Initialize day views
        val dayViews = listOf(
            view.findViewById<TextView>(R.id.tvSunday),
            view.findViewById<TextView>(R.id.tvMonday),
            view.findViewById<TextView>(R.id.tvTuesday),
            view.findViewById<TextView>(R.id.tvWednesday),
            view.findViewById<TextView>(R.id.tvThursday),
            view.findViewById<TextView>(R.id.tvFriday),
            view.findViewById<TextView>(R.id.tvSaturday)
        )

        // Set initial time
        timePickerText.text = String.format("%02d : %02d", selectedHour, selectedMinute)

        // Time picker click listener
        timePickerText.setOnClickListener {
            showTimePickerDialog()
        }

        // Setup day selection
        dayViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                selectedDays[index] = !selectedDays[index]
                textView.isSelected = selectedDays[index]
                updateDayViewAppearance(textView, selectedDays[index])
            }
        }

        // Close button
        closeButton.setOnClickListener {
            dismiss()
        }

        // Save button
        saveButton.setOnClickListener {
            onTimeSetListener?.invoke(selectedHour, selectedMinute)
            dismiss()
        }
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = android.app.TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                view?.findViewById<TextView>(R.id.timePicker)?.text =
                    String.format("%02d : %02d", hour, minute)
            },
            selectedHour,
            selectedMinute,
            true
        )
        timePickerDialog.show()
    }

    private fun updateDayViewAppearance(dayView: TextView, isSelected: Boolean) {
        dayView.isSelected = isSelected
        if (isSelected) {
            dayView.setBackgroundResource(R.drawable.day_selected_background)
            dayView.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            dayView.setBackgroundResource(R.drawable.day_unselected_background)
            dayView.setTextColor(resources.getColor(android.R.color.black, null))
        }
    }

    fun setOnTimeSetListener(listener: (Int, Int) -> Unit) {
        onTimeSetListener = listener
    }

    override fun onStart() {
        super.onStart()
        // Set dialog width to match parent with margins
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