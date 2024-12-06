package com.example.uts_map

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
//import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uts_map.ui.theme.UTS_MAP_NEWTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.Locale


data class DrinkData(
    val dayOfWeek: String,
    val date: String,
    val dailyAmount: Float
)

data class WeeklyData(
    val data: List<DrinkData>,
    val week: String,
    val weeklyAmount: Float
)

val auth = FirebaseAuth.getInstance()
private val db:FirebaseFirestore = FirebaseFirestore.getInstance()

class WeeklyReport : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UTS_MAP_NEWTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeeklyChartScreen(modifier = Modifier.padding(innerPadding))
                }
            }

        }
    }

}

@Composable
fun WeeklyChartScreen(modifier: Modifier = Modifier) {
    var weeklyDataList by remember { mutableStateOf<List<WeeklyData>>(emptyList()) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            val documents = db.collection("users")
                .document(userId)
                .collection("waterIntakes")
                .orderBy("week")
                .get()
                .await()

            val processedData = documents.groupBy { it.getString("week") ?: "" }
                .map{ (week, weekDocuments) ->
                    val dailyTotals = weekDocuments
                        .groupBy { it.getString("date") ?: "" }
                        .mapValues { (_, dayDocuments) ->
                            dayDocuments.sumOf { it.getDouble("selectedVolume") ?: 0.0 }.toFloat()
                        }

                    val drinkDataList = dailyTotals.map { (date, totalAmount) ->
                        DrinkData(
                            dayOfWeek = try {
                                LocalDate.parse(date).dayOfWeek.toString().take(3)
                                    .replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                        else it.toString()
                                    }
                            } catch(e:Exception) {
                                Log.e("DataProcessing", "Error parsing date: $date", e)
                                "" // Fallback to empty string if parsing fails
                            },
                            date = date,
                            dailyAmount = totalAmount
                        )
                    }.sortedBy { it.date }

                    WeeklyData(data=drinkDataList,
                        week=week,
                        weeklyAmount = drinkDataList.sumOf { it.dailyAmount.toDouble() }.toFloat()
                    )
                }

            weeklyDataList = processedData
            Log.d(TAG, "Processed data size: ${processedData.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching water intake data", e)
        }
    }
    Log.d(TAG, "WeeklyDataList size: ${weeklyDataList.size}")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Weekly Water Intake Report",
            style = MaterialTheme.typography.titleMedium
        )
        if (weeklyDataList.isNotEmpty()) {
            val pagerState = rememberPagerState(
                initialPage = weeklyDataList.size - 1,
                pageCount = { weeklyDataList.size }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                Log.d(TAG, "Rendering page: $page")
                Log.d(TAG, "WeeklyData for page: ${weeklyDataList[page]}")

                val weekData = weeklyDataList[page]
                val weekStartDate = try {
                    val yearWeek = weekData.week.split("-W")
                    val year = yearWeek[0].toInt()
                    val week = yearWeek[1].toInt()

                    // Find the starting date (Monday) of the week
                    val startOfWeek = LocalDate.of(year, 1, 1)
                        .with(java.time.temporal.WeekFields.ISO.weekOfYear(), week.toLong())
                        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

                    // Calculate the end date (Sunday) of the week
                    val endOfWeek = startOfWeek.plusDays(6)

                    // Format the dates as "dd MMM yyyy"
                    val formattedStartDate = startOfWeek.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    val formattedEndDate = endOfWeek.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))

                    "$formattedStartDate - $formattedEndDate" // Date range string

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing week: ${weekData.week}", e)
                    "Invalid Date Range" // Fallback in case of error
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Week: $weekStartDate",
                        style = MaterialTheme.typography.labelSmall
                    )
                    WeeklyBarChart(
                        weeklyData = weeklyDataList[page],
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Show loading or empty state
            Text("Loading water intake data...")
        }

    }
}


@Composable
fun WeeklyBarChart(weeklyData: WeeklyData, modifier: Modifier = Modifier) {

    // Check if data list is empty before creating entries
    if (weeklyData.data.isEmpty()) {
        Log.e("WeeklyBarChart", "Data list is empty!")
        Text("No data available")
        return
    }
    val weekStart = try {
        // Assuming week is in format "yyyy-Www" (ISO week format)
        val yearWeek = weeklyData.week.split("-W")
        val year = yearWeek[0].toInt()
        val week = yearWeek[1].toInt()
        LocalDate.of(year, 1, 1).with(java.time.temporal.WeekFields.ISO.weekOfYear(), week.toLong())
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    } catch (e: Exception) {
        Log.e("WeeklyBarChart", "Error parsing week: ${weeklyData.week}", e)
        LocalDate.now() // Fallback to current date
    }

    val allDays = (0 until 7).map { weekStart.plusDays(it.toLong()) }

    // Merge data with all days of the week
    val completeData = allDays.map { date ->
        weeklyData.data.find { it.date == date.toString() }
            ?: DrinkData(
                dayOfWeek = date.dayOfWeek.toString().take(3)
                    .replaceFirstChar { it.uppercaseChar() },
                date = date.toString(),
                dailyAmount = 0f
            )
    }

    val entries = completeData.mapIndexed { index, data ->
        Log.d("WeeklyBarChart", "Entry $index: Day=${data.dayOfWeek}, Amount=${data.dailyAmount}")
        FloatEntry(
            x = index.toFloat(),
            y = data.dailyAmount
        )
    }

    val chartEntryModel = try {
        entryModelOf(entries)
    } catch (e: Exception) {
        println("Error creating entry model: ${e.message}")
        null
    }

    val daysFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        // Define the days of the week (Monday to Sunday)
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        // Return the corresponding day of the week based on the index
        daysOfWeek.getOrNull(value.toInt()) ?: ""
    }

    val chartColor = MaterialTheme.colorScheme.primary

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
        indicator = null,
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
            chartEntryModel?.let { model ->
                Chart(
                    chart = columnChart(
                        spacing = 4.dp,
                        columns = listOf(
                            lineComponent(
                                thickness = 6.dp,
                                color = chartColor,
                                margins = dimensionsOf(horizontal = 2.dp),
                                shape = RoundedCornerShape(16.dp)
                            )
                        )
                    ),
                    model = model,
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = daysFormatter,
                        titleComponent = textComponent(
                            color = MaterialTheme.colorScheme.onBackground,
                            textSize = 12.sp,
                            background = null
                        ),
                        guideline = null
                    ),
                    marker = markerComponent,
                )
            } ?: run {
                // Fallback if model is null
                Text("No chart data available")
            }

            Text(
                text = "Total Weekly Intake: ${weeklyData.data.sumOf { it.dailyAmount.toInt() }} ml",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),

                )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeeklyChartPreview() {
    UTS_MAP_NEWTheme(dynamicColor = false) {
        WeeklyChartScreen()
    }
}