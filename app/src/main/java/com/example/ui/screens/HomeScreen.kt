package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.OfflineAdvisorEngine
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState

@Composable
fun HomeScreen(
    uiState: MausamUiState,
    onNavigateTab: (String) -> Unit,
    onLocationClick: () -> Unit,
    onAlertsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.language

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen_container")
    ) {
        // Severe Alerts First
        val severeAlerts = uiState.alerts.filter { it.severity == AlertSeverity.SEVERE }
        if (severeAlerts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    severeAlerts.forEach { alert ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = RiskSevereRed.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onAlertsClick() }
                                .testTag("severe_alert_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Text(text = "🚨", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = RiskSevereRed
                                    )
                                    Text(
                                        text = alert.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View Alerts",
                                    tint = RiskSevereRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main Weather Card (Localized)
        item {
            MainHeroWeatherCard(
                weather = uiState.currentWeather,
                isOffline = uiState.isOffline,
                lastUpdated = uiState.lastUpdated,
                language = lang
            )
        }

        // Active Offline Suggestions when Internet is Offline
        if (uiState.isOffline) {
            item {
                OfflineSuggestionsCard(
                    weather = uiState.currentWeather,
                    location = uiState.currentLocation,
                    language = lang
                )
            }
        }

        // MAUSAM Insight Natural Language Summary (Localized)
        item {
            MausamInsightCard(
                insightText = uiState.mausamInsight,
                language = lang
            )
        }

        // Quick Actions Section (Localized)
        item {
            QuickActionsSection(
                onCropRiskClick = { onNavigateTab("agriculture") },
                onTravelClick = { onNavigateTab("travel") },
                onBeachClick = { onNavigateTab("beach") },
                onLocationClick = onLocationClick,
                onLifestyleClick = { onNavigateTab("lifestyle") },
                language = lang
            )
        }

        // Lifestyle Highlight Cards (Localized)
        item {
            LifestyleSummarySection(
                airQuality = uiState.airQuality,
                weather = uiState.currentWeather,
                selectedLifestyles = uiState.selectedLifestyles,
                language = lang,
                onExploreLifestyle = { onNavigateTab("lifestyle") }
            )
        }

        // Smart Suggestions based on real weather variables (Localized)
        item {
            SmartSuggestionsSection(
                weather = uiState.currentWeather,
                hourly = uiState.hourlyForecast,
                language = lang
            )
        }

        // Live Weather Plot (Interactive Canvas)
        item {
            LiveWeatherPlotCard(hourly = uiState.hourlyForecast)
        }

        // 7-Day Forecast Card
        item {
            SevenDayForecastCard(daily = uiState.dailyForecast)
        }
    }
}

@Composable
private fun LifestyleSummarySection(
    airQuality: AirQualityInfo?,
    weather: CurrentWeather?,
    selectedLifestyles: Set<LifestyleType>,
    language: AppLanguage,
    onExploreLifestyle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temp = weather?.temperatureC ?: 29.0
    val humidity = weather?.humidityPercent ?: 65
    val wind = weather?.windSpeedKmh ?: 14.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = AppStrings.getLifestyleInsightsTitle(language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onExploreLifestyle) {
                Text(AppStrings.getViewAll(language), color = SkyBlueLight)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Health
            item {
                HomeLifestyleCard(
                    emoji = "❤️",
                    title = if (language == AppLanguage.TAMIL) "உடல்நலம்" else "Health & Air",
                    metric = "AQI ${airQuality?.aqi ?: 38} • ${airQuality?.aqiCategory ?: "Good"}",
                    subtext = "Comfort: ${airQuality?.comfortScore ?: 86}/100",
                    accentColor = RiskLowGreen,
                    onClick = onExploreLifestyle
                )
            }

            // 2. Fitness
            item {
                val fitnessScore = (100 - (temp - 26).coerceAtLeast(0.0) * 4 - (humidity - 60).coerceAtLeast(0) * 0.5).toInt().coerceIn(40, 98)
                HomeLifestyleCard(
                    emoji = "🏃",
                    title = if (language == AppLanguage.TAMIL) "உடற்பயிற்சி" else "Fitness & Run",
                    metric = "$fitnessScore/100 Comfort",
                    subtext = "Best: 6:00 - 7:30 AM",
                    accentColor = SkyBlueLight,
                    onClick = onExploreLifestyle
                )
            }

            // 3. Beach
            item {
                HomeLifestyleCard(
                    emoji = "🏖️",
                    title = if (language == AppLanguage.TAMIL) "கடற்கரை" else "Coastal Beach",
                    metric = "Calm Surf • 0.9m",
                    subtext = "Wind: ${wind.toInt()} km/h",
                    accentColor = SunriseOrange,
                    onClick = onExploreLifestyle
                )
            }

            // 4. Travel
            item {
                HomeLifestyleCard(
                    emoji = "✈️",
                    title = if (language == AppLanguage.TAMIL) "பயணம்" else "Highway Travel",
                    metric = "Dry Road • High Grip",
                    subtext = "GPS Tracking Ready",
                    accentColor = IndigoAccent,
                    onClick = onExploreLifestyle
                )
            }

            // 5. Family
            item {
                val familyScore = (100 - (temp - 27).coerceAtLeast(0.0) * 3 - (humidity - 65).coerceAtLeast(0) * 0.4).toInt().coerceIn(40, 96)
                HomeLifestyleCard(
                    emoji = "👨‍👩‍👧",
                    title = if (language == AppLanguage.TAMIL) "குடும்பம்" else "Family & Parks",
                    metric = "$familyScore/100 Outing",
                    subtext = "Best after 5:00 PM",
                    accentColor = PurpleAccent,
                    onClick = onExploreLifestyle
                )
            }

            // 6. Agriculture
            item {
                HomeLifestyleCard(
                    emoji = "🌱",
                    title = if (language == AppLanguage.TAMIL) "பயிர் பாதுகாப்பு" else "Crop & Soil",
                    metric = "Favorable",
                    subtext = "Check drainage channels",
                    accentColor = RiskLowGreen,
                    onClick = onExploreLifestyle
                )
            }

            // 7. Commuter
            item {
                HomeLifestyleCard(
                    emoji = "🚗",
                    title = if (language == AppLanguage.TAMIL) "தினசரி பயணம்" else "Daily Transit",
                    metric = "Smooth Commute",
                    subtext = "Low peak-hour rain",
                    accentColor = SkyBluePrimary,
                    onClick = onExploreLifestyle
                )
            }

            // 8. Events
            item {
                HomeLifestyleCard(
                    emoji = "🎉",
                    title = if (language == AppLanguage.TAMIL) "வெளிப்புற விழா" else "Outdoor Events",
                    metric = "90/100 Feasibility",
                    subtext = "Pleasant evening lawn",
                    accentColor = SunsetGold,
                    onClick = onExploreLifestyle
                )
            }
        }
    }
}

