package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.BeachSafetyInfo
import com.example.data.model.SafetyStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState

@Composable
fun BeachScreen(
    uiState: MausamUiState,
    onSelectBeach: (BeachSafetyInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedBeach = uiState.selectedBeach ?: uiState.nearbyBeaches.firstOrNull()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("beach_screen_container")
    ) {
        // Title
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "🏖️ Coastal Beaches & Marine Safety",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Live wave heights, surf safety, winds and calmer alternatives",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Beach Friend Mode Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌊", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Beach Friend Advisory",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SkyBlueLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val nearest = uiState.nearbyBeaches.firstOrNull()
                    val friendMessage = if (nearest != null) {
                        if (nearest.safetyStatus == SafetyStatus.SUITABLE) {
                            "Hey! 🌊 You're exploring coastal beaches. Your nearest beach is ${nearest.name} (${nearest.distanceKm} km away). Waves are gentle at ${String.format("%.1f", nearest.waveHeightM ?: 0.9)}m with light breeze — perfect conditions for an outing!"
                        } else {
                            "Hey! 🌊 Your nearest beach is ${nearest.name} (${nearest.distanceKm} km away). Wind is currently elevated and surf is choppy. Consider checking sheltered alternatives below for calmer waters."
                        }
                    } else {
                        "Scanning nearby coastal shores and wave monitoring stations..."
                    }

                    Text(
                        text = friendMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Selected Beach Detailed Card
        if (selectedBeach != null) {
            item {
                val statusColor = when (selectedBeach.safetyStatus) {
                    SafetyStatus.SUITABLE -> RiskLowGreen
                    SafetyStatus.CAUTION -> RiskModerateYellow
                    SafetyStatus.HIGH_RISK -> RiskSevereRed
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedBeach.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${selectedBeach.distanceKm} km from you",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SkyBlueLight
                                )
                            }

                            // Safety Status Badge
                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = when (selectedBeach.safetyStatus) {
                                        SafetyStatus.SUITABLE -> "Suitable Conditions"
                                        SafetyStatus.CAUTION -> "Caution Advised"
                                        SafetyStatus.HIGH_RISK -> "High-Risk Surf"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Marine Metrics Grid (Wave Height, Wind, UV, Water Temp)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MarineMetricItem(
                                label = "Wave Height",
                                value = "${String.format("%.1f", selectedBeach.waveHeightM ?: 1.0)} m",
                                highlight = (selectedBeach.waveHeightM ?: 1.0) > 1.8
                            )
                            MarineMetricItem(
                                label = "Wave Period",
                                value = "${selectedBeach.wavePeriodSec?.toInt() ?: 7} sec",
                                highlight = false
                            )
                            MarineMetricItem(
                                label = "Wind Speed",
                                value = "${selectedBeach.windSpeedKmh.toInt()} km/h",
                                highlight = selectedBeach.windSpeedKmh > 24
                            )
                            MarineMetricItem(
                                label = "Water Temp",
                                value = "${selectedBeach.waterTempC?.toInt() ?: 28}°C",
                                highlight = false
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Safety Assessment & Reasons
                        Text(
                            text = "Safety Assessment:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        selectedBeach.safetyReasons.forEach { reason ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = statusColor, fontWeight = FontWeight.Bold)
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Alternative Day Recommendation
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📅 Alternative Timing & Forecast Note",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SkyBlueLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val timingNote = if (selectedBeach.safetyStatus == SafetyStatus.SUITABLE) {
                                    "Conditions remain favorable today until sunset. Midday UV is high, so morning or dusk is best."
                                } else {
                                    "Today has higher wave chop. Upcoming Wednesday/Thursday has calmer forecasted wave heights (~0.8m) and lighter wind."
                                }
                                Text(
                                    text = timingNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nearby Beaches List (with Calmer Alternative highlight)
        item {
            Text(
                text = "🏖️ All Nearby Beaches (Sorted by Distance)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(uiState.nearbyBeaches) { beach ->
            val isCurrentSelected = beach.beachId == selectedBeach?.beachId
            val statusColor = when (beach.safetyStatus) {
                SafetyStatus.SUITABLE -> RiskLowGreen
                SafetyStatus.CAUTION -> RiskModerateYellow
                SafetyStatus.HIGH_RISK -> RiskSevereRed
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentSelected) 3.dp else 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectBeach(beach) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = beach.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (beach.isRecommendedAlternative) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = RiskLowGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "★ Calmer Alternative",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = RiskLowGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${beach.distanceKm} km away • Waves ${String.format("%.1f", beach.waveHeightM ?: 1.0)}m • Wind ${beach.windSpeedKmh.toInt()} km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (beach.safetyStatus) {
                                SafetyStatus.SUITABLE -> "Calm"
                                SafetyStatus.CAUTION -> "Caution"
                                SafetyStatus.HIGH_RISK -> "Rough"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarineMetricItem(
    label: String,
    value: String,
    highlight: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (highlight) RiskHighOrange else MaterialTheme.colorScheme.onSurface
        )
    }
}
