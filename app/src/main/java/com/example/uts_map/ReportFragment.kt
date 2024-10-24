package com.example.uts_map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.uts_map.databinding.FragmentReportBinding

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        setupBarChart()
        setupCalendarHeatmap()
        return binding.root
    }

    private fun setupBarChart() {
        val customBarChartView = binding.customBarChartView
        val weeklyData = mapOf(
            "Mon" to 70,
            "Tue" to 80,
            "Wed" to 50,
            "Thu" to 90,
            "Fri" to 60,
            "Sat" to 75,
            "Sun" to 85
        ) // Replace with actual data from UserPreferences or your data source
        customBarChartView.setData(weeklyData)
    }

    private fun setupCalendarHeatmap() {
        val calendarHeatmapView = binding.calendarHeatmapView
        val heatmapData = mapOf(
            1 to 10, 2 to 20, 3 to 30, 4 to 40, 5 to 50,
            6 to 60, 7 to 70, 8 to 80, 9 to 90, 10 to 100
        ) // Replace with your actual data

        calendarHeatmapView.setData(heatmapData)
    }

}