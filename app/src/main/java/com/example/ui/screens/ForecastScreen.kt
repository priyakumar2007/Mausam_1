package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.ui.components.ChartMetric
import com.example.ui.components.WeatherSmoothLineChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState

@Composable
fun ForecastScreen(
    uiState: MausamUiState,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("forecast_screen_container")
    ) {
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "📅 7-Day & Hourly Forecast",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap any day to inspect detailed hourly weather trends",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Horizontal Day Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.dailyForecast.size) { index ->
                    val day = uiState.dailyForecast[index]
                    val isSelected = selectedDayIndex == index

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .width(86.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedDayIndex = index }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = day.dayOfWeek,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (day.weatherCode) {
                                    0 -> "☀️"
                                    1, 2 -> "🌤️"
                                    3 -> "☁️"
                                    61, 63, 65 -> "🌧️"
                                    else -> "🌦️"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${day.maxTempC.toInt()}° / ${day.minTempC.toInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Detailed Selected Day Breakdown
        val selectedDay = uiState.dailyForecast.getOrNull(selectedDayIndex)
        if (selectedDay != null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "${selectedDay.dayOfWeek} Forecast",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = selectedDay.conditionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SkyBlueLight
                                )
                            }
                            Text(
                                text = "💧 ${selectedDay.rainProbMax}% rain max",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SkyBlueLight
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DayMetric("Max Temp", "${selectedDay.maxTempC.toInt()}°C")
                            DayMetric("Min Temp", "${selectedDay.minTempC.toInt()}°C")
                            DayMetric("Max Wind", "${selectedDay.windSpeedMaxKmh.toInt()} km/h")
                            DayMetric("UV Max", "${selectedDay.uvMax.toInt()}")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DayMetric("Sunrise", selectedDay.sunrise)
                            DayMetric("Sunset", selectedDay.sunset)
                            DayMetric("Location", uiState.currentLocation.locality)
                        }
                    }
                }
            }
        }

        // Hourly Breakdown for the day
        item {
            Text(
                text = "⏱️ Hourly Temperature & Rain Chart",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(12.dp)
                ) {
                    WeatherSmoothLineChart(
                        items = uiState.hourlyForecast.take(12),
                        metric = ChartMetric.TEMPERATURE,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun DayMetric(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}
