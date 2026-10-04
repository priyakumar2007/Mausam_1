package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.data.repository.EmergencyNotificationHelper
import com.example.data.repository.OfflineAdvisorEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState

@Composable
fun AlertsScreen(
    uiState: MausamUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = uiState.language

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            triggerSampleEmergencyNotification(context, uiState)
        } else {
            Toast.makeText(context, "Notification permission is needed for emergency alerts", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestAndSendNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                triggerSampleEmergencyNotification(context, uiState)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            triggerSampleEmergencyNotification(context, uiState)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("alerts_screen_container")
    ) {
        // Title & Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = AppStrings.getAlertsTitle(lang),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (lang == AppLanguage.TAMIL) {
                        "அதிகாரப்பூர்வ அரசு எச்சரிக்கைகள் மற்றும் MAUSAM அவசரநிலை அறிவிப்புகள்"
                    } else if (lang == AppLanguage.TANGLISH) {
                        "Official Govt Alerts & MAUSAM Emergency Push Notifications"
                    } else {
                        "Official Government Alerts & MAUSAM Emergency Push Notifications"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Emergency Notification Trigger Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = RiskSevereRed.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚨", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == AppLanguage.TAMIL) "அவசரநிலை எச்சரிக்கை அறிவிப்பு (System Push)"
                            else if (lang == AppLanguage.TANGLISH) "Emergency Alert Notification Test"
                            else "Emergency Weather Alert Push Notification",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RiskSevereRed
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang == AppLanguage.TAMIL) {
                            "புயல், அடைமழை, அல்லது அதீத வெப்ப அலை வரும்போது உங்கள் செல்போனில் ஒலி மற்றும் அதிர்வுடன் (Sound & Vibration) எச்சரிக்கை அறிவிப்பு தோன்றும்."
                        } else if (lang == AppLanguage.TANGLISH) {
                            "Cyclone, heavy rain or severe weather varumbothu unga mobile-la sound & vibration-oda emergency notification alert varum."
                        } else {
                            "Critical heads-up push notifications with sound and vibration will instantly warn you of approaching severe storms, flash floods, or cyclones."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { requestAndSendNotification() },
                        colors = ButtonDefaults.buttonColors(containerColor = RiskSevereRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("test_emergency_alert_button")
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Test Notification", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == AppLanguage.TAMIL) "அவசர அறிவிப்பை சோதிக்கவும் (Send Test Alert)"
                            else if (lang == AppLanguage.TANGLISH) "🔔 Send Emergency Notification Alert"
                            else "🔔 Trigger Emergency Alert Notification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Offline Emergency & Safety Guide (Always available offline)
        item {
            val offlineSuggestions = remember(lang, uiState.currentWeather) {
                OfflineAdvisorEngine.getOfflineSuggestions(uiState.currentWeather, uiState.currentLocation, lang)
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📡", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == AppLanguage.TAMIL) "ஆஃப்லைன் அவசரநிலை வழிகாட்டி"
                            else if (lang == AppLanguage.TANGLISH) "Offline Emergency & Safety Advice"
                            else "Offline Emergency & Safety Guidelines",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    offlineSuggestions.forEach { item ->
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

        // Active Alerts List
        if (uiState.alerts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(text = "🌤️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.getNoAlerts(lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            items(uiState.alerts) { alert ->
                val severityColor = when (alert.severity) {
                    AlertSeverity.SEVERE -> RiskSevereRed
                    AlertSeverity.MODERATE -> RiskModerateYellow
                    AlertSeverity.INFO -> SkyBlueLight
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val categoryIcon = when (alert.category) {
                                    AlertCategory.RAIN -> "🌧️"
                                    AlertCategory.STORM -> "⛈️"
                                    AlertCategory.WIND -> "💨"
                                    AlertCategory.HEAT -> "🔥"
                                    AlertCategory.UV -> "☀️"
                                    AlertCategory.AQI -> "🌫️"
                                    AlertCategory.MARINE -> "🌊"
                                    AlertCategory.AGRICULTURE -> "🌱"
                                    AlertCategory.TRAVEL -> "🚗"
                                    AlertCategory.GENERAL -> "ℹ️"
                                }
                                Text(text = categoryIcon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Source Badge: OFFICIAL vs MAUSAM ADVISORY
                            Surface(
                                color = if (alert.isOfficial) RiskSevereRed.copy(alpha = 0.2f) else SkyBlueLight.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (alert.isOfficial) "OFFICIAL ALERT" else "MAUSAM ADVISORY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (alert.isOfficial) RiskSevereRed else SkyBlueLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = alert.description,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Source: ${alert.source}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = alert.timeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = severityColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun triggerSampleEmergencyNotification(context: android.content.Context, uiState: MausamUiState) {
    val lang = uiState.language
    val title = when (lang) {
        AppLanguage.TAMIL -> "அதிதீவிர கனமழை எச்சரிக்கை (Red Alert)"
        AppLanguage.TANGLISH -> "Heavy Torrential Rain & Flood Warning"
        AppLanguage.ENGLISH -> "Severe Weather & Torrential Rain Alert"
    }
    val message = when (lang) {
        AppLanguage.TAMIL -> "${uiState.currentLocation.locality} பகுதியில் அடுத்த சில மணிநேரங்களில் இடியுடன் கூடிய கனமழை பெய்ய வாய்ப்புள்ளது. பாதுகாப்பான இடங்களில் தங்கவும்."
        AppLanguage.TANGLISH -> "${uiState.currentLocation.locality}-la next few hours-la heavy rain & thunder expected. Highway travel avoid panni safe shelter-la irunga."
        AppLanguage.ENGLISH -> "Intense thunderstorm & heavy downpour detected approaching ${uiState.currentLocation.locality}. Avoid outdoor travel and move to secure shelter."
    }

    EmergencyNotificationHelper.sendEmergencyAlertNotification(
        context = context,
        title = title,
        description = message,
        severity = AlertSeverity.SEVERE,
        category = AlertCategory.STORM
    )

    Toast.makeText(context, "🚨 Emergency Notification Sent to Status Bar!", Toast.LENGTH_SHORT).show()
}
