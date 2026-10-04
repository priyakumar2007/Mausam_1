package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HeaderBar
import com.example.ui.components.MausamBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.MausamTheme
import com.example.ui.viewmodel.MausamViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MausamTheme {
                val viewModel: MausamViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                var showSplash by remember { mutableStateOf(true) }
                var showLogin by remember { mutableStateOf(false) }
                var showLocationDialog by remember { mutableStateOf(false) }

                // Location Permission Launcher
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                    if (fineGranted || coarseGranted) {
                        viewModel.useCurrentLocation(context)
                    }
                }

                // Handle Splash Screen
                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                            if (uiState.isGuest && uiState.userName == "Friend") {
                                showLogin = true
                            }
                        }
                    )
                } else if (showLogin) {
                    LoginScreen(
                        onContinueAsGuest = { guestName ->
                            viewModel.loginAsGuest(guestName)
                            showLogin = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onContinueWithGoogle = { name, email ->
                            viewModel.loginWithGoogle(name, email)
                            showLogin = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                } else {
                    // Back handler for sub-tabs to return to Home
                    if (uiState.activeNavTab != "home") {
                        BackHandler {
                            viewModel.setNavTab("home")
                        }
                    }

                    Scaffold(
                        topBar = {
                            HeaderBar(
                                location = uiState.currentLocation,
                                alertCount = uiState.alerts.size,
                                currentLanguage = uiState.language,
                                isDemoMode = uiState.isDemoMode,
                                onLocationClick = { showLocationDialog = true },
                                onAlertsClick = { viewModel.setNavTab("alerts") },
                                onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                                onDemoToggle = { viewModel.toggleDemoMode() },
                                onProfileClick = { viewModel.setNavTab("profile") },
                                onLifestyleClick = { viewModel.setNavTab("lifestyle") }
                            )
                        },
                        bottomBar = {
                            MausamBottomBar(
                                currentTab = uiState.activeNavTab,
                                onTabSelected = { tabId -> viewModel.setNavTab(tabId) },
                                language = uiState.language
                            )
                        },
                        contentWindowInsets = WindowInsets.safeDrawing,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (uiState.activeNavTab) {
                                "home" -> HomeScreen(
                                    uiState = uiState,
                                    onNavigateTab = { tabId -> viewModel.setNavTab(tabId) },
                                    onLocationClick = { showLocationDialog = true },
                                    onAlertsClick = { viewModel.setNavTab("alerts") }
                                )
                                "forecast" -> ForecastScreen(
                                    uiState = uiState
                                )
                                "lifestyle" -> LifestyleScreen(
                                    uiState = uiState,
                                    onToggleLifestyle = { lifestyle -> viewModel.toggleLifestyle(lifestyle) }
                                )
                                "agriculture" -> AgricultureScreen(
                                    uiState = uiState,
                                    onImageSelected = { ctx, uri -> viewModel.handleCropImageSelected(ctx, uri) },
                                    onSelectFieldLocation = { ctx, loc -> viewModel.setFieldLocation(ctx, loc) },
                                    onSaveNewField = { name, crop, date -> viewModel.saveField(name, crop, date) }
                                )
                                "travel" -> TravelScreen(
                                    uiState = uiState,
                                    onSelectSource = { src -> viewModel.setTravelSource(src) },
                                    onSelectDestination = { dest -> viewModel.setTravelDestination(dest) }
                                )
                                "beach" -> BeachScreen(
                                    uiState = uiState,
                                    onSelectBeach = { beach -> viewModel.selectBeach(beach) }
                                )
                                "alerts" -> AlertsScreen(
                                    uiState = uiState
                                )
                                "chat" -> AskMausamScreen(
                                    uiState = uiState,
                                    onSendMessage = { q -> viewModel.sendChatMessage(q) },
                                    onLanguageChange = { lang -> viewModel.setLanguage(lang) }
                                )
                                "profile" -> ProfileScreen(
                                    uiState = uiState,
                                    onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                                    onDemoToggle = { viewModel.toggleDemoMode() },
                                    onNavigateTab = { tabId -> viewModel.setNavTab(tabId) }
                                )
                                else -> HomeScreen(
                                    uiState = uiState,
                                    onNavigateTab = { tabId -> viewModel.setNavTab(tabId) },
                                    onLocationClick = { showLocationDialog = true },
                                    onAlertsClick = { viewModel.setNavTab("alerts") }
                                )
                            }
                        }

                        // Location Search Dialog
                        if (showLocationDialog) {
                            LocationSearchDialog(
                                onDismiss = { showLocationDialog = false },
                                onSelectLocation = { selectedLoc ->
                                    viewModel.setLocation(selectedLoc)
                                },
                                onUseCurrentLocation = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                onSearchQuery = { query -> viewModel.searchLocations(query) },
                                searchResults = uiState.searchResults,
                                isSearching = uiState.isSearchingLocation
                            )
                        }
                    }
                }
            }
        }
    }
}
