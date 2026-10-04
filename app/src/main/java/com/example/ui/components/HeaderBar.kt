package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.ExactLocation
import com.example.ui.theme.*

@Composable
fun HeaderBar(
    location: ExactLocation,
    alertCount: Int,
    currentLanguage: AppLanguage,
    isDemoMode: Boolean,
    onLocationClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onDemoToggle: () -> Unit,
    onProfileClick: () -> Unit,
    onLifestyleClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Row 1: App Title, Demo Badge, Lifestyle Button, Language, Alerts, Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MAUSAM",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = SkyBlueLight
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // 8 Lifestyles Quick Access Chip
                    AssistChip(
                        onClick = onLifestyleClick,
                        label = {
                            Text(
                                text = AppStrings.getLifestylesChip(currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SkyBlueLight
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SkyBlueLight.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("header_lifestyles_chip")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Demo Mode Badge
                    AssistChip(
                        onClick = onDemoToggle,
                        label = {
                            Text(
                                text = if (isDemoMode) "DEMO" else "LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDemoMode) RiskModerateYellow else RiskLowGreen
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isDemoMode) RiskModerateYellow.copy(alpha = 0.15f) else RiskLowGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("demo_mode_chip")
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Selector
                    Box {
                        FilledTonalButton(
                            onClick = { showLangMenu = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("language_switch_button")
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "EN"
                                    AppLanguage.TAMIL -> "தமிழ்"
                                    AppLanguage.TANGLISH -> "Tanglish"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English (EN)") },
                                onClick = {
                                    onLanguageChange(AppLanguage.ENGLISH)
                                    showLangMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("தமிழ் (Tamil)") },
                                onClick = {
                                    onLanguageChange(AppLanguage.TAMIL)
                                    showLangMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Tanglish (Tamil+English)") },
                                onClick = {
                                    onLanguageChange(AppLanguage.TANGLISH)
                                    showLangMenu = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Alert Icon with Badge
                    IconButton(
                        onClick = onAlertsClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("alerts_header_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (alertCount > 0) {
                                    Badge(containerColor = RiskSevereRed) {
                                        Text("$alertCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Profile Icon
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("profile_header_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IndigoAccent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Profile",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Exact Location Display (Village / Locality -> District -> State)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLocationClick() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("exact_location_header_click")
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location Pin",
                    tint = SkyBlueLight,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = location.locality,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${location.district}, ${location.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Change Location",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
