package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExactLocation
import com.example.data.repository.LocationHelper
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary

@Composable
fun LocationSearchDialog(
    onDismiss: () -> Unit,
    onSelectLocation: (ExactLocation) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onSearchQuery: (String) -> Unit,
    searchResults: List<ExactLocation>,
    isSearching: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val popularLocalities = remember {
        listOf(
            LocationHelper.DEMO_LOCATION, // Melmaruvathur
            ExactLocation("Acharapakkam", "Chengalpattu District", "Tamil Nadu", latitude = 12.4083, longitude = 79.8167),
            ExactLocation("Madurantakam", "Chengalpattu District", "Tamil Nadu", latitude = 12.5089, longitude = 79.8856),
            ExactLocation("Guindy", "Chennai", "Tamil Nadu", latitude = 13.0067, longitude = 80.2025),
            ExactLocation("Mahabalipuram", "Chengalpattu District", "Tamil Nadu", latitude = 12.6208, longitude = 80.1944),
            ExactLocation("Thanjavur Delta", "Thanjavur District", "Tamil Nadu", latitude = 10.7870, longitude = 79.1378),
            ExactLocation("Coimbatore City", "Coimbatore", "Tamil Nadu", latitude = 11.0168, longitude = 76.9558),
            ExactLocation("Meenakshi Nagar", "Madurai", "Tamil Nadu", latitude = 9.9252, longitude = 78.1198)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .testTag("location_search_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📍 Select Exact Location",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQuery(it)
                    },
                    placeholder = { Text("Search village, town, city, district...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_location_text_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Use Current Location Button
                Button(
                    onClick = {
                        onUseCurrentLocation()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("use_current_location_button")
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "GPS", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Current GPS Location")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SkyBlueLight)
                    }
                } else {
                    val listToDisplay = if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                        searchResults
                    } else {
                        popularLocalities
                    }

                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results" else "Common Localities & Taluks",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(listToDisplay) { loc ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelectLocation(loc)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Pin",
                                    tint = SkyBlueLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = loc.locality,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${loc.district}, ${loc.state}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
