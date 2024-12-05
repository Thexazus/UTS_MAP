package com.example.uts_map

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
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

        // Initialize Firebase Firestore and Authentication
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initializeViews(view)
        setupClickListeners(view)
        setupChipGroup()

        // Load initial data
        loadCurrentAmount()

        // Set greeting with user name
        setGreetingMessage()

        // Set current date
        val currentDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        view.findViewById<TextView>(R.id.textViewToday).text = "Today, $currentDate"

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
        // Plus button click listener
        view.findViewById<MaterialButton>(R.id.buttonPlus).setOnClickListener {
            showAddWaterDialog() // Dialog untuk menambahkan jumlah air
        }

        // Minus button click listener
        view.findViewById<MaterialButton>(R.id.buttonMinus).setOnClickListener {
            showDeleteConfirmationDialog(selectedAmount) // Dialog konfirmasi penghapusan
        }

        // Drink now button click listener
        view.findViewById<MaterialButton>(R.id.buttonDrinkNow).setOnClickListener {
            addWaterIntake(selectedAmount)
        }
    }

    private fun setupChipGroup() {
        chipGroupVolumes.setOnCheckedStateChangeListener { _, checkedIds ->
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

        showToast("Added $amount ml")
    }

    private fun showAddWaterDialog() {
        val editTextAmount = EditText(requireContext()).apply {
            hint = "Enter amount (ml)"
            inputType = InputType.TYPE_CLASS_NUMBER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Water")
            .setView(editTextAmount)
            .setPositiveButton("Add") { _, _ ->
                val input = editTextAmount.text.toString()
                val amount = input.toIntOrNull()
                if (amount != null && amount > 0) {
                    addWaterIntake(amount)
                } else {
                    showToast("Invalid input. Please enter a positive number.")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(amount: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Water")
            .setMessage("Are you sure you want to delete $amount ml?")
            .setPositiveButton("Yes") { _, _ ->
                removeWaterIntake(amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeWaterIntake(amount: Int) {
        val prefs = requireContext().getSharedPreferences("WaterTracker", Context.MODE_PRIVATE)
        val currentAmount = prefs.getFloat("todayAmount", 0f)
        val newAmount = (currentAmount - amount).coerceAtLeast(0f)

        prefs.edit().putFloat("todayAmount", newAmount).apply()
        updateWaterIntakeDisplay(newAmount.toInt())

        showToast("Removed $amount ml")
    }

    private fun loadCurrentAmount() {
        val prefs = requireContext().getSharedPreferences("WaterTracker", Context.MODE_PRIVATE)
        val currentAmount = prefs.getFloat("todayAmount", 0f)
        updateWaterIntakeDisplay(currentAmount.toInt())
    }



    private fun updateWaterIntakeDisplay(amount: Int) {
        textViewCurrentIntake.text = "$amount ml"

        val goalAmount = 2000
        val progress = ((amount.toFloat() / goalAmount) * 100).toInt().coerceAtMost(100)

        progressCircular.setProgress(progress, true)
        textViewProgress.text = "$progress%"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setGreetingMessage() {
        val userEmail = auth.currentUser?.email
        greetingTextView.text = userEmail?.let { "Hi, $it!" } ?: "Hi, User!"
    }
}