@Composable
private fun HomeLifestyleCard(
    emoji: String,
    title: String,
    metric: String,
    subtext: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = metric,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = accentColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SmartSuggestionsSection(
    weather: CurrentWeather?,
    hourly: List<HourlyForecastItem>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    if (weather == null) return

    val suggestions = mutableListOf<Pair<String, String>>()

    val highRain = hourly.take(12).filter { it.rainProbability >= 45 }
    if (highRain.isNotEmpty()) {
        val title = when (language) {
            AppLanguage.TAMIL -> "🌧️ மழை எச்சரிக்கை"
            AppLanguage.TANGLISH -> "🌧️ Rain Advisory"
            AppLanguage.ENGLISH -> "🌧️ Rain Advisory"
        }
        val desc = when (language) {
            AppLanguage.TAMIL -> "${highRain.first().hourLabel} மணியளவில் மழை பெய்ய வாய்ப்புள்ளது. குடை எடுத்துச் செல்லவும்."
            AppLanguage.TANGLISH -> "Rain chance peaks near ${highRain.first().hourLabel}. Umbrella carry pannunga."
            AppLanguage.ENGLISH -> "Rain probability peaks near ${highRain.first().hourLabel}. Carrying an umbrella is recommended."
        }
        suggestions.add(Pair(title, desc))
    }

    if (weather.uvIndex >= 7.0) {
        val title = when (language) {
            AppLanguage.TAMIL -> "☀️ வெயில் பாதுகாப்பு"
            AppLanguage.TANGLISH -> "☀️ Sun Protection"
            AppLanguage.ENGLISH -> "☀️ Sun Protection"
        }
        val desc = when (language) {
            AppLanguage.TAMIL -> "நண்பகலில் வெயில் தாக்கம் அதிகமாக இருக்கும். சன்ஸ்கிரீன் அல்லது தொப்பி அணியவும்."
            AppLanguage.TANGLISH -> "Midday UV athigama irukum. Sunscreen or cap use pannunga."
            AppLanguage.ENGLISH -> "UV radiation is elevated around midday. Reduce prolonged direct sun exposure."
        }
        suggestions.add(Pair(title, desc))
    } else {
        val title = when (language) {
            AppLanguage.TAMIL -> "🌿 வெளிப்புற வானிலை"
            AppLanguage.TANGLISH -> "🌿 Outdoor Air"
            AppLanguage.ENGLISH -> "🌿 Outdoor Air"
        }
        val desc = when (language) {
            AppLanguage.TAMIL -> "காலை வேளையில் நடைபயிற்சிக்கு இதமான சூழல் நிலவுகிறது."
            AppLanguage.TANGLISH -> "Morning walk-ku climate romba pleasant-ah irukku."
            AppLanguage.ENGLISH -> "Morning conditions are comfortable for routine outdoor activity."
        }
        suggestions.add(Pair(title, desc))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val sectionTitle = when (language) {
                AppLanguage.TAMIL -> "🧠 ஸ்மார்ட் பரிந்துரைகள்"
                AppLanguage.TANGLISH -> "🧠 Smart Suggestions"
                AppLanguage.ENGLISH -> "🧠 Smart Suggestions"
            }
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            suggestions.forEach { (title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "•",
                        fontWeight = FontWeight.Bold,
                        color = SkyBlueLight,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineSuggestionsCard(
    weather: CurrentWeather?,
    location: ExactLocation,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val items = OfflineAdvisorEngine.getOfflineSuggestions(weather, location, language)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_suggestions_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📡", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                val title = when (language) {
                    AppLanguage.TAMIL -> "ஆஃப்லைன் வழிகாட்டல் & பாதுகாப்பு (Offline Advice)"
                    AppLanguage.TANGLISH -> "Offline Smart Suggestions & Safety"
                    AppLanguage.ENGLISH -> "Offline Smart Suggestions & Safety"
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { item ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 ${item.actionableTip}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SkyBlueLight
                        )
                    }
                }
            }
        }
    }
}

