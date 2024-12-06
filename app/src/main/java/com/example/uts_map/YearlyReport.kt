package com.example.uts_map

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uts_map.ui.theme.UTS_MAP_NEWTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.OverlayingComponent
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.extension.copyColor
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Month
import java.util.Locale

data class MonthlyData(
    val month: String,
    val amount: Float
)

data class YearlyData(
    val monthList: List<MonthlyData>,
    val year: Int
)

class YearlyReport : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UTS_MAP_NEWTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    YearlyChartScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

suspend fun fetchYearlyData(userId: String): Map<Int, List<MonthlyData>> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val documents = db.collection("users")
            .document(userId)
            .collection("waterIntakes")
            .orderBy("date")
            .get()
            .await()

        // Group data by year
        val yearlyData = documents.groupBy { document ->
            val date = document.getString("date") ?: ""
            try {
                LocalDate.parse(date).year // Extract the year
            } catch (e: Exception) {
                Log.e("YearlyDataProcessing", "Error parsing date: $date", e)
                null
            }
        }
            .filterKeys { it != null } // Exclude null years (invalid dates)
            .mapKeys { it.key!! } // Safe unwrap since nulls are filtered
            .mapValues { (_, yearDocuments) ->
                // Group by month and calculate totals
                val monthlyTotals = yearDocuments.groupBy { document ->
                    try {
                        LocalDate.parse(document.getString("date") ?: "").month
                    } catch (e: Exception) {
                        Log.e("YearlyDataProcessing", "Error parsing date", e)
                        null
                    }
                }
                    .filterKeys { it != null }
                    .mapKeys { it.key!! }
                    .mapValues { (_, monthDocuments) ->
                        monthDocuments.sumOf { it.getDouble("selectedVolume") ?: 0.0 }.toFloat()
                    }

                // Initialize all months with zero intake
                val allMonths = Month.values().associateWith { 0f }.toMutableMap()
                monthlyTotals.forEach { (month, total) ->
                    allMonths[month] = total
                }

                // Convert to a sorted list of MonthlyData
                allMonths.entries
                    .sortedBy { it.key.value }
                    .map { (month, totalAmount) ->
                        MonthlyData(
                            month = month.name.lowercase().replaceFirstChar { it.uppercase() },
                            amount = totalAmount
                        )
                    }
            }
        yearlyData
    } catch (e: Exception) {
        Log.e("YearlyDataProcessing", "Error fetching yearly data", e)
        emptyMap()
    }

}

@Composable
fun YearlyChartScreen(modifier: Modifier = Modifier) {
    var yearlyData by remember { mutableStateOf<Map<Int, List<MonthlyData>>>(emptyMap()) }
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        yearlyData = fetchYearlyData(userId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if(yearlyData.isNotEmpty()) {
            val sortedYears = yearlyData.keys.sorted()
            val pagerState = rememberPagerState(initialPage = sortedYears.size -1,
                pageCount = { sortedYears.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()) { page ->
                    val year = sortedYears[page]
                    val monthlyData = yearlyData[year] ?: emptyList()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$year",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (monthlyData.isNotEmpty()) {
                            YearlyLineChart(
                                data = monthlyData,
                            )
                        } else {
                            Text("No data available for this year.")
                        }
                    }
                }

        }
    }
}

@Composable
fun YearlyLineChart(data: List<MonthlyData>, modifier: Modifier = Modifier) {
    val entries = data.mapIndexed { index, monthData ->
        FloatEntry(
            x = index.toFloat(),
            y = monthData.amount
        )
    }

    val chartEntryModel = entryModelOf(entries)

    val monthFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val monthIndex = value.toInt()
        Month.values().getOrNull(monthIndex)?.name?.take(3)
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: ""
    }

    val chartColor = MaterialTheme.colorScheme.surfaceTint
    val textColor = MaterialTheme.colorScheme.onBackground

    val markerLabelFormatter = MarkerLabelFormatter { markedEntries, _ ->
        markedEntries.firstOrNull()?.let {
            "${it.entry.y.toInt()} ml"
        } ?: ""
    }

    val markerComponent = MarkerComponent(
        label = textComponent(
            color = MaterialTheme.colorScheme.onBackground,
            textSize = 12.sp,
            background = null,
        ),
        guideline = null,
        indicator = OverlayingComponent(
            outer = ShapeComponent(Shapes.pillShape, Color.BLACK.copyColor(alpha = .32f)),
            innerPaddingAllDp = 10f,
            inner = OverlayingComponent(
                outer = ShapeComponent(Shapes.pillShape, Color.BLACK),
                inner = ShapeComponent(Shapes.pillShape, Color.LTGRAY),
                innerPaddingAllDp = 5f,
            ),
        ),
    ).apply {
        this.labelFormatter = markerLabelFormatter
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Chart(
                chart = lineChart(
                    spacing = 8.dp,

                ),
                model = chartEntryModel,
                bottomAxis = rememberBottomAxis(
                    valueFormatter = monthFormatter,
                    titleComponent = textComponent(
                        color = MaterialTheme.colorScheme.onBackground,
                        textSize = 8.sp,
                        background = null
                    ),
                    guideline = null
                ),
                marker = markerComponent,
            )
            Text(
                text = "Total Yearly Intake: ${data.sumOf { it.amount.toInt() }} ml",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun YearlyChartPreview() {
    UTS_MAP_NEWTheme {
        YearlyChartScreen()
    }
}