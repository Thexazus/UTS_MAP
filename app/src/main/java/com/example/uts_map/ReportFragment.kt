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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.uts_map.databinding.FragmentReportBinding
import com.example.uts_map.ui.theme.UTS_MAP_NEWTheme
import java.time.Month


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

        Column(modifier = Modifier.fillMaxSize()) {
            // Buttons to switch between reports
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { currentReport.value = ReportType.WeeklyReport }) {
                    Text("Weekly")
                }
                Button(onClick = { currentReport.value = ReportType.MonthlyReport }) {
                    Text("Monthly")
                }
                Button(onClick = { currentReport.value = ReportType.YearlyReport }) {
                    Text("Yearly")
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
            Text("Monthly Report (Coming Soon)")
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