package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MausamUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val lastUpdated: String = "",

    // User Profile & Settings
    val userName: String = "Friend",
    val userEmail: String = "guest@mausam.app",
    val isGuest: Boolean = true,
    val isDemoMode: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val tempUnit: String = "C",
    val selectedLifestyles: Set<LifestyleType> = setOf(
        LifestyleType.HEALTH,
        LifestyleType.FITNESS,
        LifestyleType.AGRICULTURE,
        LifestyleType.TRAVEL,
        LifestyleType.BEACH
    ),

    // Current Exact Location & Weather
    val currentLocation: ExactLocation = LocationHelper.DEMO_LOCATION,
    val currentWeather: CurrentWeather? = null,
    val hourlyForecast: List<HourlyForecastItem> = emptyList(),
    val dailyForecast: List<DailyForecastItem> = emptyList(),
    val airQuality: AirQualityInfo? = null,
    val alerts: List<MausamAlert> = emptyList(),
    val mausamInsight: String = "",

    // Agriculture & Crop Risk
    val activeFieldLocation: ExactLocation = LocationHelper.DEMO_LOCATION,
    val selectedCropImageUri: Uri? = null,
    val imageHasExifGps: Boolean = false,
    val cropQualityPassed: Boolean = true,
    val cropQualityWarning: String? = null,
    val cropRiskAssessment: CropRiskAssessment? = null,
    val savedFields: List<SavedFieldEntity> = emptyList(),
    val cropHistory: List<CropHistoryEntity> = emptyList(),

    // Travel
    val travelSource: ExactLocation = LocationHelper.DEMO_LOCATION,
    val travelDestination: ExactLocation = LocationHelper.CHENNAI_LOCATION,
    val shortestRoute: TravelRouteInfo? = null,
    val longerRoute: TravelRouteInfo? = null,
    val isCalculatingTravel: Boolean = false,

    // Beach Finder & Marine Safety
    val nearbyBeaches: List<BeachSafetyInfo> = emptyList(),
    val selectedBeach: BeachSafetyInfo? = null,
    val isLoadingBeaches: Boolean = false,

    // Search Location Results
    val searchResults: List<ExactLocation> = emptyList(),
    val isSearchingLocation: Boolean = false,

    // Ask MAUSAM Chat
    val chatMessages: List<AskMausamEngine.ConversationMessage> = emptyList(),
    val activeNavTab: String = "home"
)

class MausamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)
    private val db = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(MausamUiState())
    val uiState: StateFlow<MausamUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        observeDatabase()
        initDefaultChat()
        refreshWeather(LocationHelper.DEMO_LOCATION)
        loadBeaches(LocationHelper.DEMO_LOCATION)
        calculateTravel(LocationHelper.DEMO_LOCATION, LocationHelper.CHENNAI_LOCATION)
    }

    private fun initDefaultChat() {
        val welcome = when (_uiState.value.language) {
            AppLanguage.TAMIL -> "வணக்கம்! நான் உங்கள் MAUSAM உதவியாளர். இன்று உங்கள் பகுதி வானிலை, உடற்பயிற்சி நேரம், பயிர் இடர் அல்லது பயண வழிகள் பற்றி கேட்கலாம்."
            AppLanguage.TANGLISH -> "Vanakkam! Naan unga MAUSAM friend. Innaiku weather, running time, crop risk, or travel route pathi ethu venumnaalum kelunga!"
            AppLanguage.ENGLISH -> "Hello! I am MAUSAM, your personal weather companion. Ask me about running comfort, rain timing, crop health, or travel routes!"
        }
        _uiState.update {
            it.copy(
                chatMessages = listOf(
                    AskMausamEngine.ConversationMessage(isUser = false, text = welcome)
                )
            )
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = db.userDao().getUserProfileOnce()
            if (profile != null) {
                val lifestyles = profile.selectedLifestyles.split(",")
                    .mapNotNull { name ->
                        try { LifestyleType.valueOf(name.trim()) } catch (e: Exception) { null }
                    }.toSet()
                val lang = try {
                    AppLanguage.valueOf(profile.language)
                } catch (e: Exception) {
                    AppLanguage.ENGLISH
                }

                _uiState.update {
                    it.copy(
                        userName = profile.name,
                        userEmail = profile.email,
                        isGuest = profile.isGuest,
                        isDemoMode = profile.isDemoMode,
                        language = lang,
                        tempUnit = profile.tempUnit,
                        selectedLifestyles = if (lifestyles.isNotEmpty()) lifestyles else it.selectedLifestyles
                    )
                }
            }
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            db.fieldDao().getAllFields().collect { fields ->
                _uiState.update { it.copy(savedFields = fields) }
            }
        }
        viewModelScope.launch {
            db.cropHistoryDao().getAllCropHistory().collect { history ->
                _uiState.update { it.copy(cropHistory = history) }
            }
        }
    }

    fun setNavTab(tab: String) {
        _uiState.update { it.copy(activeNavTab = tab) }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        recalculateInsight()
        viewModelScope.launch {
            val cur = db.userDao().getUserProfileOnce() ?: UserProfileEntity()
            db.userDao().saveUserProfile(cur.copy(language = language.name))
        }
    }

    fun toggleLifestyle(lifestyle: LifestyleType) {
        val current = _uiState.value.selectedLifestyles.toMutableSet()
        if (current.contains(lifestyle)) {
            if (current.size > 1) current.remove(lifestyle)
        } else {
            current.add(lifestyle)
        }
        _uiState.update { it.copy(selectedLifestyles = current) }
        recalculateInsight()
        viewModelScope.launch {
            val cur = db.userDao().getUserProfileOnce() ?: UserProfileEntity()
            db.userDao().saveUserProfile(cur.copy(selectedLifestyles = current.joinToString(",") { it.name }))
        }
    }

    fun toggleDemoMode() {
        val newDemo = !_uiState.value.isDemoMode
        _uiState.update { it.copy(isDemoMode = newDemo) }
        if (newDemo) {
            setLocation(LocationHelper.DEMO_LOCATION)
        }
        viewModelScope.launch {
            val cur = db.userDao().getUserProfileOnce() ?: UserProfileEntity()
            db.userDao().saveUserProfile(cur.copy(isDemoMode = newDemo))
        }
    }

    fun loginAsGuest(name: String = "Priya") {
        _uiState.update { it.copy(userName = name, isGuest = true) }
        viewModelScope.launch {
            val cur = db.userDao().getUserProfileOnce() ?: UserProfileEntity()
            db.userDao().saveUserProfile(cur.copy(name = name, isGuest = true))
        }
    }

    fun loginWithGoogle(name: String, email: String) {
        _uiState.update { it.copy(userName = name, userEmail = email, isGuest = false) }
        viewModelScope.launch {
            val cur = db.userDao().getUserProfileOnce() ?: UserProfileEntity()
            db.userDao().saveUserProfile(cur.copy(name = name, email = email, isGuest = false))
        }
    }

    fun setLocation(location: ExactLocation) {
        _uiState.update { it.copy(currentLocation = location) }
        refreshWeather(location)
        loadBeaches(location)
    }

    fun useCurrentLocation(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val detected = LocationHelper.getCurrentLocation(context)
            setLocation(detected)
        }
    }

    fun searchLocations(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearchingLocation = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingLocation = true) }
            val results = LocationHelper.searchLocations(query)
            _uiState.update { it.copy(searchResults = results, isSearchingLocation = false) }
        }
    }

    fun refreshWeather(location: ExactLocation = _uiState.value.currentLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.getFullWeatherData(location.latitude, location.longitude, forceRefresh = true)
                val insight = WeatherUtils.generateMausamInsight(
                    result.current,
                    result.hourly,
                    _uiState.value.selectedLifestyles,
                    _uiState.value.language
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentWeather = result.current,
                        hourlyForecast = result.hourly,
                        dailyForecast = result.daily,
                        airQuality = result.airQuality,
                        alerts = result.alerts,
                        mausamInsight = insight,
                        isOffline = result.isOffline,
                        lastUpdated = result.lastUpdatedFormatted
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not update weather. Showing cached data."
                    )
                }
            }
        }
    }

    private fun recalculateInsight() {
        val cur = _uiState.value.currentWeather ?: return
        val hourly = _uiState.value.hourlyForecast
        val insight = WeatherUtils.generateMausamInsight(
            cur,
            hourly,
            _uiState.value.selectedLifestyles,
            _uiState.value.language
        )
        _uiState.update { it.copy(mausamInsight = insight) }
    }

    // -------------------------------------------------------------
    // Agriculture & Crop Risk Operations
    // -------------------------------------------------------------

    fun handleCropImageSelected(context: android.content.Context, uri: Uri) {
        viewModelScope.launch {
            val (isQualityPass, qualityWarning) = LocationHelper.checkImageQuality(context, uri)
            val gpsCoords = LocationHelper.extractExifGps(context, uri)

            val fieldLocation = if (gpsCoords != null) {
                LocationHelper.reverseGeocode(context, gpsCoords.first, gpsCoords.second)
            } else {
                _uiState.value.activeFieldLocation
            }

            _uiState.update {
                it.copy(
                    selectedCropImageUri = uri,
                    imageHasExifGps = (gpsCoords != null),
                    activeFieldLocation = fieldLocation,
                    cropQualityPassed = isQualityPass,
                    cropQualityWarning = qualityWarning
                )
            }

            // Fetch weather at FIELD location and evaluate
            evaluateFieldCropRisk(context, fieldLocation, isQualityPass, qualityWarning, uri)
        }
    }

    fun setFieldLocation(context: android.content.Context, location: ExactLocation) {
        _uiState.update { it.copy(activeFieldLocation = location) }
        val uri = _uiState.value.selectedCropImageUri
        if (uri != null) {
            viewModelScope.launch {
                evaluateFieldCropRisk(
                    context,
                    location,
                    _uiState.value.cropQualityPassed,
                    _uiState.value.cropQualityWarning,
                    uri
                )
            }
        }
    }

    private suspend fun evaluateFieldCropRisk(
        context: android.content.Context,
        fieldLocation: ExactLocation,
        isQualityPass: Boolean,
        qualityWarning: String?,
        imageUri: Uri
    ) {
        val fieldWeatherResult = repository.getFullWeatherData(fieldLocation.latitude, fieldLocation.longitude)
        val assessment = CropRiskEngine.evaluateCropRisk(
            observationPrompt = "Leaf margin chlorosis with early necrotic spots observed on paddy foliage.",
            weather = fieldWeatherResult.current,
            forecast = fieldWeatherResult.daily,
            fieldLocation = fieldLocation,
            isQualityPass = isQualityPass,
            qualityMessage = qualityWarning
        )

        _uiState.update { it.copy(cropRiskAssessment = assessment) }

        // Save entry into Room CropHistory
        val historyEntry = CropHistoryEntity(
            fieldName = "Field at ${fieldLocation.locality}",
            imageUri = imageUri.toString(),
            analysisDate = SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            riskLevel = assessment.riskLevel.name,
            reasons = assessment.reasons.joinToString(" • "),
            recommendations = assessment.recommendations.joinToString(" • "),
            visualObservation = assessment.visualObservation,
            weatherSummary = assessment.weatherSummary,
            locality = fieldLocation.locality
        )
        db.cropHistoryDao().insertCropHistory(historyEntry)
    }

    fun saveField(fieldName: String, cropType: String, plantingDate: String) {
        viewModelScope.launch {
            val loc = _uiState.value.activeFieldLocation
            val field = SavedFieldEntity(
                fieldName = fieldName,
                cropType = cropType,
                locality = loc.locality,
                district = loc.district,
                state = loc.state,
                latitude = loc.latitude,
                longitude = loc.longitude,
                plantingDate = plantingDate
            )
            db.fieldDao().insertField(field)
        }
    }

    // -------------------------------------------------------------
    // Travel Operations
    // -------------------------------------------------------------

    fun setTravelSource(location: ExactLocation) {
        _uiState.update { it.copy(travelSource = location) }
        calculateTravel(location, _uiState.value.travelDestination)
    }

    fun setTravelDestination(location: ExactLocation) {
        _uiState.update { it.copy(travelDestination = location) }
        calculateTravel(_uiState.value.travelSource, location)
    }

    fun calculateTravel(source: ExactLocation, dest: ExactLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingTravel = true) }
            val srcWeather = try {
                repository.getFullWeatherData(source.latitude, source.longitude).current
            } catch (e: Exception) {
                null
            }
            val destWeather = try {
                repository.getFullWeatherData(dest.latitude, dest.longitude).current
            } catch (e: Exception) {
                null
            }

            val (shortest, longer) = TravelEngine.buildRouteOptions(
                source = source,
                destination = dest,
                sourceTemp = srcWeather?.temperatureC ?: 29.0,
                destTemp = destWeather?.temperatureC ?: 31.0,
                sourceRainProb = 15,
                destRainProb = if (dest.locality.contains("Chennai", ignoreCase = true)) 65 else 25
            )

            _uiState.update {
                it.copy(
                    shortestRoute = shortest,
                    longerRoute = longer,
                    isCalculatingTravel = false
                )
            }
        }
    }

    // -------------------------------------------------------------
    // Beach Operations
    // -------------------------------------------------------------

    fun loadBeaches(location: ExactLocation = _uiState.value.currentLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBeaches = true) }
            val beaches = BeachSafetyEngine.getNearbyBeachesWithSafety(location.latitude, location.longitude)
            _uiState.update {
                it.copy(
                    nearbyBeaches = beaches,
                    selectedBeach = beaches.firstOrNull(),
                    isLoadingBeaches = false
                )
            }
        }
    }

    fun selectBeach(beach: BeachSafetyInfo) {
        _uiState.update { it.copy(selectedBeach = beach) }
    }

    // -------------------------------------------------------------
    // Ask MAUSAM Chat Operations
    // -------------------------------------------------------------

    fun sendChatMessage(query: String) {
        if (query.isBlank()) return
        val userMsg = AskMausamEngine.ConversationMessage(isUser = true, text = query)
        val currentMsgs = _uiState.value.chatMessages.toMutableList()
        currentMsgs.add(userMsg)
        _uiState.update { it.copy(chatMessages = currentMsgs) }

        viewModelScope.launch {
            val answer = AskMausamEngine.answerQuery(
                query = query,
                activeContext = _uiState.value.activeNavTab.uppercase(),
                currentWeather = _uiState.value.currentWeather,
                hourly = _uiState.value.hourlyForecast,
                daily = _uiState.value.dailyForecast,
                location = _uiState.value.currentLocation,
                cropAssessment = _uiState.value.cropRiskAssessment,
                selectedBeach = _uiState.value.selectedBeach,
                travelRoutes = if (_uiState.value.shortestRoute != null && _uiState.value.longerRoute != null)
                    Pair(_uiState.value.shortestRoute!!, _uiState.value.longerRoute!!) else null,
                language = _uiState.value.language
            )

            val botMsg = AskMausamEngine.ConversationMessage(isUser = false, text = answer)
            val updated = _uiState.value.chatMessages.toMutableList()
            updated.add(botMsg)
            _uiState.update { it.copy(chatMessages = updated) }
        }
    }
}
