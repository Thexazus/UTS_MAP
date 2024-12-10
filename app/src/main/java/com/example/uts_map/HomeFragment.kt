package com.example.uts_map

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class HomeFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var greetingTextView: TextView
    private lateinit var progressCircular: CircularProgressIndicator
    private lateinit var textViewProgress: TextView
    private lateinit var textViewCurrentIntake: TextView
    private lateinit var textViewSelectedVolume: TextView
    private lateinit var chipGroupVolumes: ChipGroup
    private var selectedAmount = 50
    // Modify the class-level declaration to make DAILY_WATER_GOAL mutable
    private var DAILY_WATER_GOAL = 1000 // Default goal, will be overridden by personalized calculation

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
        loadUserProfile()

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
        profileImageView = view.findViewById(R.id.imageViewProfile)
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

        view.findViewById<MaterialButton>(R.id.buttonMinus).setOnClickListener {
            val amountToDelete = selectedAmount
            showDeleteConfirmationDialog(amountToDelete)
        }

        // Drink now button click listener
        view.findViewById<MaterialButton>(R.id.buttonDrinkNow).setOnClickListener {
            addWaterIntake(selectedAmount)
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
        recyclerView.layoutManager = LinearLayoutManager(context) // Mendukung scrolling
        loadWaterIntakeHistory(recyclerView)
    }

    @SuppressLint("DefaultLocale")
    private fun addWaterIntake(amount: Int) {
        val userId = auth.currentUser ?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Reference dokumen harian untuk pengguna
        val dailyIntakeRef = firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(date)

        // Tambahkan entri ke subkoleksi intake
        val intakeEntry = mapOf(
            "amount" to amount,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Transaction untuk update total dan tambah entri
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(dailyIntakeRef)

            val currentAmount = snapshot.getLong("totalAmount") ?: 0
            val newAmount = currentAmount + amount

            // Update dokumen utama
            transaction.set(dailyIntakeRef, mapOf(
                "totalAmount" to newAmount,
                "date" to date,
                "userId" to userId,
                "timestamp" to FieldValue.serverTimestamp(),
                "week" to "${LocalDate.parse(date).year}-W${String.format("%02d", LocalDate.parse(date).get(
                    WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()))}"
            ))

            // Tambah entri ke subkoleksi
            dailyIntakeRef.collection("intakes").add(intakeEntry)

            newAmount
        }.addOnSuccessListener { newTotal ->
            updateWaterIntakeDisplay(newTotal.toInt())

            // Tambahkan entri baru ke historyList dan perbarui adapter
            val newHistoryItem = WaterIntakeHistoryItem(
                date = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), // Waktu saat ini
                amount = amount
            )

            // Tambahkan item baru ke adapter
            (requireView().findViewById<RecyclerView>(R.id.recyclerViewHistory).adapter as? WaterIntakeHistoryAdapter)?.addItem(newHistoryItem)
        }.addOnFailureListener { e ->
            showToast("Failed to add water intake: ${e.message}")
        }
    }

    private fun removeWaterIntake(amount: Int) {
        val userId = auth.currentUser?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val dailyIntakeRef = firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(date)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(dailyIntakeRef)

            val currentAmount = snapshot.getLong("totalAmount") ?: 0
            val newAmount = (currentAmount - amount).coerceAtLeast(0)

            transaction.set(dailyIntakeRef, mapOf(
                "totalAmount" to newAmount,
                "date" to date,
                "userId" to userId,
                "timestamp" to FieldValue.serverTimestamp()
            ))

            newAmount
        }.addOnSuccessListener { newTotal ->
            updateWaterIntakeDisplay(newTotal.toInt())
            // After success listener
            loadWaterIntakeHistory(requireView().findViewById(R.id.recyclerViewHistory))
        }.addOnFailureListener { e ->
            showToast("Failed to remove water intake: ${e.message}")
        }
    }

    private fun loadCurrentAmount() {
        val userId = auth.currentUser?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(date)
            .get()
            .addOnSuccessListener { document ->
                val currentAmount = document.getLong("totalAmount") ?: 0
                updateWaterIntakeDisplay(currentAmount.toInt())
            }
            .addOnFailureListener { e ->
                showToast("Error loading water intake: ${e.message}")
            }
    }

    private fun loadWaterIntakeHistory(recyclerView: RecyclerView) {
        val userId = auth.currentUser ?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            .collection("intakes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Urutkan berdasarkan waktu terbaru
            .get()
            .addOnSuccessListener { querySnapshot ->
                val historyList = querySnapshot.documents.mapNotNull { doc ->
                    val amount = doc.getLong("amount")?.toInt()
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()
                    if (amount != null && timestamp != null) {
                        WaterIntakeHistoryItem(
                            date = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp), // Format waktu
                            amount = amount
                        )
                    } else null
                }

                val adapter = WaterIntakeHistoryAdapter(historyList.toMutableList()) { item ->
                    // Handle delete action
                    showDeleteConfirmationDialog(item.amount)
                }

                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                showToast("Error loading water intake history: ${e.message}")
            }
    }

    private fun deleteWaterIntake(item: WaterIntakeHistoryItem) {
        val userId = auth.currentUser?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(date)
            .collection("intakes")
            .whereEqualTo("amount", item.amount) // Filter by amount
            .whereEqualTo("timestamp", item.date) // Filter by timestamp
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("daily_water_intake")
                        .document(date)
                        .collection("intakes")
                        .document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            showToast("${item.amount} ml intake removed.")
                            loadWaterIntakeHistory(requireView().findViewById(R.id.recyclerViewHistory))
                        }
                        .addOnFailureListener { e ->
                            showToast("Error deleting intake: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error finding intake to delete: ${e.message}")
            }
    }


    private fun updateWaterIntakeDisplay(amount: Int) {
        // Update current intake display
        textViewCurrentIntake.text = "$amount ml"

        // Update progress
        var progress = (amount.toFloat() / DAILY_WATER_GOAL * 100).toInt()

        // Ensure progress doesn't exceed 100%
        if (progress > 100) {
            progress = 100
        }

        progressCircular.setProgress(progress, true)
        textViewProgress.text = "$progress%"

        // Optional: Change progress color based on progress
        val progressColor = when {
            progress < 50 -> ContextCompat.getColor(requireContext(), R.color.progress_low)
            progress < 75 -> ContextCompat.getColor(requireContext(), R.color.progress_medium)
            else -> ContextCompat.getColor(requireContext(), R.color.progress_high)
        }
        progressCircular.setIndicatorColor(progressColor)
    }

    private fun calculatePersonalizedWaterGoal(
        height: Double,
        weight: Double,
        age: Long,
        gender: String,
        sleepingTime: String,
        wakeUpTime: String
    ): Int {
        // Perhitungan berbasis berat badan dengan faktor koreksi yang lebih detail
        val baseGoalMl = when {
            gender == "Male" -> {
                when {
                    age < 20 -> weight * 40.0  // Remaja laki-laki butuh lebih banyak
                    age in 20..30 -> weight * 35.0  // Dewasa muda
                    age in 31..50 -> weight * 33.0  // Dewasa tengah
                    else -> weight * 30.0  // Lansia
                }
            }
            gender == "Female" -> {
                when {
                    age < 20 -> weight * 38.0  // Remaja perempuan
                    age in 20..30 -> weight * 33.0  // Dewasa muda
                    age in 31..50 -> weight * 31.0  // Dewasa tengah
                    else -> weight * 28.0  // Lansia
                }
            }
            else -> weight * 32.0  // Netral
        }

        // Faktor penyesuaian berdasarkan aktivitas dan waktu bangun
        val sleepHours = calculateSleepDuration(sleepingTime, wakeUpTime)
        val activeHours = 24 - sleepHours

        val activityMultiplier = when {
            activeHours > 16 -> 1.4  // Sangat aktif (kerja lapangan, olahraga)
            activeHours > 14 -> 1.3  // Aktif (kerja kantoran dengan aktivitas tambahan)
            activeHours > 12 -> 1.2  // Cukup aktif (kerja kantoran)
            activeHours > 8 -> 1.1   // Kurang aktif
            else -> 1.0               // Minimal aktivitas
        }

        // Faktor musim dan iklim (Indonesia tropis)
        val climateMultiplier = 1.2  // Tambahan untuk iklim panas

        // Faktor tinggi badan
        val heightFactor = 1.0 + ((height - 170.0) / 100.0 * 0.15)

        // Perhitungan final dengan pembulatan
        val personalizedGoal = baseGoalMl *
                activityMultiplier *
                climateMultiplier *
                heightFactor

        // Pembulatan ke 50 ml terdekat dengan batas minimal 1000 ml
        return maxOf(1000, (personalizedGoal / 50.0).roundToInt() * 50)
    }

    // Fungsi perhitungan durasi tidur tetap sama
    private fun calculateSleepDuration(sleepingTime: String, wakeUpTime: String): Double {
        val (sleepHour, sleepMinute) = sleepingTime.split(":").map { it.toInt() }
        val (wakeHour, wakeMinute) = wakeUpTime.split(":").map { it.toInt() }

        var sleepDuration = if (wakeHour > sleepHour) {
            wakeHour - sleepHour + (wakeMinute - sleepMinute) / 60.0
        } else {
            (24 - sleepHour + wakeHour) + (wakeMinute - sleepMinute) / 60.0
        }

        return sleepDuration.coerceIn(4.0, 12.0)
    }

    // Modifikasi loadUserProfile untuk logging
    private fun loadUserProfile() {
        val currentUser  = auth.currentUser
        if (currentUser  == null) {
            Log.w("HomeFragment", "No authenticated user found.")
            return
        }

        firestore.collection("users")
            .document(currentUser .email ?: "")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: "User "
                    greetingTextView.text = "Hi, $firstName!"

                    // Ambil data profil untuk kalkulasi
                    val height = document.getDouble("height") ?: 170.0
                    val weight = document.getDouble("weight") ?: 70.0
                    val age = document.getLong("age") ?: 30
                    val gender = document.getString("gender") ?: "Other"
                    val sleepingTime = document.getString("sleepingTime") ?: "22:00"
                    val wakeUpTime = document.getString("wakeUpTime") ?: "06:00"

                    // Hitung goal air minum personal
                    val personalizedWaterGoal = calculatePersonalizedWaterGoal(
                        height, weight, age, gender, sleepingTime, wakeUpTime
                    )

                    // Update DAILY_WATER_GOAL dengan kalkulasi personal
                    DAILY_WATER_GOAL = personalizedWaterGoal

                    // Update TextView Goal dengan goal dalam Liter
                    view?.findViewById<TextView>(R.id.textViewGoal)?.text =
                        "${personalizedWaterGoal / 1000.0} Liter"

                    // Store the goal in Firestore
                    firestore.collection("users")
                        .document(currentUser .email ?: "")
                        .update("targetAmount", personalizedWaterGoal)
                        .addOnSuccessListener {
                            Log.d("HomeFragment", "Daily water goal updated in Firestore.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("HomeFragment", "Error updating daily water goal: ${e.message}")
                        }

                    // Log untuk debug
                    Log.d("WaterGoal", "Personalized Goal: $personalizedWaterGoal ml " +
                            "for $gender, Age: $age, Weight: $weight, Height: $height")

                    // Update profile picture based on gender
                    val genderStored = document.getString("gender") ?: "Other"
                    updateProfilePicture(genderStored)

                    // Reload current amount with new goal
                    loadCurrentAmount()
                } else {
                    Log.w("HomeFragment", "User  document does not exist.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Failed to fetch user data: ${exception.message}")
                Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfilePicture(gender: String) {
        when (gender) {
            "Male" -> {
                profileImageView.setImageResource(R.drawable.male_profile)
            }
            "Female" -> {
                profileImageView.setImageResource(R.drawable.female_profile)
            }
            else -> {
                profileImageView.setImageResource(R.drawable.profile_default)
            }
        }
    }



    private fun setGreetingMessage() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users")
                .document(user.email ?: "") // Gunakan email sebagai document ID
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        greetingTextView.text = "Hi, $firstName!"
                    } else {
                        greetingTextView.text = "Hi, User!"
                        Log.w("Firestore", "Document for user ${user.email} does not exist.")
                    }
                }
                .addOnFailureListener { exception ->
                    greetingTextView.text = "Hi, User!"
                    Log.e("Firestore", "Failed to fetch user data: ${exception.message}")
                }
        } else {
            greetingTextView.text = "Hi, User!"
            Log.w("Auth", "No authenticated user found.")
        }
    }


    private fun showAddWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_water, null)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.editTextAmount)
        editTextAmount.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(requireContext())
            .setTitle("Add Water")
            .setView(dialogView)
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Data class untuk history
    data class WaterIntakeHistoryItem(
        val date: String,
        val amount: Int
    )

    companion object {
        fun newInstance() = HomeFragment()
    }
}