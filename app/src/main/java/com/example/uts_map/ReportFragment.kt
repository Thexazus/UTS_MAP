package com.example.uts_map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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


sealed class ReportType {
    data object WeeklyReport : ReportType()
    data object MonthlyReport : ReportType()
    data object YearlyReport : ReportType()
}


class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            ReportScreen()
        }
    }

    @Composable
    fun ReportScreen() {
        // State to track the current report type
        val currentReport = remember { mutableStateOf<ReportType>(ReportType.WeeklyReport) }

        var dropdownExpanded by remember { mutableStateOf(false) }
        val reportOptions = mapOf(
            "Weekly" to ReportType.WeeklyReport,
            "Monthly" to ReportType.MonthlyReport,
            "Yearly" to ReportType.YearlyReport
        )

        Column(modifier = Modifier.fillMaxHeight()) {
            // Buttons to switch between reports
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text="Activity Progress",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(20.0F, TextUnitType.Sp)
                )

                Box {
                    Button(onClick = { dropdownExpanded = true }) {
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
                                    currentReport.value = reportType // Update the report type
                                    dropdownExpanded = false         // Close the menu
                                }
                            )
                        }
                    }
                }
            }

            // Show the selected report
            when (currentReport.value) {
                is ReportType.WeeklyReport -> WeeklyReport()
                is ReportType.MonthlyReport -> MonthlyReport()
                is ReportType.YearlyReport -> YearlyReport()
            }
        }
    }

    @Composable
    fun WeeklyReport() {
        // Directly call the WeeklyChartScreen Composable
        WeeklyChartScreen()
    }

    @Composable
    fun MonthlyReport() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Monthly Report (Coming Soon)",
                color = Color.Black)
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

}