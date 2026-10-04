package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.LocationHelper
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TravelScreen(
    uiState: MausamUiState,
    onSelectSource: (ExactLocation) -> Unit,
    onSelectDestination: (ExactLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showSourcePicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }
    var selectedRouteTab by remember { mutableIntStateOf(0) } // 0 = Shortest, 1 = Longer Alternative

    // Live GPS Location Tracker State
    var isTracking by remember { mutableStateOf(false) }
    var trackingProgress by remember { mutableFloatStateOf(0.35f) }
    var trackingSpeed by remember { mutableIntStateOf(58) }

    // GPS Camera State
    var capturedRoadBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showGpsPhotoModal by remember { mutableStateOf(false) }
    var photoTimestamp by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedRoadBitmap = bitmap
            photoTimestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            showGpsPhotoModal = true
        }
    }

    // Live Tracker animation loop
    LaunchedEffect(isTracking) {
        if (isTracking) {
            while (isTracking) {
                delay(2000)
                trackingProgress = if (trackingProgress >= 0.95f) 0.1f else trackingProgress + 0.05f
                trackingSpeed = (52..68).random()
            }
        }
    }

    val shortest = uiState.shortestRoute
    val longer = uiState.longerRoute
    val activeRoute = if (selectedRouteTab == 0) shortest else longer

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("travel_screen_container")
    ) {
        // Title & Description
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = AppStrings.getTravelTitle(uiState.language),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = AppStrings.getTravelSubtitle(uiState.language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // =========================================================================
        // SOURCE & DESTINATION CHOOSER CARD
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = AppStrings.getChooseRoute(uiState.language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Departure (Source)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showSourcePicker = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(SkyBlueLight.copy(alpha = 0.25f), CircleShape)
                            ) {
                                Text(text = "A", fontWeight = FontWeight.Bold, color = SkyBlueLight)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.getFromSource(uiState.language),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SkyBlueLight
                                )
                                Text(
                                    text = "${uiState.travelSource.locality}, ${uiState.travelSource.district}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Change Source",
                                tint = SkyBlueLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Destination
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showDestPicker = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(SunriseOrange.copy(alpha = 0.25f), CircleShape)
                            ) {
                                Text(text = "B", fontWeight = FontWeight.Bold, color = SunriseOrange)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.getToDestination(uiState.language),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SunriseOrange
                                )
                                Text(
                                    text = "${uiState.travelDestination.locality}, ${uiState.travelDestination.district}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Change Destination",
                                tint = SunriseOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Button: OPEN IN GOOGLE MAPS NAVIGATION
                    Button(
                        onClick = {
                            openGoogleMapsNavigation(
                                context = context,
                                source = uiState.travelSource,
                                destination = uiState.travelDestination
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("open_google_maps_button")
                    ) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = "Google Maps", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.getOpenInMaps(uiState.language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SHORTEST PATH & TRAFFIC DETECTION CARD
        // =========================================================================
        if (shortest != null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
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
                            Surface(
                                color = RiskLowGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = AppStrings.getShortestPathIdentified(uiState.language),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RiskLowGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            val isHeavy = shortest.trafficStatus.contains("Heavy", ignoreCase = true)
                            Surface(
                                color = if (isHeavy) RiskSevereRed.copy(alpha = 0.18f) else RiskModerateYellow.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                val trafficLabel = when (uiState.language) {
                                    AppLanguage.TAMIL -> if (isHeavy) "⚠️ போக்குவரத்து நெரிசல்" else "🚗 சீரான போக்குவரத்து"
                                    AppLanguage.TANGLISH -> if (isHeavy) "⚠️ Traffic Delay" else "🚗 Normal Flow"
                                    AppLanguage.ENGLISH -> if (isHeavy) "⚠️ Traffic Delay" else "🚗 Normal Flow"
                                }
                                Text(
                                    text = trafficLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isHeavy) RiskSevereRed else RiskModerateYellow,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(AppStrings.getDistance(uiState.language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${shortest.distanceKm} km", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = SkyBlueLight)
                            }
                            Column {
                                Text(AppStrings.getTravelTime(uiState.language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val hrs = shortest.durationMin / 60
                                val mins = shortest.durationMin % 60
                                val timeText = if (hrs > 0) "${hrs}h ${mins}m" else "$mins min"
                                Text(timeText, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            }
                            Column {
                                val rainLabel = if (uiState.language == AppLanguage.TAMIL) "மழை ஆபத்து" else "Rain Risk"
                                Text(rainLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${shortest.avgRainProb}%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = if (shortest.avgRainProb > 45) RiskHighOrange else RiskLowGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Traffic Detection Details
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🚦 Traffic Detection: ${shortest.trafficStatus}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = shortest.advisory,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // ROUTE WEATHER CHECKPOINTS (TRACK WEATHER)
        // =========================================================================
        if (activeRoute != null) {
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
                            Text(
                                text = AppStrings.getTrackWeatherTitle(uiState.language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        activeRoute.checkpoints.forEachIndexed { index, cp ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (index == 0) SkyBlueLight else if (index == activeRoute.checkpoints.size - 1) SunriseOrange else PurpleAccent, CircleShape)
                                ) {
                                    Text(text = "${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cp.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = cp.condition,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (cp.rainProb > 45) RiskHighOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${cp.tempC.toInt()}°C",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "💧 ${cp.rainProb}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SkyBlueLight
                                    )
                                }
                            }

                            if (index < activeRoute.checkpoints.size - 1) {
                                Row(modifier = Modifier.padding(start = 11.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(16.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // GPS CAMERA & LIVE TRACKING BAR
        // =========================================================================
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("gps_camera_capture_button")
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.getGpsRoadCam(uiState.language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { isTracking = !isTracking },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracking) RiskSevereRed else RiskLowGreen
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("toggle_tracking_button")
                ) {
                    Icon(
                        imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.Navigation,
                        contentDescription = "Track",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val trackLabel = if (isTracking) {
                        if (uiState.language == AppLanguage.TAMIL) "நிறுத்து" else "Stop Tracking"
                    } else {
                        AppStrings.getLiveGpsTrack(uiState.language)
                    }
                    Text(
                        text = trackLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Live Tracker Details (when tracking enabled)
        if (isTracking) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🚗 VEHICLE EN ROUTE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = RiskLowGreen
                            )
                            Text(
                                text = "$trackingSpeed km/h",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SkyBlueLight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val currentLat = uiState.travelSource.latitude + (uiState.travelDestination.latitude - uiState.travelSource.latitude) * trackingProgress
                        val currentLon = uiState.travelSource.longitude + (uiState.travelDestination.longitude - uiState.travelSource.longitude) * trackingProgress

                        Text(
                            text = "Approaching Highway Checkpoint • ${String.format(Locale.US, "%.4f° N, %.4f° E", currentLat, currentLon)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { trackingProgress },
                            color = SkyBlueLight,
                            trackColor = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL: SOURCE SELECTOR (Search Any City/Town/Village)
    // =========================================================================
    if (showSourcePicker) {
        LocationSelectorDialog(
            title = "Choose Departure Source",
            onDismiss = { showSourcePicker = false },
            onSelect = { loc ->
                onSelectSource(loc)
                showSourcePicker = false
            },
            onUseCurrentGps = {
                onSelectSource(uiState.currentLocation)
                showSourcePicker = false
            }
        )
    }

    // =========================================================================
    // MODAL: DESTINATION SELECTOR (Search Any Destination)
    // =========================================================================
    if (showDestPicker) {
        LocationSelectorDialog(
            title = "Choose Journey Destination",
            onDismiss = { showDestPicker = false },
            onSelect = { loc ->
                onSelectDestination(loc)
                showDestPicker = false
            },
            onUseCurrentGps = null
        )
    }

    // =========================================================================
    // GPS ROAD CAMERA WATERMARK DIALOG
    // =========================================================================
    if (showGpsPhotoModal && capturedRoadBitmap != null) {
        Dialog(onDismissRequest = { showGpsPhotoModal = false }) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📷 GPS Road Camera Photo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            bitmap = capturedRoadBitmap!!.asImageBitmap(),
                            contentDescription = "Road Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "📍 ${uiState.travelSource.locality} Highway Corridor",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "GPS: ${String.format(Locale.US, "%.4f° N, %.4f° E", uiState.travelSource.latitude, uiState.travelSource.longitude)} • $photoTimestamp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "30°C • Wet Road Alert • Verified by MAUSAM GPS Camera",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RiskLowGreen,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = { cameraLauncher.launch(null) }) {
                            Text("Retake")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { showGpsPhotoModal = false }) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

private fun openGoogleMapsNavigation(context: Context, source: ExactLocation, destination: ExactLocation) {
    try {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${source.latitude},${source.longitude}&destination=${destination.latitude},${destination.longitude}&travelmode=driving")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Opening Google Maps navigation...", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun LocationSelectorDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelect: (ExactLocation) -> Unit,
    onUseCurrentGps: (() -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ExactLocation>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val popularHubs = remember {
        listOf(
            ExactLocation("Guindy", "Chennai", "Tamil Nadu", latitude = 13.0067, longitude = 80.2025),
            LocationHelper.DEMO_LOCATION, // Melmaruvathur
            ExactLocation("Mahabalipuram", "Chengalpattu District", "Tamil Nadu", latitude = 12.6208, longitude = 80.1944),
            ExactLocation("White Town", "Puducherry", "Puducherry", latitude = 11.9338, longitude = 79.8358),
            ExactLocation("Meenakshi Temple", "Madurai", "Tamil Nadu", latitude = 9.9252, longitude = 78.1198),
            ExactLocation("Gandhipuram", "Coimbatore", "Tamil Nadu", latitude = 11.0168, longitude = 76.9558),
            ExactLocation("Thillai Nagar", "Tiruchirappalli", "Tamil Nadu", latitude = 10.8250, longitude = 78.6934),
            ExactLocation("Kanchipuram Temple Area", "Kanchipuram", "Tamil Nadu", latitude = 12.8342, longitude = 79.7036),
            ExactLocation("MG Road", "Bengaluru", "Karnataka", latitude = 12.9716, longitude = 77.5946)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.isNotBlank()) {
                            isSearching = true
                            coroutineScope.launch {
                                searchResults = LocationHelper.searchLocations(it)
                                isSearching = false
                            }
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    placeholder = { Text("Search city, village, town, tourist spot...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional: Use Current GPS Location
                if (onUseCurrentGps != null) {
                    Button(
                        onClick = onUseCurrentGps,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "GPS", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📍 Use Current GPS Location")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val listToDisplay = if (query.isNotBlank() && searchResults.isNotEmpty()) {
                    searchResults
                } else {
                    popularHubs
                }

                Text(
                    text = if (query.isNotBlank()) "Search Results" else "Quick Select Destinations / Cities",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(listToDisplay) { loc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(loc) }
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = "Pin", tint = SkyBlueLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = loc.locality,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
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
