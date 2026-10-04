package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LifestyleType
import com.example.data.model.SafetyStatus
import com.example.data.repository.WeatherUtils
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState

@Composable
fun LifestyleScreen(
    uiState: MausamUiState,
    onToggleLifestyle: (LifestyleType) -> Unit,
    modifier: Modifier = Modifier
) {
    val weather = uiState.currentWeather
    val aqi = uiState.airQuality
    val hourly = uiState.hourlyForecast
    val temp = weather?.temperatureC ?: 29.0
    val humidity = weather?.humidityPercent ?: 65
    val wind = weather?.windSpeedKmh ?: 14.0
    val uv = weather?.uvIndex ?: 5.0
    val rainMax6h = hourly.take(6).maxOfOrNull { it.rainProbability } ?: 20
    val morningRainProb = hourly.filter { it.hourLabel.contains("8 AM") || it.hourLabel.contains("9 AM") || it.hourLabel.contains("10 AM") }.maxOfOrNull { it.rainProbability } ?: 15
    val eveningRainProb = hourly.filter { it.hourLabel.contains("5 PM") || it.hourLabel.contains("6 PM") || it.hourLabel.contains("7 PM") || it.hourLabel.contains("8 PM") }.maxOfOrNull { it.rainProbability } ?: 25

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("lifestyle_screen_container")
    ) {
        // Title & Intro
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "🎯 8 Smart Lifestyles",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Personalized atmospheric intelligence across all 8 aspects of your daily life",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 8 Selectable Lifestyles Chips
        item {
            Text(
                text = "Toggle Active Lifestyles",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LifestyleChip(LifestyleType.HEALTH, uiState.selectedLifestyles.contains(LifestyleType.HEALTH), onToggleLifestyle, Modifier.weight(1f))
                    LifestyleChip(LifestyleType.FITNESS, uiState.selectedLifestyles.contains(LifestyleType.FITNESS), onToggleLifestyle, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LifestyleChip(LifestyleType.BEACH, uiState.selectedLifestyles.contains(LifestyleType.BEACH), onToggleLifestyle, Modifier.weight(1f))
                    LifestyleChip(LifestyleType.TRAVEL, uiState.selectedLifestyles.contains(LifestyleType.TRAVEL), onToggleLifestyle, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LifestyleChip(LifestyleType.FAMILY, uiState.selectedLifestyles.contains(LifestyleType.FAMILY), onToggleLifestyle, Modifier.weight(1f))
                    LifestyleChip(LifestyleType.AGRICULTURE, uiState.selectedLifestyles.contains(LifestyleType.AGRICULTURE), onToggleLifestyle, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LifestyleChip(LifestyleType.COMMUTER, uiState.selectedLifestyles.contains(LifestyleType.COMMUTER), onToggleLifestyle, Modifier.weight(1f))
                    LifestyleChip(LifestyleType.EVENTS, uiState.selectedLifestyles.contains(LifestyleType.EVENTS), onToggleLifestyle, Modifier.weight(1f))
                }
            }
        }

        // =========================================================================
        // 1. HEALTH DASHBOARD
        // =========================================================================
        item {
            LifestyleDashboardCard(
                emoji = "❤️",
                title = "Health & Air Quality",
                scoreText = "${aqi?.comfortScore ?: 86}/100 Score",
                scoreColor = RiskLowGreen,
                stat1 = Pair("AQI", "${aqi?.aqi ?: 38} (${aqi?.aqiCategory ?: "Good"})"),
                stat2 = Pair("PM2.5", "${aqi?.pm25 ?: 12.0} µg/m³"),
                stat3 = Pair("PM10", "${aqi?.pm10 ?: 24.0} µg/m³"),
                stat4 = Pair("UV Index", "${weather?.uvIndex?.toInt() ?: 5}"),
                explanation = aqi?.comfortExplanation ?: "Air quality is favorable for outdoor breathing and general wellness.",
                note = "Note: Pollen information is unavailable for this location. MAUSAM provides atmospheric insights, not medical diagnosis."
            )
        }

        // =========================================================================
        // 2. FITNESS DASHBOARD
        // =========================================================================
        item {
            val fitScore = WeatherUtils.calculateFitnessScore(
                tempC = temp,
                humidityPercent = humidity,
                windKmh = wind,
                rainProb = rainMax6h,
                uvIndex = uv
            )

            LifestyleDashboardCard(
                emoji = "🏃",
                title = "Fitness & Workouts",
                scoreText = "${fitScore.first}/100 Comfort",
                scoreColor = SkyBlueLight,
                stat1 = Pair("Best Run Window", "6:00 - 7:30 AM"),
                stat2 = Pair("Best Walk Window", "5:30 - 6:45 PM"),
                stat3 = Pair("Ambient Temp", "${temp.toInt()}°C"),
                stat4 = Pair("Humidity", "$humidity%"),
                explanation = fitScore.second,
                note = "Hydration recommendation: Drink at least 500ml water prior to strenuous cardio in current humidity."
            )
        }

        // =========================================================================
        // 3. BEACH DASHBOARD
        // =========================================================================
        item {
            val nearestBeach = uiState.nearbyBeaches.firstOrNull()
            val surfStatus = nearestBeach?.safetyStatus ?: SafetyStatus.SUITABLE
            val surfScore = when (surfStatus) {
                SafetyStatus.SUITABLE -> 88
                SafetyStatus.CAUTION -> 65
                SafetyStatus.HIGH_RISK -> 35
            }
            val surfColor = when (surfStatus) {
                SafetyStatus.SUITABLE -> RiskLowGreen
                SafetyStatus.CAUTION -> RiskModerateYellow
                SafetyStatus.HIGH_RISK -> RiskSevereRed
            }

            LifestyleDashboardCard(
                emoji = "🏖️",
                title = "Beach & Coastal Safety",
                scoreText = "$surfScore/100 Surf Score",
                scoreColor = surfColor,
                stat1 = Pair("Nearest Beach", nearestBeach?.name?.take(16) ?: "Covelong"),
                stat2 = Pair("Wave Height", "${String.format("%.1f", nearestBeach?.waveHeightM ?: 1.0)} m"),
                stat3 = Pair("Coastal Wind", "${nearestBeach?.windSpeedKmh?.toInt() ?: wind.toInt()} km/h"),
                stat4 = Pair("Surf Status", when (surfStatus) {
                    SafetyStatus.SUITABLE -> "Calm Surf"
                    SafetyStatus.CAUTION -> "Moderate Chop"
                    SafetyStatus.HIGH_RISK -> "Rough Waves"
                }),
                explanation = if (surfStatus == SafetyStatus.SUITABLE)
                    "Coastal conditions are pleasant with moderate wave chop. Ideal for beach strolls and calm swimming."
                else
                    "Higher waves and gusty winds observed. Check sheltered bay alternatives or plan for sunset hours.",
                note = "Always swim in designated lifeguard zones. UV index is ${uv.toInt()} — wear water-resistant SPF 50+."
            )
        }

        // =========================================================================
        // 4. TRAVEL DASHBOARD
        // =========================================================================
        item {
            val travelScore = (100 - (if (rainMax6h > 40) rainMax6h * 0.4 else 0.0) - (if (wind > 25) (wind - 25) * 1.5 else 0.0)).toInt().coerceIn(40, 95)
            val travelColor = if (travelScore >= 75) RiskLowGreen else RiskModerateYellow

            LifestyleDashboardCard(
                emoji = "✈️",
                title = "Travel & Highway Driving",
                scoreText = "$travelScore/100 Road Comfort",
                scoreColor = travelColor,
                stat1 = Pair("Visibility", "${weather?.visibilityKm?.toInt() ?: 10} km"),
                stat2 = Pair("Rain on Highway", "$rainMax6h% Max"),
                stat3 = Pair("Crosswind", "${wind.toInt()} km/h"),
                stat4 = Pair("Road Grip", if (rainMax6h >= 50) "Wet Surface" else "Dry Surface"),
                explanation = if (rainMax6h >= 50)
                    "Scattered rain along highway corridors may reduce braking distance. Maintain safe vehicle following distance."
                else
                    "Clear visibility and dry road surfaces along major state and national highways.",
                note = "Use the dedicated Travel Planner tab to compare direct routes with expressway alternatives."
            )
        }

        // =========================================================================
        // 5. FAMILY & KIDS DASHBOARD
        // =========================================================================
        item {
            val familyScore = (100 - (temp - 27).coerceAtLeast(0.0) * 3.5 - (humidity - 65).coerceAtLeast(0) * 0.5 - (rainMax6h * 0.4)).toInt().coerceIn(35, 96)
            val familyColor = if (familyScore >= 75) RiskLowGreen else if (familyScore >= 55) RiskModerateYellow else RiskHighOrange

            LifestyleDashboardCard(
                emoji = "👨‍👩‍👧",
                title = "Family & Kids Outings",
                scoreText = "$familyScore/100 Park Score",
                scoreColor = familyColor,
                stat1 = Pair("Playground Time", "5:00 - 6:45 PM"),
                stat2 = Pair("Stroller Comfort", if (temp > 33) "Hot" else "Pleasant"),
                stat3 = Pair("Mosquito Risk", if (humidity > 75) "Moderate" else "Low"),
                stat4 = Pair("Rain Risk", "$rainMax6h%"),
                explanation = if (temp > 33)
                    "Midday is too warm for playground slides and open turf. Evening (after 5 PM) is ideal for park visits."
                else
                    "Comfortable temperature and gentle breeze make this a great day for garden walks and outdoor play.",
                note = "Carry light jackets if staying outdoors past sunset, and keep young kids hydrated."
            )
        }

        // =========================================================================
        // 6. AGRICULTURE & CROPS DASHBOARD
        // =========================================================================
        item {
            val agriRainRisk = uiState.dailyForecast.getOrNull(1)?.rainProbMax ?: 20
            val agriScore = if (agriRainRisk >= 50 || humidity >= 80) 62 else 85
            val agriColor = if (agriScore >= 75) RiskLowGreen else RiskModerateYellow

            LifestyleDashboardCard(
                emoji = "🌱",
                title = "Agriculture & Farming",
                scoreText = if (agriRainRisk >= 50) "Rain Alert" else "Favorable",
                scoreColor = agriColor,
                stat1 = Pair("Active Field", uiState.activeFieldLocation.locality),
                stat2 = Pair("Next Day Rain", "$agriRainRisk%"),
                stat3 = Pair("Soil Humidity", "$humidity%"),
                stat4 = Pair("Fungal Risk", if (humidity >= 75) "Moderate" else "Low"),
                explanation = if (agriRainRisk >= 50)
                    "Rainfall expected tomorrow ($agriRainRisk%). Avoid unnecessary flood irrigation today and inspect drainage bunds."
                else
                    "Favorable field conditions. Proceed with routine field scouting, light weeding, and scheduled drip irrigation.",
                note = "Upload a leaf photo in the Crops section to diagnose specific leaf spots and disease indicators."
            )
        }

        // =========================================================================
        // 7. COMMUTER DASHBOARD
        // =========================================================================
        item {
            val commuteScore = (100 - (eveningRainProb * 0.4) - (morningRainProb * 0.3) - (wind * 0.5)).toInt().coerceIn(45, 96)
            val commuteColor = if (commuteScore >= 75) RiskLowGreen else RiskModerateYellow

            LifestyleDashboardCard(
                emoji = "🚗",
                title = "Commuter & Daily Transit",
                scoreText = "$commuteScore/100 Transit Score",
                scoreColor = commuteColor,
                stat1 = Pair("Morning 8-10 AM", "$morningRainProb% Rain"),
                stat2 = Pair("Evening 5-8 PM", "$eveningRainProb% Rain"),
                stat3 = Pair("Wind Gusts", "${wind.toInt()} km/h"),
                stat4 = Pair("2-Wheeler Comfort", if (eveningRainProb > 45) "Carry Raincoat" else "Comfortable"),
                explanation = if (eveningRainProb >= 50)
                    "Evening commute has elevated rain chance ($eveningRainProb%). Two-wheeler riders should pack rain protection."
                else
                    "Smooth daily commute expected with minimal weather-induced traffic delays during peak rush hours.",
                note = "Plan 10 minutes buffer time if traveling through waterlogged urban bottlenecks."
            )
        }

        // =========================================================================
        // 8. EVENTS & CELEBRATIONS DASHBOARD
        // =========================================================================
        item {
            val eventScore = (100 - (rainMax6h * 0.6) - (if (wind > 22) (wind - 22) * 1.8 else 0.0)).toInt().coerceIn(35, 98)
            val eventColor = if (eventScore >= 75) RiskLowGreen else if (eventScore >= 55) RiskModerateYellow else RiskHighOrange

            LifestyleDashboardCard(
                emoji = "🎉",
                title = "Events & Outdoor Gatherings",
                scoreText = "$eventScore/100 Feasibility",
                scoreColor = eventColor,
                stat1 = Pair("Open-Air Feasibility", if (eventScore >= 75) "High" else "Moderate"),
                stat2 = Pair("Evening Rain", "$eveningRainProb%"),
                stat3 = Pair("Lawn Comfort", "${temp.toInt()}°C"),
                stat4 = Pair("Tent Wind Risk", if (wind >= 25) "Secure Canopies" else "Safe"),
                explanation = if (eveningRainProb >= 50)
                    "Moderate rain chance during evening hours ($eveningRainProb%). Keep waterproof shamiana or indoor banquet backup ready."
                else
                    "Delightful evening climate for lawn parties, rooftop dinners, and outdoor wedding ceremonies.",
                note = "Ensure adequate decorative lighting and anchor lightweight promotional banners securely."
            )
        }
    }
}

@Composable
private fun LifestyleDashboardCard(
    emoji: String,
    title: String,
    scoreText: String,
    scoreColor: androidx.compose.ui.graphics.Color,
    stat1: Pair<String, String>,
    stat2: Pair<String, String>,
    stat3: Pair<String, String>,
    stat4: Pair<String, String>,
    explanation: String,
    note: String
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = scoreColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = scoreText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = scoreColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatBox(stat1.first, stat1.second)
                StatBox(stat2.first, stat2.second)
                StatBox(stat3.first, stat3.second)
                StatBox(stat4.first, stat4.second)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LifestyleChip(
    type: LifestyleType,
    isSelected: Boolean,
    onToggle: (LifestyleType) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = { onToggle(type) },
        leadingIcon = { Text(type.emoji, fontSize = 16.sp) },
        label = { Text(type.label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SkyBluePrimary.copy(alpha = 0.2f),
            selectedLabelColor = SkyBlueLight
        ),
        modifier = modifier
    )
}

@Composable
private fun StatBox(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}
