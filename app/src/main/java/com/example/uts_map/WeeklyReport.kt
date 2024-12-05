package com.example.uts_map

import android.os.Bundle
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
import androidx.compose.runtime.rememberCoroutineScope
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
import java.time.LocalDate
import java.util.Locale


data class DrinkData(
    val dayOfWeek: String,
    val date: String,
    val amount: Float
)

data class WeeklyData(
    val data: List<DrinkData>,
    val week: String,
    val totalAmount: Float
)

val auth = FirebaseAuth.getInstance()
val db = FirebaseFirestore.getInstance()

class WeeklyReport : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                WeeklyChartScreen(modifier = Modifier.padding(innerPadding))
            }

        }
    }
}

@Composable
fun WeeklyChartScreen(modifier: Modifier = Modifier) {
    val data = mutableListOf<WeeklyData>()

    val userId = auth.currentUser?.uid ?: return
    db.collection("users")
        .document(userId)
        .collection("waterIntakes")
        .get()
        .addOnSuccessListener { documents ->
            val groupedByWeek = documents.groupBy { it.getString("week") ?: "" }

            // Process each week's documents
            val processedData = groupedByWeek.map { (week, weekDocuments) ->
                // Convert each document to DrinkData
                val drinkDataList = weekDocuments.map { document ->
                    DrinkData(
                        dayOfWeek = document.getString("date")?.let {
                            LocalDate.parse(it).dayOfWeek.toString().take(3)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        } ?: "",
                        date = document.getString("date") ?: "",
                        amount = document.getDouble("selectedVolume")?.toFloat() ?: 0f
                    )
                }

                // Create WeeklyData for each week
                WeeklyData(
                    data = drinkDataList,
                    week = week,
                    totalAmount = drinkDataList.sumOf { it.amount.toDouble() }.toFloat()
                )
            }

            // Update the data list
            data.addAll(processedData)
        }


//
//    userRef.get().addOnSuccessListener { querySnapshot ->
//        for(document in querySnapshot.documents) {
//            val obj = document.
//        }
//    }

    val pagerState = rememberPagerState(
        initialPage = data.size - 1,
        pageCount = {data.size}
    )
    val coroutineScope = rememberCoroutineScope()

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
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier.fillMaxWidth()
//        ) { page ->
//            WeeklyBarChart(data = data[page])
//        }
    }
}

//@Composable
//fun WeeklyBarChart(data: WeeklyData, modifier: Modifier = Modifier) {
//    val entries = data.mapIndexed { index, data ->
//        FloatEntry(
//            x = index.toFloat(),
//            y = data.amount
//        )
//    }
//
//    val chartEntryModel = entryModelOf(entries)
//
//    val daysFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
//        data.getOrNull(value.toInt())?.dayOfWeek ?: ""
//    }
//
//    val chartColor = MaterialTheme.colorScheme.primary
//    val textColor = MaterialTheme.colorScheme.onSurface
//
//    val gradientColors = listOf(
//        chartColor.copy(alpha = 0.5f),
//        chartColor.copy(alpha = 0.1f)
//    )
//
//    val markerLabelFormatter = MarkerLabelFormatter { markedEntries, _ ->
//        markedEntries.firstOrNull()?.let {
//            "${it.entry.y.toInt()} ml"
//        } ?: ""
//    }
//
//    val markerComponent = MarkerComponent(
//        label = textComponent(
//            color = MaterialTheme.colorScheme.onBackground,
//            textSize = 12.sp,
//            background = null,
//        ),
//        guideline = null,
//        indicator = null,
//        ).apply {
//            this.labelFormatter = markerLabelFormatter
//    }
//    UTS_MAP_NEWTheme(dynamicColor = false) {
//        Box(
//            modifier = modifier
//                .fillMaxWidth()
//                .height(300.dp)
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                Chart(
//                    chart = columnChart(
//                        spacing = 4.dp,
//                        columns = listOf(
//                            lineComponent(
//                                thickness = 6.dp,
//                                color = chartColor,
//                                margins = dimensionsOf(horizontal = 2.dp),
//                                shape = RoundedCornerShape(16.dp)
//                            )
//                        )
//                    ),
//                    model = chartEntryModel,
//                    bottomAxis = rememberBottomAxis(
//                        valueFormatter = daysFormatter,
//                        titleComponent = textComponent(
//                            color = MaterialTheme.colorScheme.onBackground,
//                            textSize = 12.sp,
//                            background = null
//                        ),
//                        guideline = null
//                    ),
//                    marker = markerComponent,
//                )
//                Text(
//                    text = "Total Weekly Intake: ${data.sumOf { it.amount.toInt() }} ml",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.padding(top = 6.dp),
//
//                )
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun WeeklyChartPreview() {
//    UTS_MAP_NEWTheme(dynamicColor = false) {
//        WeeklyChartScreen()
//    }
//}
