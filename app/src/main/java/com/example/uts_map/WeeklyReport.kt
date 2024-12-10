package com.example.uts_map

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.patrykandpatrick.vico.core.extension.sumOf
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.Locale


data class DrinkData(
    val dayOfWeek: String,
    val date: String,
    val dailyAmount: Float,
    val dailyGoal: Float
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
                .collection("daily_water_intake")
                .orderBy("week")
                .orderBy("date")
                .get()
                .await()

            val processedData = documents.groupBy { it.getString("week") ?: "" }
                .map{ (week, weekDocuments) ->
                    val drinkDataList = weekDocuments.mapNotNull { dayDoc ->
                        val date = dayDoc.getString("date") ?: return@mapNotNull null
                        val goal = dayDoc.getDouble("goal")?.toFloat() ?: 2000f
                        val dailyTotal = try {
                            val intakes = db.collection("users")
                                .document(userId)
                                .collection("daily_water_intake")
                                .document(date)
                                .collection("intakes")
                                .get()
                                .await()

                            intakes.sumOf { (it.getDouble("amount") ?: 0.0).toFloat() }
                        } catch( e: Exception) {
                            Log.e("DataProcessing", "Error calculating daily total for $date", e)
                            0f
                        }

                        DrinkData(
                            dayOfWeek = try {
                                LocalDate.parse(date).dayOfWeek.toString().take(3)
                                    .replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                        else it.toString()
                                    }
                            } catch(e: Exception) {
                                Log.e("DataProcessing", "Error parsing date: $date", e)
                                ""
                            }
                            ,
                            date = date,
                            dailyAmount = dailyTotal,
                            dailyGoal = goal
                        )
                    }.sortedBy { it.date }

                    WeeklyData(data=drinkDataList,
                        week=week,
                        weeklyAmount = drinkDataList.sumOf { it.dailyAmount.toDouble().toFloat() }
                    )
                }

            weeklyDataList = processedData
            Log.d(TAG, "Processed data size: ${processedData.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching water intake data: ${e.message}")
            e.printStackTrace()
        }
    }
    Log.d(TAG, "WeeklyDataList size: ${weeklyDataList.size}")

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
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
                        .height(256.dp)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "$weekStartDate",
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

//        progress this week
        val latestWeeklyData = weeklyDataList.getOrNull(weeklyDataList.size - 1)
        latestWeeklyData?.let { calculateFullWeekGoal(it, 2000) }?.let {
            WeeklyProgressReport(
                weeklyConsumption = latestWeeklyData.weeklyAmount.toInt() ?: 0, // Default to 0 if null
                weeklyGoal = it
            )
            WeeklyChecklistComponent(
                currentWeekData = latestWeeklyData,
                defaultGoal = 2000
            )
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
                dailyAmount = 0f,
                dailyGoal = 2000f
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

    val chartColor = Color(0xFF5DCCFC)

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
            .padding(16.dp)
            .fillMaxWidth(),

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color= Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(16.dp))
                .padding(16.dp, 8.dp),
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
        }
    }
}

fun calculateFullWeekGoal(currentWeekData:WeeklyData, defaultGoal: Int): Int {
    var totalIntake = 0
    var totalGoal = 0
    var lastKnownGoal = defaultGoal // Start with the default goal for the first day

    // Get the days of the current week (Monday to Sunday)
    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    // Loop through each day of the current week (Monday to Sunday)
    for (day in daysOfWeek) {
        // Find the data for the current day from the current week's data
        val dayData = currentWeekData.data.filter { it.dayOfWeek == day }

        val dailyIntake = dayData.sumOf { it.dailyAmount }
        // If data exists for this day, use the goal from that day, otherwise use the last known goal
        val dailyGoal = if (dayData.isNotEmpty()) {
            dayData.firstOrNull()?.dailyGoal?.toInt() ?: lastKnownGoal
        } else {
            lastKnownGoal // Use the last known goal if the current day does not have a goal
        }
        // Add intake and goal to total for progress calculation
        totalIntake += dailyIntake.toInt()
        totalGoal += dailyGoal

        // Update the last known goal for the next days
        lastKnownGoal = dailyGoal as Int
    }

    // Calculate the progress percentage
    Log.d(TAG, "$totalGoal")
    return totalGoal
}

@Composable
fun WeeklyProgressReport(
    weeklyConsumption: Int, // Total water consumption for the week
    weeklyGoal: Int // Goal for the week
) {
    val progress = (weeklyConsumption.toFloat() / weeklyGoal).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(
                color = Color(0xFF323232),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(24.dp, 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left Side - Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Progress this week",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$weeklyConsumption ml",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium

                )
            }

            // Right Side - Circular Progress Bar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    color = Color.Cyan,
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(60.dp)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun WeeklyChecklistComponent(
    currentWeekData: WeeklyData, // The WeeklyData object representing the current week
    defaultGoal: Int // Default daily goal for days without set goals
) {
    // Calculate daily completion status and streak
    val dailyCompletion = mutableListOf<Boolean>()
    var isStreak = true // Assume user is on a streak initially
    var lastKnownGoal = defaultGoal

    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    for (day in daysOfWeek) {
        val dayData = currentWeekData.data.filter { it.dayOfWeek == day }

        // Calculate total intake and goal for the day
        val dailyIntake = dayData.sumOf { it.dailyAmount }
        val dailyGoal = if (dayData.isNotEmpty()) {
            dayData.firstOrNull()?.dailyGoal?.toInt() ?: lastKnownGoal
        } else {
            lastKnownGoal // Use the last known goal if the current day does not have a goal
        }
        // Check if the day's goal is achieved
        val isCompleted = dailyIntake >= dailyGoal
        dailyCompletion.add(isCompleted)

        // Update the streak status
        if (!isCompleted) isStreak = false

        // Update the last known goal
        lastKnownGoal = dailyGoal
    }

    // Conditional supportive text
    val supportiveText = if (dailyCompletion.all { it }) {
        "Congratulations!!"
    } else {
        "Keep Going" // Or "You're doing great"
    }

    val streakText = if (isStreak) {
        "You're on a streak! 🔥"
    } else {
        "Start a new streak today!"
    }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFBAC)) // Golden yellow background
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Supportive Text
            Text(
                text = supportiveText,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF1F4529),
                fontWeight = FontWeight.Bold,
            )

            // Trophy Icon and Streak Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trophy_achievement), // Replace with your trophy drawable
                    contentDescription = "Trophy",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.width(2.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = streakText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black
                                )

                                Checkbox(
                                    checked = dailyCompletion.getOrNull(index) == true,
                                    onCheckedChange = null // Checkboxes are read-only
                                )
                            }
                        }
                    }
                }
            }

            // Daily Checklist

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