package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SavedFieldEntity
import com.example.data.model.CropRiskLevel
import com.example.data.model.ExactLocation
import com.example.data.repository.LocationHelper
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState
import java.io.File
import java.io.FileOutputStream

@Composable
fun AgricultureScreen(
    uiState: MausamUiState,
    onImageSelected: (android.content.Context, Uri) -> Unit,
    onSelectFieldLocation: (android.content.Context, ExactLocation) -> Unit,
    onSaveNewField: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLocationModal by remember { mutableStateOf(false) }
    var showAddFieldModal by remember { mutableStateOf(false) }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(context, uri)
        }
    }

    // Camera Capture Launcher (bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Save temporary cache file to get URI
            try {
                val tempFile = File(context.cacheDir, "crop_cam_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()
                val uri = Uri.fromFile(tempFile)
                onImageSelected(context, uri)
            } catch (e: Exception) {
                // handle error gracefully
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("agriculture_screen_container")
    ) {
        // Section Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "🌱 Agriculture & Crop Risk",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Combines field image analysis with localized weather patterns",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Field Location Banner (Critical: Must display the FIELD's location, NOT current location blindly!)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_location_banner")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Field Location",
                                tint = RiskLowGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FIELD LOCATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = RiskLowGreen
                            )
                        }

                        if (uiState.imageHasExifGps) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Photo GPS") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = SkyBlueLight.copy(alpha = 0.15f),
                                    labelColor = SkyBlueLight
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        } else {
                            TextButton(
                                onClick = { showLocationModal = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Change Field", fontSize = 12.sp, color = SkyBlueLight)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.activeFieldLocation.locality,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.activeFieldLocation.district}, ${uiState.activeFieldLocation.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ Weather and disease risk are strictly evaluated for this field coordinates.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Image Upload & Camera Capture Buttons
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (uiState.selectedCropImageUri != null) {
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = uiState.selectedCropImageUri,
                                contentDescription = "Uploaded Field Crop",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Status Tag
                            Surface(
                                color = DeepIndigoBackground.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(bottomStart = 12.dp),
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Text(
                                    text = if (uiState.imageHasExifGps) "📍 GPS Tagged" else "Manual Field Location",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Upload Action Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("camera_capture_field_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capture Photo")
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("upload_crop_photo_button")
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Upload", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo")
                        }
                    }
                }
            }
        }

        // Image Quality Warning if quality failed
        if (!uiState.cropQualityPassed && uiState.cropQualityWarning != null) {
            item {
                Surface(
                    color = RiskModerateYellow.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚠️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.cropQualityWarning!!,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Crop Risk Assessment Result
        item {
            val assessment = uiState.cropRiskAssessment
            if (assessment != null) {
                val riskColor = when (assessment.riskLevel) {
                    CropRiskLevel.LOW -> RiskLowGreen
                    CropRiskLevel.MODERATE -> RiskModerateYellow
                    CropRiskLevel.HIGH -> RiskHighOrange
                    CropRiskLevel.VERY_HIGH -> RiskSevereRed
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("crop_risk_assessment_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "🌱 CROP RISK LEVEL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = assessment.riskLevel.name.replace("_", " "),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                    color = riskColor
                                )
                            }

                            // Visual observation tag
                            Surface(
                                color = riskColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "AI-Assisted Observation",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = riskColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual Observation description
                        Text(
                            text = "Visual Observation:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = assessment.visualObservation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Contributing Factors
                        Text(
                            text = "Contributing Factors:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        assessment.reasons.forEach { reason ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = riskColor, fontWeight = FontWeight.Bold)
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actionable Agronomic Recommendations
                        Text(
                            text = "Actionable Recommendations:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        assessment.recommendations.forEach { rec ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("✓ ", color = SkyBlueLight, fontWeight = FontWeight.Bold)
                                Text(
                                    text = rec,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Honest disclaimer
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ AI-assisted visual observation grounded in weather data. Not a laboratory or pathological diagnosis.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // My Fields Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🌾 My Saved Fields",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { showAddFieldModal = true }) {
                    Text("+ Add Field", color = SkyBlueLight)
                }
            }

            if (uiState.savedFields.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No saved fields yet. Tap '+ Add Field' to save your native plot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.savedFields) { field ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .width(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val loc = ExactLocation(
                                        locality = field.locality,
                                        district = field.district,
                                        state = field.state,
                                        latitude = field.latitude,
                                        longitude = field.longitude
                                    )
                                    onSelectFieldLocation(context, loc)
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = field.fieldName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Crop: ${field.cropType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SkyBlueLight
                                )
                                Text(
                                    text = "📍 ${field.locality}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to change field location
    if (showLocationModal) {
        AlertDialog(
            onDismissRequest = { showLocationModal = false },
            title = { Text("Where was this field photo taken?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MAUSAM fetches weather strictly for the field's actual coordinates, never substituting your current location.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Option 1: Melmaruvathur Demo Field
                    OutlinedButton(
                        onClick = {
                            onSelectFieldLocation(context, LocationHelper.DEMO_LOCATION)
                            showLocationModal = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🌾 Melmaruvathur Field (Demo)")
                    }

                    // Option 2: Use Current Location
                    OutlinedButton(
                        onClick = {
                            onSelectFieldLocation(context, uiState.currentLocation)
                            showLocationModal = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📍 Use Current Location (${uiState.currentLocation.locality})")
                    }

                    // Option 3: Madurai / Thanjavur Agricultural Hubs
                    OutlinedButton(
                        onClick = {
                            val thanjavur = ExactLocation("Thanjavur Delta", "Thanjavur District", "Tamil Nadu", latitude = 10.7870, longitude = 79.1378)
                            onSelectFieldLocation(context, thanjavur)
                            showLocationModal = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📍 Thanjavur Paddy Belt")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationModal = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal to Add Field
    if (showAddFieldModal) {
        var fieldName by remember { mutableStateOf("Native Field") }
        var cropType by remember { mutableStateOf("Paddy") }
        var plantDate by remember { mutableStateOf("2026-09-15") }

        AlertDialog(
            onDismissRequest = { showAddFieldModal = false },
            title = { Text("Save Field Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fieldName,
                        onValueChange = { fieldName = it },
                        label = { Text("Field Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cropType,
                        onValueChange = { cropType = it },
                        label = { Text("Crop Type (e.g. Paddy, Cotton)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = plantDate,
                        onValueChange = { plantDate = it },
                        label = { Text("Planting Date") },
                        singleLine = true
                    )
                    Text(
                        text = "Location: ${uiState.activeFieldLocation.locality}, ${uiState.activeFieldLocation.district}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveNewField(fieldName, cropType, plantDate)
                    showAddFieldModal = false
                }) {
                    Text("Save Field")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFieldModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
