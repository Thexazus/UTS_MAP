package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Locale

class ReminderDialogFragment : DialogFragment() {

    private var onTimeSetListener: ((Int, Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timePicker: TimePicker = view.findViewById(R.id.timePicker)
        val saveButton: Button = view.findViewById(R.id.saveButton)

        timePicker.setIs24HourView(true)

        saveButton.setOnClickListener {
            val hour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.hour
            } else {
                @Suppress("DEPRECATION")
                timePicker.currentHour
            }
            val minute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.minute
            } else {
                @Suppress("DEPRECATION")
                timePicker.currentMinute
            }
            onTimeSetListener?.invoke(hour, minute)
            dismiss()
        }
    }

    fun setOnTimeSetListener(listener: (Int, Int) -> Unit) {
        onTimeSetListener = listener
    }

    companion object {
        fun newInstance(): ReminderDialogFragment {
            return ReminderDialogFragment()
        }
    }
}