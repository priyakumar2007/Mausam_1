package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.CurrentWeather
import com.example.ui.theme.*

@Composable
fun MainHeroWeatherCard(
    weather: CurrentWeather?,
    isOffline: Boolean,
    lastUpdated: String,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    if (weather == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(DarkSurfaceCard, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SkyBlueLight)
        }
        return
    }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_hero_weather_card")
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(WeatherGradientStart, WeatherGradientMid, WeatherGradientEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Offline Banner if offline
                if (isOffline) {
                    Surface(
                        color = RiskModerateYellow.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline",
                                tint = RiskModerateYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val offlineLabel = when (language) {
                                AppLanguage.TAMIL -> "ஆஃப்லைன் முறை • கடைசியாக புதுப்பிக்கப்பட்டது $lastUpdated"
                                AppLanguage.TANGLISH -> "Offline Mode • Last updated $lastUpdated"
                                AppLanguage.ENGLISH -> "OFFLINE MODE • Last updated $lastUpdated"
                            }
                            Text(
                                text = offlineLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Row 1: Condition Text & Weather Emoji / Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = weather.conditionText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${AppStrings.getFeelsLike(language)} ${weather.feelsLikeC.toInt()}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Weather Icon / Emoji
                    Text(
                        text = when (weather.weatherCode) {
                            0 -> "☀️"
                            1, 2 -> "🌤️"
                            3 -> "☁️"
                            61, 63, 65 -> "🌧️"
                            80, 81, 82 -> "🌦️"
                            95, 96 -> "⛈️"
                            else -> "🌤️"
                        },
                        fontSize = 46.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 2: Massive Current Temperature
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${weather.temperatureC.toInt()}°",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 68.sp,
                            lineHeight = 72.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = SkyBlueContainer,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // Row 3: Weather metrics grid (Humidity, Wind, UV, Pressure)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WeatherStatItem(
                        icon = Icons.Default.WaterDrop,
                        label = AppStrings.getHumidity(language),
                        value = "${weather.humidityPercent}%"
                    )
                    WeatherStatItem(
                        icon = Icons.Default.Air,
                        label = AppStrings.getWind(language),
                        value = "${weather.windSpeedKmh.toInt()} km/h"
                    )
                    WeatherStatItem(
                        icon = Icons.Default.WbSunny,
                        label = AppStrings.getUvIndex(language),
                        value = "${weather.uvIndex.toInt()}"
                    )
                    WeatherStatItem(
                        icon = Icons.Default.Speed,
                        label = if (language == AppLanguage.TAMIL) "அழுத்தம்" else "Pressure",
                        value = "${weather.surfacePressureHpa.toInt()} hPa"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 4: Sunrise, Sunset, Visibility
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WeatherStatItem(
                        icon = Icons.Default.WbTwilight,
                        label = if (language == AppLanguage.TAMIL) "சூரிய உதயம்" else "Sunrise",
                        value = weather.sunrise
                    )
                    WeatherStatItem(
                        icon = Icons.Default.NightsStay,
                        label = if (language == AppLanguage.TAMIL) "சூரிய அஸ்தமனம்" else "Sunset",
                        value = weather.sunset
                    )
                    WeatherStatItem(
                        icon = Icons.Default.Visibility,
                        label = if (language == AppLanguage.TAMIL) "பார்வை தூரம்" else "Visibility",
                        value = "${weather.visibilityKm.toInt()} km"
                    )
                    WeatherStatItem(
                        icon = Icons.Default.Cloud,
                        label = if (language == AppLanguage.TAMIL) "மேகமூட்டம்" else "Clouds",
                        value = "${weather.cloudCoverPercent}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = SkyBlueContainer,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
fun MausamInsightCard(
    insightText: String,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("mausam_insight_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🧠",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                val title = when (language) {
                    AppLanguage.TAMIL -> "வானிலை நுண்ணறிவு (AI Insight)"
                    AppLanguage.TANGLISH -> "MAUSAM AI Insight"
                    AppLanguage.ENGLISH -> "MAUSAM AI Insight"
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SkyBlueLight
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = insightText.ifBlank { "Analyzing atmospheric patterns for your locality..." },
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
