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
import kotlinx.coroutines.launch


data class DrinkData(
    val dayOfWeek: String,
    val amount: Float
)

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
    val data = listOf(
        listOf( // Week 1
            DrinkData("Mon", 800f),
            DrinkData("Tue", 1200f),
            DrinkData("Wed", 1000f),
            DrinkData("Thu", 1500f),
            DrinkData("Fri", 900f),
            DrinkData("Sat", 1300f),
            DrinkData("Sun", 1100f)
        ),
        listOf( // Week 2
            DrinkData("Mon", 900f),
            DrinkData("Tue", 1000f),
            DrinkData("Wed", 1100f),
            DrinkData("Thu", 1400f),
            DrinkData("Fri", 1200f),
            DrinkData("Sat", 1500f),
            DrinkData("Sun", 1300f)
        ),
        listOf( // Week 3
            DrinkData("Mon", 700f),
            DrinkData("Tue", 800f),
            DrinkData("Wed", 900f),
            DrinkData("Thu", 1200f),
            DrinkData("Fri", 1000f),
            DrinkData("Sat", 1100f),
            DrinkData("Sun", 1000f)
        )
    )

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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            WeeklyBarChart(data = data[page])
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        androidx.compose.material3.Button(
            onClick = {
                coroutineScope.launch {
                    if (pagerState.currentPage > 0) {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            enabled = pagerState.currentPage > 0
        ) {
            Text("Previous Week")
        }

        // Next Week Button
        androidx.compose.material3.Button(
            onClick = {
                coroutineScope.launch {
                    if (pagerState.currentPage < data.size - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            enabled = pagerState.currentPage < data.size - 1
        ) {
            Text("Next Week")
        }
        Text(
            text = "Week ${pagerState.currentPage + 1}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun WeeklyBarChart(data: List<DrinkData>, modifier: Modifier = Modifier) {
    val entries = data.mapIndexed { index, data ->
        FloatEntry(
            x = index.toFloat(),
            y = data.amount
        )
    }

    val chartEntryModel = entryModelOf(entries)

    val daysFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        data.getOrNull(value.toInt())?.dayOfWeek ?: ""
    }

    val chartColor = MaterialTheme.colorScheme.surfaceTint
    val textColor = MaterialTheme.colorScheme.onBackground

    val gradientColors = listOf(
        chartColor.copy(alpha = 0.5f),
        chartColor.copy(alpha = 0.1f)
    )

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
                model = chartEntryModel,
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
            Text(
                text = "Total Weekly Intake: ${data.sumOf { it.amount.toInt() }} ml",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),

            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun WeeklyChartPreview() {
    UTS_MAP_NEWTheme {
        WeeklyChartScreen()
    }
}
