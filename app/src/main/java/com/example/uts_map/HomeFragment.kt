package com.example.uts_map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import com.example.uts_map.ui.theme.UTS_MAP_NEWTheme
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import kotlin.math.roundToInt
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Button
import android.widget.ProgressBar
import com.example.uts_map.databinding.FragmentHomeBinding

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
    private val waterIntakeList = mutableListOf<WaterIntakeHistoryItem>()
    private var selectedAmount = 330
    // Modify the class-level declaration to make DAILY_WATER_GOAL mutable
    private var DAILY_WATER_GOAL = 1000 // Default goal, will be overridden by personalized calculation

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private lateinit var activityDetector: ActivityDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { initializeWaterIntakeSelection(it) }


        // Initialize Firebase Firestore and Authentication
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initializeViews(view)
        val composeView = view.findViewById<ComposeView>(R.id.waterControlCompose)
        composeView.setContent {
            ComposeContent(modifier = Modifier)
        }

        setupClickListeners(view)
//        setupChipGroup()

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

        // Initialize ActivityDetector
        activityDetector = ActivityDetector(requireContext())
        activityDetector.setOnWaterGoalIncreasedListener(object : ActivityDetector.OnWaterGoalIncreasedListener {
            override fun onWaterGoalIncreased(newGoal: Int) {
                DAILY_WATER_GOAL += newGoal
                updateWaterIntakeDisplay(DAILY_WATER_GOAL)
            }
        })

        // Check for permissions and start tracking
        checkPermissionsAndStartTracking()
    }

    private fun checkPermissionsAndStartTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    STEP_COUNTER_PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission already granted, start tracking
                startActivityTracking()
            }
        } else {
            // For older Android versions, start tracking directly
            startActivityTracking()
        }
    }

    private fun startActivityTracking() {
        if (activityDetector.isSensorAvailable()) {
            activityDetector.start()
        } else {
            Log.e("HomeFragment", "Step sensor is NOT available on this device")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Stop the sensor when the fragment is destroyed
        activityDetector.stop()
    }

    private fun initializeViews(view: View) {
        greetingTextView = view.findViewById(R.id.textViewGreeting)
        progressCircular = view.findViewById(R.id.progressCircular)
        profileImageView = view.findViewById(R.id.imageViewProfile)
        textViewProgress = view.findViewById(R.id.textViewProgress)
        textViewCurrentIntake = view.findViewById(R.id.textViewCurrentIntake)
//        textViewSelectedVolume = view.findViewById(R.id.textViewSelectedVolume)
//        chipGroupVolumes = view.findViewById(R.id.chipGroupVolumes)
//        textViewSelectedVolume = view.findViewById(R.id.textViewSelectedVolume)
//        chipGroupVolumes = view.findViewById(R.id.chipGroupVolumes)
    }

    fun initializeWaterIntakeSelection(context: Context) {
        val sharedPreferences = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
        if (!sharedPreferences.contains("water_intake_selection")) {
            val defaultIntakes = listOf(50, 100, 200)
            saveWaterIntakeList(context, defaultIntakes)
        }
    }

    fun saveWaterIntakeList(context: Context, intakeList: List<Int>) {
        val sharedPreferences = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = Gson()
        val sortedList = intakeList.sorted() // Sort the list in ascending order
        val json = gson.toJson(sortedList) // Convert the sorted list to JSON
        editor.putString("water_intake_selection", json)
        editor.apply()
    }

    fun getWaterIntakeList(context: Context): List<Int> {
        val sharedPreferences = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("water_intake_selection", null)

        val type = object : TypeToken<List<Int>>() {}.type
        return if (json != null) gson.fromJson(json, type) else emptyList()
    }

    fun addWaterIntakeList(context: Context, newVolume: Int) {
        val currentList = getWaterIntakeList(context).toMutableList()
        if (!currentList.contains(newVolume)) {
            currentList.add(newVolume)
            saveWaterIntakeList(context, currentList)
        }
    }

    fun deleteWaterIntake(context: Context, amount: Int, updateWaterAmounts: (List<Int>) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
        val gson = Gson()

        // Retrieve the current list of water intakes
        val json = sharedPreferences.getString("water_intake_selection", null)
        val waterIntakeList = if (json != null) {
            gson.fromJson(json, ArrayList::class.java) as ArrayList<Int>
        } else {
            ArrayList<Int>()
        }

        // Remove the selected amount from the list
        waterIntakeList.remove(amount)

        // Save the updated list back to SharedPreferences
        val sortedList = waterIntakeList.sorted() // Sort the list before saving
        val updatedJson = gson.toJson(sortedList)
        val editor = sharedPreferences.edit()
        editor.putString("water_intake_selection", updatedJson)
        editor.apply()

        // Update the UI by passing the updated list back to the callback
        updateWaterAmounts(sortedList) // Pass the updated list back to the function
    }

    @Composable
    fun ComposeContent(modifier: Modifier) {
        val context = LocalContext.current
        val waterAmounts = remember { mutableStateOf(getWaterIntakeList(context)) }
        var selectedAmount by remember { mutableStateOf(50) } // Default selected amount

        // Function to update the waterAmounts state
        fun updateWaterAmounts(newList: List<Int>) {
            waterAmounts.value = newList
        }

        UTS_MAP_NEWTheme {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = RoundedCornerShape(32.dp))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Minus Button
                    PlusMinusButton(
                        onClickFunction = {
                            val amountToDelete = selectedAmount
                            showDeleteConfirmationDialog(amountToDelete, context) { updatedList ->
                                updateWaterAmounts(updatedList)
                            }
                        },
                        drawableId = R.drawable.minus
                    )

                    // Center Section
                    Column(
                        modifier = Modifier
                            .height(120.dp)
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${selectedAmount} ml",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(32.dp, 0.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(waterAmounts.value) { amount ->
                                WaterAmountChip(
                                    amount = amount,
                                    isSelected = amount == selectedAmount,
                                    onChipClick = { selectedAmount = amount }
                                )
                            }
                        }
                    }

                    // Plus Button
                    PlusMinusButton(
                        onClickFunction = {
                            showAddWaterDialog(context) { updatedList ->
                                updateWaterAmounts(updatedList)
                            }
                        },
                        drawableId = R.drawable.plus
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { addWaterIntake(selectedAmount) },
                    modifier = Modifier.width(200.dp)
                ) {
                    Text(
                        text = "DRINK NOW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    @Composable
    fun WaterAmountChip(
        amount: Int,
        isSelected: Boolean,
        onChipClick: () -> Unit
    ) {
        Chip(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .then(
                    if (!isSelected) {
                        Modifier
                            .graphicsLayer(
                                scaleX = 0.8f,  // Shrink size for unselected
                                scaleY = 0.8f,
                                alpha = 0.5f    // Reduce opacity for unselected
                            )
                    } else Modifier
                ),
            onClick = { onChipClick() }, // Trigger parent composable's logic
            border = ChipDefaults.chipBorder(),
            colors = ChipDefaults.chipColors(
                backgroundColor = if (isSelected) Color(0xFF5DCCFC) else Color(0xFFB0E6FF)
            ),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$amount ml", color = Color.White)
            }
        }
    }

    @Composable
    fun PlusMinusButton(
        onClickFunction: () -> Unit,
        drawableId: Int
    ) {
        Button(
            onClick = onClickFunction,
            modifier = Modifier.wrapContentWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp, pressedElevation = 8.dp)
        ) {
            Image(modifier = Modifier.width(14.dp),
                painter = painterResource(drawableId),
                contentDescription = "Button"
                )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun previewContent() {
        UTS_MAP_NEWTheme {
            Scaffold(modifier = Modifier.fillMaxSize(),
                containerColor = Color.White) { innerPadding ->
                ComposeContent(modifier = Modifier.padding(innerPadding))
            }
        }
    }

    private fun setupClickListeners(view: View) {
        // Bell icon click listener
        view.findViewById<ImageView>(R.id.imageViewBell).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, ReminderFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

//        view.findViewById<MaterialButton>(R.id.buttonPlus).setOnClickListener {
//            showAddWaterDialog()
//        }
//
//        view.findViewById<MaterialButton>(R.id.buttonMinus).setOnClickListener {
//            val amountToDelete = selectedAmount
//            showDeleteConfirmationDialog(amountToDelete)
//        }

        // Drink now button click listener
//        view.findViewById<MaterialButton>(R.id.buttonDrinkNow).setOnClickListener {
//            addWaterIntake(selectedAmount)
//        }

        // Profile image click listener
        view.findViewById<ShapeableImageView>(R.id.imageViewProfile).setOnClickListener {
            // Navigate to profile or implement profile functionality
        }
    }

//    private fun setupChipGroup() {
//        chipGroupVolumes.setOnCheckedStateChangeListener { _, checkedIds ->
//            if (checkedIds.isNotEmpty()) {
//                when (checkedIds[0]) {
//                    R.id.chip330ml -> selectChip(330)
//                    R.id.chip600ml -> selectChip(600)
//                    R.id.chip1500ml -> selectChip(1500)
//                }
//            }
//        }
//    }

//    private fun selectChip(amount: Int) {
//        selectedAmount = amount
//        textViewSelectedVolume.text = "$amount ml"
//
//        val chipId = when (amount) {
//            330 -> R.id.chip330ml
//            600 -> R.id.chip600ml
//            1500 -> R.id.chip1500ml
//            else -> R.id.chip330ml
//        }
//
//        chipGroupVolumes.check(chipId)
//    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(context) // Mendukung scrolling
        loadWaterIntakeHistory(recyclerView)
    }

    @SuppressLint("DefaultLocale")
    private fun addWaterIntake(amount: Int) {
        val userId = auth.currentUser ?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val user = auth.currentUser?.email

        var goal = 0

        if (user != null) {
            val userDocRef = firestore.collection("users").document(user)

            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val height = documentSnapshot.getDouble("height") ?: 170.0
                    val weight = documentSnapshot.getDouble("weight") ?: 60.0
                    val age = documentSnapshot.getDouble("age") ?: 20.0
                    val sleepingTime = documentSnapshot.getString("sleepingTime") ?: "00:00"
                    val wakeUpTime = documentSnapshot.getString("wakeUpTime") ?: "00:00"
                    val gender = documentSnapshot.getString("gender") ?: "Male"

                    goal = calculatePersonalizedWaterGoal(height = height,
                        weight = weight,
                        age = age.toLong(),
                        gender = gender,
                        sleepingTime = sleepingTime,
                        wakeUpTime = wakeUpTime
                        )
                }
            }
        }

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
                    WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()))}",
                "goal" to goal
            ))

            // Tambah entri ke subkoleksi
            dailyIntakeRef.collection("intakes").add(intakeEntry)
            newAmount
        }.addOnSuccessListener { newTotal ->
            // Make sure view exists before accessing it
            view?.let { fragmentView ->
                updateWaterIntakeDisplay(newTotal.toInt())

                val newHistoryItem = WaterIntakeHistoryItem(
                    date = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    amount = amount,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )

                val recyclerView = fragmentView.findViewById<RecyclerView>(R.id.recyclerViewHistory)
                (recyclerView.adapter as? WaterIntakeHistoryAdapter)?.addItem(newHistoryItem)
                recyclerView.scrollToPosition(0)

                checkUserProgress()
            }
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
                setTextWithAnimation(textViewCurrentIntake, "$currentAmount ml") // Animate the current intake
            }
            .addOnFailureListener { e ->
                showToast("Error loading water intake: ${e.message}")
            }
    }

    private fun loadWaterIntakeHistory(recyclerView: RecyclerView) {
        val userId = auth.currentUser?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(date)
            .collection("intakes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val historyList = querySnapshot.documents.mapNotNull { doc ->
                    val amount = doc.getLong("amount")?.toInt()
                    val timestamp = doc.getTimestamp("timestamp")

                    if (amount != null && timestamp != null) {
                        WaterIntakeHistoryItem(
                            date = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate()),
                            amount = amount,
                            timestamp = timestamp.toDate() // Simpan sebagai Date
                        )
                    } else null
                }

                val adapter = WaterIntakeHistoryAdapter(historyList.toMutableList()) { item ->
                    // Show confirmation dialog before deleting
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Water Intake")
                        .setMessage("Are you sure you want to delete ${item.amount} ml intake at ${item.date}?")
                        .setPositiveButton("Delete") { _, _ ->
                            deleteWaterIntake(item)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
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

        // First, find the specific intake document to delete
        firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(date)
            .collection("intakes")
            .whereEqualTo("amount", item.amount)
            .whereEqualTo("timestamp", item.timestamp) // Pastikan ini sesuai dengan format yang disimpan
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    showToast("No matching intake found.")
                    return@addOnSuccessListener
                }

                // Pilih dokumen pertama yang cocok
                val intakeDocumentId = querySnapshot.documents[0].id

                // Referensi ke dokumen intake harian
                val dailyIntakeRef = firestore.collection("users")
                    .document(userId)
                    .collection("daily_water_intake")
                    .document(date)

                // Jalankan transaksi untuk update total amount
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(dailyIntakeRef)

                    // Dapatkan total amount saat ini
                    val currentAmount = snapshot.getLong("totalAmount") ?: 0
                    val newAmount = (currentAmount - item.amount).coerceAtLeast(0)

                    // Update total amount
                    transaction.update(dailyIntakeRef, "totalAmount", newAmount)

                    // Hapus dokumen intake spesifik
                    dailyIntakeRef.collection("intakes").document(intakeDocumentId).delete()

                    newAmount
                }.addOnSuccessListener { newTotal ->
                    // Update tampilan dengan total baru
                    updateWaterIntakeDisplay(newTotal.toInt())

                    // Muat ulang riwayat air minum
                    loadWaterIntakeHistory(requireView().findViewById(R.id.recyclerViewHistory))

                }.addOnFailureListener { e ->
                    showToast("Error deleting intake: ${e.message}")
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

    private fun setTextWithAnimation(textView: TextView, text: String) {
        textView.text = text
        textView.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(null)
            .start()
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
                    setTextWithAnimation(greetingTextView, "Hi, $firstName!")

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

                    // Animate the goal display
                    val goalTextView = view?.findViewById<TextView>(R.id.textViewGoal)
                    if (goalTextView != null) {
                        setTextWithAnimation(goalTextView, "${personalizedWaterGoal / 1000.0} Liter")
                    }

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
                        val firstName = document.getString("firstName") ?: "User "
                        setTextWithAnimation(greetingTextView, "Hi, $firstName!")
                    } else {
                        setTextWithAnimation(greetingTextView, "Hi, User!")
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

    private fun checkUserProgress() {
        val userId = auth.currentUser?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firestore.collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(date)
            .get()
            .addOnSuccessListener { document ->
                val currentAmount = document.getLong("totalAmount") ?: 0
                val progress = (currentAmount.toFloat() / DAILY_WATER_GOAL * 100).toInt()

                if (progress >= 100) {
                    showAchieveGoalFragment()
                } else {
                    showNotAchieveFragment()
                }
            }
            .addOnFailureListener { e ->
                showToast("Error checking progress: ${e.message}")
            }
    }

    private fun showAchieveGoalFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, AchieveDayGoalFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showNotAchieveFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, NotAchieveFragment())
            .addToBackStack(null)
            .commit()
    }


    private fun showAddWaterDialog(context: Context, updateWaterAmounts: (List<Int>) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_water, null)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.editTextAmount)
        val addWaterButton = dialogView.findViewById<Button>(R.id.btnAddWaterIntake) // Reference to the blue button

        editTextAmount.inputType = InputType.TYPE_CLASS_NUMBER

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        addWaterButton.setOnClickListener {
            val input = editTextAmount.text.toString()
            val amount = input.toIntOrNull()
            if (amount != null && amount > 0) {
                val currentList = getWaterIntakeList(context).toMutableList()
                if (!currentList.contains(amount)) {
                    currentList.add(amount)
                    saveWaterIntakeList(context, currentList)
                    updateWaterAmounts(currentList.sorted()) // Update the state
                    dialog.dismiss() // Close the dialog after adding
                }
            } else {
                Toast.makeText(context, "Invalid input. Please enter a positive number.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(
        amount: Int,
        context: Context,
        updateWaterAmounts: (List<Int>) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Delete Water")
            .setMessage("Are you sure you want to delete $amount ml?")
            .setPositiveButton("Yes") { _, _ ->
                val currentList = getWaterIntakeList(context).toMutableList()
                if (currentList.contains(amount)) {
                    currentList.remove(amount)
                    saveWaterIntakeList(context, currentList)
                    updateWaterAmounts(currentList.sorted()) // Trigger LazyColumn recomposition
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    data class WaterIntakeHistoryItem(
        val date: String,
        val amount: Int,
        val timestamp: Any // Make it non-nullable
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STEP_COUNTER_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Izin diberikan, mulai deteksi langkah
                    activityDetector.start()
                } else {
                    // Izin ditolak
                    Toast.makeText(
                        requireContext(),
                        "Step tracking permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
        private const val STEP_COUNTER_PERMISSION_REQUEST_CODE = 100

    }
}