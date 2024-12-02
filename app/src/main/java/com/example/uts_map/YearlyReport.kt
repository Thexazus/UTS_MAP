package com.example.uts_map

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uts_map.ui.theme.UTS_MAP_NEWTheme
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

data class MonthlyData(
    val month: String,
    val amount: Float
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

@Composable
fun YearlyChartScreen(modifier: Modifier = Modifier) {
    val data = listOf(
        MonthlyData("Jan", 9500f),
        MonthlyData("Feb", 10200f),
        MonthlyData("Mar", 8800f),
        MonthlyData("Apr", 11000f),
        MonthlyData("May", 9700f),
        MonthlyData("Jun", 10500f),
        MonthlyData("Jul", 9900f),
        MonthlyData("Aug", 10300f),
        MonthlyData("Sep", 9600f),
        MonthlyData("Oct", 10000f),
        MonthlyData("Nov", 9800f),
        MonthlyData("Dec", 10100f)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Yearly Water Intake Report",
            style = MaterialTheme.typography.titleMedium
        )
        YearlyLineChart(
            data = data,
        )
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
        data.getOrNull(value.toInt())?.month ?: ""
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
                        textSize = 12.sp,
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