package com.example.uts_map

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AlertDialog


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
        // Bell icon click listener
        view.findViewById<ImageView>(R.id.imageViewBell).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, ReminderFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<MaterialButton>(R.id.buttonPlus).setOnClickListener {
            showAddWaterDialog()
        }


        // Minus button click listener
        view.findViewById<MaterialButton>(R.id.buttonMinus).setOnClickListener {
            showDeleteConfirmationDialog(amount = 100) //
        }



        // Drink now button click listener
        view.findViewById<MaterialButton>(R.id.buttonDrinkNow).setOnClickListener {
            addWaterIntake(selectedAmount)
        }

        // Sync button click listener
        view.findViewById<ImageView>(R.id.imageViewSync).setOnClickListener {
            // Sync functionality can be added here
        }

        // Profile image click listener
        view.findViewById<ShapeableImageView>(R.id.imageViewProfile).setOnClickListener {
            // Navigate to profile or implement profile functionality
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

    private fun addWaterIntake(amount: Int, saveToFirestore: Boolean = false) {
        val prefs = requireContext().getSharedPreferences("WaterTracker", Context.MODE_PRIVATE)
        val currentAmount = prefs.getFloat("todayAmount", 0f)
        val newAmount = currentAmount + amount

        // Simpan data ke SharedPreferences
        prefs.edit().putFloat("todayAmount", newAmount).apply()
        updateWaterIntakeDisplay(newAmount.toInt())

        // Tampilkan pesan ke pengguna
        showToast("Added $amount ml")

        // Simpan data ke Firestore jika diperlukan
        if (saveToFirestore) {
            saveToFirestore(amount)
        }
    }


    private fun saveToFirestore(volume: Int) {
        val userId = auth.currentUser?.uid ?: return // Ensure user is logged in
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val userData = mapOf(
            "date" to date,
            "timestamp" to FieldValue.serverTimestamp(),
            "selectedVolume" to volume,
            "userId" to userId,
            "week" to "${LocalDate.parse(date).year}-W${String.format("%02d", LocalDate.parse(date).get(
                WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()))}"
        )

        // Reference to user's water intake data in Firestore
        firestore.collection("users")
            .document(userId)
            .collection("waterIntakes")
            .add(userData)
            .addOnSuccessListener {
                // Data successfully saved
                println("Data saved to Firestore")
            }
            .addOnFailureListener { e ->
                // Handle failure
                println("Error saving data to Firestore: $e")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showAddWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_water, null)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.editTextAmount)

        // Buat dialog menggunakan AlertDialog.Builder
        AlertDialog.Builder(requireContext())
            .setTitle("Add Water")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val input = editTextAmount.text.toString()
                val amount = input.toIntOrNull()
                if (amount != null && amount > 0) {
                    addWaterIntake(amount, saveToFirestore = true)
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

        // Optionally, load data from Firestore
        loadFromFirestore()
    }

    private fun loadFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("waterIntakes")
            .orderBy("date")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    val waterIntake = document.getLong("waterIntake")?.toInt() ?: 0
                    updateWaterIntakeDisplay(waterIntake)
                }
            }
            .addOnFailureListener { e ->
                println("Error loading data from Firestore: $e")
            }
    }

    private fun updateWaterIntakeDisplay(amount: Int) {
        // Update current intake display
        textViewCurrentIntake.text = "$amount ml"

        // Update progress
        val goalAmount = 2000 // 2 Liters
        var progress = (amount.toFloat() / goalAmount * 100).toInt()

        // Ensure progress doesn't exceed 100%
        if (progress > 100) {
            progress = 100
        }

        progressCircular.setProgress(progress, true)
        textViewProgress.text = "$progress%"
    }

    private fun setGreetingMessage() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            firestore.collection("users").document(userEmail).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        greetingTextView.text = "Hi, $firstName!"
                    } else {
                        greetingTextView.text = "Hi, User!"
                    }
                }
                .addOnFailureListener {
                    greetingTextView.text = "Hi, User!"
                }
        } else {
            greetingTextView.text = "Hi, User!"
        }
    }
}
