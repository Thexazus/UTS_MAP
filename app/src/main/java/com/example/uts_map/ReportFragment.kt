package com.example.uts_map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.uts_map.databinding.FragmentReportBinding
import com.example.uts_map.ui.theme.UTS_MAP_NEWTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.time.LocalDate
import kotlin.math.min

sealed class ReportType {
    data object WeeklyReport : ReportType()
    data object MonthlyReport : ReportType()
    data object YearlyReport : ReportType()
}

class ReportFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = auth.currentUser?.uid

        if (userId != null) {
            auth.currentUser?.email?.let {
                fetchUserData(it)
            }
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val twoDaysAgo = today.minusDays(2)
            fetchWaterIntake(today, userId, binding.progressToday, binding.progressTodayText)
            fetchWaterIntake(yesterday, userId, binding.progressYesterday, binding.progressYesterdayText)
            fetchWaterIntake(twoDaysAgo, userId, binding.progress2DaysAgo, binding.progress2DaysAgoText)
        } else {
            Log.e("ReportFragment", "User is not logged in.")
        }
        binding.composeView.setContent {
            ReportScreen()
        }

        // Setup back button click listener
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun fetchUserData(email: String) {
        val userDocumentRef = db.collection("users").document(email)
        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Fetch both user's name and gender
                val firstName = documentSnapshot.getString("firstName") ?: ""
                val lastName = documentSnapshot.getString("lastName") ?: ""
                val fullName = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName
                val gender = documentSnapshot.getString("gender") ?: "Other"

                // Update UI
                binding.tvUsername.text = fullName.ifEmpty { "User" }
                updateProfileImage(gender)
            } else {
                binding.tvUsername.text = "User"
                updateProfileImage("Other")
                Log.w("ReportFragment", "User document not found for $email")
            }
        }.addOnFailureListener { exception ->
            binding.tvUsername.text = "User"
            updateProfileImage("Other")
            Log.e("ReportFragment", "Failed to fetch user data for $email", exception)
        }
    }

    private fun updateProfileImage(gender: String) {
        binding.profileImage.setImageResource(
            when (gender.lowercase()) {
                "male" -> R.drawable.profile_placeholder
                "female" -> R.drawable.female_profile
                else -> R.drawable.male_profile
            }
        )
    }

    private fun fetchWaterIntake(date: LocalDate, userId: String, progressBar: CircularProgressBar, progressTextView: TextView) {
        val dateString = date.toString()

        val dateDocumentRef = db
            .collection("users")
            .document(userId)
            .collection("daily_water_intake")
            .document(dateString)

        dateDocumentRef.get().addOnSuccessListener { dateDocumentSnapshot ->
            if (dateDocumentSnapshot.exists()) {
                val dailyGoal = dateDocumentSnapshot.getDouble("goal") ?: 2000.0
                val totalAmount = dateDocumentSnapshot.getDouble("totalAmount") ?: 0.0

                val progress = if (dailyGoal > 0) {
                    (totalAmount.toFloat() / dailyGoal.toFloat()) * 100
                } else {
                    0f
                }

                progressBar.setProgressWithAnimation(progress)
                progressTextView.text = "${min(progress.toInt(), 100)}%"
            } else {
                progressBar.setProgressWithAnimation(0f)
                progressTextView.text = "0%"
                Log.w("ReportFragment", "Date document not found for $dateString")
            }
        }.addOnFailureListener { exception ->
            progressBar.setProgressWithAnimation(0f)
            progressTextView.text = "0%"
            Log.e("ReportFragment", "Failed to fetch date document for $dateString", exception)
        }
    }

    @Composable
    fun ReportScreen() {
        val currentReport = remember { mutableStateOf<ReportType>(ReportType.WeeklyReport) }
        var dropdownExpanded by remember { mutableStateOf(false) }
        val reportOptions = mapOf(
            "Weekly" to ReportType.WeeklyReport,
            "Monthly" to ReportType.MonthlyReport,
            "Yearly" to ReportType.YearlyReport
        )

        Column(modifier = Modifier.fillMaxHeight()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Progress",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(20.0F, TextUnitType.Sp)
                )

                Box {
                    Button(
                        onClick = { dropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5DCCFC),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            when (currentReport.value) {
                                is ReportType.WeeklyReport -> "Weekly"
                                is ReportType.MonthlyReport -> "Monthly"
                                is ReportType.YearlyReport -> "Yearly"
                            }
                        )
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        reportOptions.forEach { (label, reportType) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    currentReport.value = reportType
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            when (currentReport.value) {
                is ReportType.WeeklyReport -> WeeklyReport()
                is ReportType.MonthlyReport -> MonthlyReport()
                is ReportType.YearlyReport -> YearlyReport()
            }
        }
    }

    @Composable
    fun WeeklyReport() {
        WeeklyChartScreen()
    }

    @Composable
    fun MonthlyReport() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Monthly Report (Coming Soon)",
                color = Color.Black
            )
        }
    }

    @Composable
    fun YearlyReport() {
        YearlyChartScreen()
    }

    @Preview(showBackground = true)
    @Composable
    fun ReportScreenPreview() {
        UTS_MAP_NEWTheme {
            ReportScreen()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}