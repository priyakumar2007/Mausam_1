package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.ui.theme.*

enum class ChartMetric(val label: String, val unit: String) {
    RAIN_PROB("Rain %", "%"),
    TEMPERATURE("Temp", "°C"),
    WIND("Wind", "km/h"),
    HUMIDITY("Humidity", "%")
}

@Composable
fun LiveWeatherPlotCard(
    hourly: List<HourlyForecastItem>,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.RAIN_PROB) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_weather_plot_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "📊 Live 24-Hour Forecast Chart",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hourly atmospheric trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartMetric.values().forEach { metric ->
                    FilterChip(
                        selected = selectedMetric == metric,
                        onClick = { selectedMetric = metric },
                        label = { Text(metric.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SkyBluePrimary.copy(alpha = 0.2f),
                            selectedLabelColor = SkyBlueLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Natural Language Summary
            val summaryText = remember(hourly, selectedMetric) {
                generateChartSummary(hourly, selectedMetric)
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 $summaryText",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                val displayList = hourly.take(18)
                val chartWidth = (displayList.size * 56).dp

                WeatherSmoothLineChart(
                    items = displayList,
                    metric = selectedMetric,
                    modifier = Modifier
                        .width(chartWidth)
                        .height(160.dp)
                )
            }
        }
    }
}

@Composable
fun WeatherSmoothLineChart(
    items: List<HourlyForecastItem>,
    metric: ChartMetric,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val values = items.map { item ->
        when (metric) {
            ChartMetric.RAIN_PROB -> item.rainProbability.toFloat()
            ChartMetric.TEMPERATURE -> item.tempC.toFloat()
            ChartMetric.WIND -> item.windSpeedKmh.toFloat()
            ChartMetric.HUMIDITY -> item.humidityPercent.toFloat()
        }
    }

    val minVal = values.minOrNull() ?: 0f
    val maxVal = values.maxOrNull() ?: 100f
    val range = (maxVal - minVal).coerceAtLeast(10f)

    val lineColor = when (metric) {
        ChartMetric.RAIN_PROB -> SkyBlueLight
        ChartMetric.TEMPERATURE -> SunriseOrange
        ChartMetric.WIND -> RiskLowGreen
        ChartMetric.HUMIDITY -> PurpleAccent
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val paddingTop = 25f
        val chartHeight = height - paddingBottom - paddingTop
        val stepX = width / (items.size - 1).coerceAtLeast(1)

        val points = values.mapIndexed { index, v ->
            val x = index * stepX
            val normalized = (v - minVal) / range
            val y = height - paddingBottom - (normalized * chartHeight)
            Offset(x, y)
        }

        // Draw Cubic Bezier Path
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX = (p0.x + p1.x) / 2
                    cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }
            }
        }

        // Gradient Area Fill
        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, height - paddingBottom)
            lineTo(points.first().x, height - paddingBottom)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = paddingTop,
                endY = height - paddingBottom
            )
        )

        // Draw Stroke Line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw Points & Values
        points.forEachIndexed { i, pt ->
            // Point dot
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }

    // Time & Value Labels placed in overlay row
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        items.forEachIndexed { index, item ->
            val v = values[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(56.dp)
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = "${v.toInt()}${metric.unit}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.hourLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun generateChartSummary(hourly: List<HourlyForecastItem>, metric: ChartMetric): String {
    if (hourly.isEmpty()) return "Hourly data is updating."
    return when (metric) {
        ChartMetric.RAIN_PROB -> {
            val highRain = hourly.take(12).filter { it.rainProbability >= 45 }
            if (highRain.isNotEmpty()) {
                val start = highRain.first().hourLabel
                val end = highRain.last().hourLabel
                "Rain probability increases between $start and $end (peaks at ${highRain.maxOf { it.rainProbability }}%)."
            } else {
                "Rain probability remains low (< 25%) throughout the upcoming 12 hours."
            }
        }
        ChartMetric.TEMPERATURE -> {
            val maxItem = hourly.take(16).maxByOrNull { it.tempC }
            val minItem = hourly.take(16).minByOrNull { it.tempC }
            "Peak warmth reaches ${maxItem?.tempC?.toInt()}°C around ${maxItem?.hourLabel}, cooling to ${minItem?.tempC?.toInt()}°C near ${minItem?.hourLabel}."
        }
        ChartMetric.WIND -> {
            val maxWind = hourly.take(12).maxByOrNull { it.windSpeedKmh }
            "Wind speeds peak at ${maxWind?.windSpeedKmh?.toInt()} km/h around ${maxWind?.hourLabel}."
        }
        ChartMetric.HUMIDITY -> {
            val maxHum = hourly.take(12).maxByOrNull { it.humidityPercent }
            "Humidity is highest (${maxHum?.humidityPercent}%) in the early morning hours."
        }
    }
}

@Composable
fun SevenDayForecastCard(
    daily: List<DailyForecastItem>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("seven_day_forecast_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📅 7-Day Forecast",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            daily.forEach { day ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Day of week
                    Text(
                        text = day.dayOfWeek,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(70.dp)
                    )

                    // Condition Text & Rain Chance
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = day.conditionText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        if (day.rainProbMax > 20) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "💧 ${day.rainProbMax}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SkyBlueLight
                            )
                        }
                    }

                    // Temp Range Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.width(110.dp)
                    ) {
                        Text(
                            text = "${day.minTempC.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Mini indicator bar
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(5.dp)
                                .background(
                                    brush = Brush.horizontalGradient(listOf(SkyBluePrimary, SunriseOrange)),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${day.maxTempC.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}
