package com.example.uts_map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var greetingTextView: TextView
    private lateinit var progressCircular: CircularProgressIndicator
    private lateinit var textViewProgress: TextView
    private lateinit var textViewCurrentIntake: TextView
    private lateinit var textViewSelectedVolume: TextView
    private lateinit var chipGroupVolumes: ChipGroup
    private var selectedAmount = 50

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Initialize views
        initializeViews(view)
        setupClickListeners(view)
        setupChipGroup()

        // Load initial data
        loadCurrentAmount()

        // Set greeting
        val firstName = dbHelper.getCurrentUserFirstName()
        greetingTextView.text = "Hi, $firstName!"

        // Set current date
        view.findViewById<TextView>(R.id.textViewToday).text =
            "Today, ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())}"

        // Setup RecyclerView
        setupRecyclerView(view)
    }

    private fun initializeViews(view: View) {
        greetingTextView = view.findViewById(R.id.textViewGreeting)
        progressCircular = view.findViewById(R.id.progressCircular)
        textViewProgress = view.findViewById(R.id.textViewProgress)
        textViewCurrentIntake = view.findViewById(R.id.textViewCurrentIntake)
        textViewSelectedVolume = view.findViewById(R.id.textViewSelectedVolume)
        chipGroupVolumes = view.findViewById(R.id.chipGroupVolumes)
    }

    private fun setupClickListeners(view: View) {
        // Bell icon click listener
        view.findViewById<ImageView>(R.id.imageViewBell).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, ReminderFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        // Plus button click listener
        view.findViewById<MaterialButton>(R.id.buttonPlus).setOnClickListener {
            when (selectedAmount) {
                50 -> selectChip(200)
                200 -> selectChip(550)
            }
        }

        // Minus button click listener
        view.findViewById<MaterialButton>(R.id.buttonMinus).setOnClickListener {
            when (selectedAmount) {
                550 -> selectChip(200)
                200 -> selectChip(50)
            }
        }

        // Drink now button click listener
        view.findViewById<MaterialButton>(R.id.buttonDrinkNow).setOnClickListener {
            addWaterIntake(selectedAmount)
        }

        // Sync button click listener
        view.findViewById<ImageView>(R.id.imageViewSync).setOnClickListener {
            // Implement sync functionality
        }

        // Profile image click listener
        view.findViewById<ShapeableImageView>(R.id.imageViewProfile).setOnClickListener {
            // Navigate to profile or implement profile functionality
        }
    }

    private fun setupChipGroup() {
        chipGroupVolumes.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chip50ml -> selectChip(50)
                    R.id.chip200ml -> selectChip(200)
                    R.id.chip550ml -> selectChip(550)
                }
            }
        }
    }

    private fun selectChip(amount: Int) {
        selectedAmount = amount
        textViewSelectedVolume.text = "$amount ml"

        val chipId = when (amount) {
            50 -> R.id.chip50ml
            200 -> R.id.chip200ml
            550 -> R.id.chip550ml
            else -> R.id.chip50ml
        }

        chipGroupVolumes.check(chipId)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // TODO: Implement RecyclerView adapter and data loading
    }

    private fun addWaterIntake(amount: Int) {
        val prefs = requireContext().getSharedPreferences("WaterTracker", Context.MODE_PRIVATE)
        val currentAmount = prefs.getFloat("todayAmount", 0f)
        val newAmount = currentAmount + amount

        prefs.edit().putFloat("todayAmount", newAmount).apply()
        updateWaterIntakeDisplay(newAmount.toInt())

        // TODO: Save to database if needed
    }

    private fun loadCurrentAmount() {
        val prefs = requireContext().getSharedPreferences("WaterTracker", Context.MODE_PRIVATE)
        val currentAmount = prefs.getFloat("todayAmount", 0f)
        updateWaterIntakeDisplay(currentAmount.toInt())
    }

    private fun updateWaterIntakeDisplay(amount: Int) {
        // Update current intake display
        textViewCurrentIntake.text = "$amount ml"

        // Update progress
        val goalAmount = 2000 // 2 Liter
        val progress = (amount.toFloat() / goalAmount * 100).toInt()
        progressCircular.setProgress(progress, true)
        textViewProgress.text = "$progress%"
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}