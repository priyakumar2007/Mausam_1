package com.example.data.model

object AppStrings {

    fun getTabLabel(tabId: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.TAMIL -> when (tabId) {
                "home" -> "முகப்பு"
                "lifestyle" -> "வாழ்க்கை"
                "forecast" -> "வானிலை"
                "agriculture" -> "பயிர்"
                "travel" -> "பயணம்"
                "beach" -> "கடற்கரை"
                "chat" -> "கேளுங்கள்"
                else -> tabId.replaceFirstChar { it.uppercase() }
            }
            AppLanguage.TANGLISH -> when (tabId) {
                "home" -> "Home"
                "lifestyle" -> "Lifestyle"
                "forecast" -> "Forecast"
                "agriculture" -> "Crops"
                "travel" -> "Travel"
                "beach" -> "Beach"
                "chat" -> "Ask"
                else -> tabId.replaceFirstChar { it.uppercase() }
            }
            AppLanguage.ENGLISH -> when (tabId) {
                "home" -> "Home"
                "lifestyle" -> "Lifestyle"
                "forecast" -> "Forecast"
                "agriculture" -> "Crops"
                "travel" -> "Travel"
                "beach" -> "Beach"
                "chat" -> "Ask"
                else -> tabId.replaceFirstChar { it.uppercase() }
            }
        }
    }

    // Header Bar
    fun getLifestylesChip(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🎯 வாழ்க்கை"
        AppLanguage.TANGLISH -> "🎯 Lifestyles"
        AppLanguage.ENGLISH -> "🎯 Lifestyles"
    }

    // Quick Actions Section
    fun getQuickActionsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "⚡ விரைவு சேவைகள்"
        AppLanguage.TANGLISH -> "⚡ Quick Actions"
        AppLanguage.ENGLISH -> "⚡ Quick Actions"
    }

    fun getLifestylesHubTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "8 ஸ்மார்ட் வாழ்க்கை முறை சேவைகள்"
        AppLanguage.TANGLISH -> "8 Smart Lifestyles Hub"
        AppLanguage.ENGLISH -> "8 Smart Lifestyles Hub"
    }

    fun getLifestylesHubSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "உடல்நலம் • உடற்பயிற்சி • கடற்கரை • பயணம் • குடும்பம் • பயிர் • போக்குவரத்து • விழாக்கள்"
        AppLanguage.TANGLISH -> "Health • Fitness • Beach • Travel • Family • Crops • Transit • Events"
        AppLanguage.ENGLISH -> "Health • Fitness • Beach • Travel • Family • Crops • Transit • Events"
    }

    fun getActionCropTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "பயிர் பாதுகாப்பு"
        AppLanguage.TANGLISH -> "Check Crop Risk"
        AppLanguage.ENGLISH -> "Check Crop Risk"
    }

    fun getActionCropSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "வயல் படம் & மழை ஆபத்து"
        AppLanguage.TANGLISH -> "Field photo & rain risk"
        AppLanguage.ENGLISH -> "Field photo & rain risk"
    }

    fun getActionTravelTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "பயணம் திட்டமிடு"
        AppLanguage.TANGLISH -> "Plan Travel"
        AppLanguage.ENGLISH -> "Plan Travel"
    }

    fun getActionTravelSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "வழிகள் & ஜிபிஎஸ் வரைபடம்"
        AppLanguage.TANGLISH -> "Routes & GPS tracker"
        AppLanguage.ENGLISH -> "Routes & GPS tracker"
    }

    fun getActionBeachTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "கடற்கரை தேடு"
        AppLanguage.TANGLISH -> "Find Beach"
        AppLanguage.ENGLISH -> "Find Beach"
    }

    fun getActionBeachSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "அலை, நீச்சல் & பாதுகாப்பு"
        AppLanguage.TANGLISH -> "Surf, waves & safety"
        AppLanguage.ENGLISH -> "Surf, waves & safety"
    }

    fun getActionExploreTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "இடங்களை காண்க"
        AppLanguage.TANGLISH -> "Explore Place"
        AppLanguage.ENGLISH -> "Explore Place"
    }

    fun getActionExploreSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "நகரங்கள் & கிராமங்கள்"
        AppLanguage.TANGLISH -> "Towns & localities"
        AppLanguage.ENGLISH -> "Towns & localities"
    }

    // Home Screen
    fun getFeelsLike(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "உணர்வது"
        AppLanguage.TANGLISH -> "Feels like"
        AppLanguage.ENGLISH -> "Feels like"
    }

    fun getRainChance(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "மழை வாய்ப்பு"
        AppLanguage.TANGLISH -> "Rain Chance"
        AppLanguage.ENGLISH -> "Rain Chance"
    }

    fun getHumidity(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "ஈரப்பதம்"
        AppLanguage.TANGLISH -> "Humidity"
        AppLanguage.ENGLISH -> "Humidity"
    }

    fun getWind(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "காற்று"
        AppLanguage.TANGLISH -> "Wind"
        AppLanguage.ENGLISH -> "Wind"
    }

    fun getUvIndex(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "UV குறியீடு"
        AppLanguage.TANGLISH -> "UV Index"
        AppLanguage.ENGLISH -> "UV Index"
    }

    fun getAirQuality(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "காற்றின் தரம்"
        AppLanguage.TANGLISH -> "Air Quality"
        AppLanguage.ENGLISH -> "Air Quality"
    }

    fun getHourlyForecastTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🕒 மணிநேர முன்னறிவிப்பு (24 மணி)"
        AppLanguage.TANGLISH -> "🕒 Hourly Forecast (24 hrs)"
        AppLanguage.ENGLISH -> "🕒 Hourly Forecast (24 hrs)"
    }

    fun getDailyForecastTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "📅 7 நாள் வானிலை கண்ணோட்டம்"
        AppLanguage.TANGLISH -> "📅 7-Day Weather Outlook"
        AppLanguage.ENGLISH -> "📅 7-Day Weather Outlook"
    }

    fun getLifestyleInsightsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "✨ உங்கள் 8 வாழ்க்கை முறை வழிகாட்டல்"
        AppLanguage.TANGLISH -> "✨ Unga 8 Lifestyle Insights"
        AppLanguage.ENGLISH -> "✨ Your 8 Lifestyle Insights"
    }

    fun getViewAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "அனைத்தும் காண்க"
        AppLanguage.TANGLISH -> "View All"
        AppLanguage.ENGLISH -> "View All"
    }

    // Lifestyle Screen
    fun getLifestyleScreenTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🎯 8 ஸ்மார்ட் வாழ்க்கை முறை கண்ணோட்டம்"
        AppLanguage.TANGLISH -> "🎯 8 Smart Lifestyle Insights"
        AppLanguage.ENGLISH -> "🎯 8 Smart Lifestyle Insights"
    }

    fun getLifestyleScreenSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "நிகழ்நேர வளிமண்டல அளவீடுகளின் அடிப்படையில் தனிப்பயனாக்கப்பட்ட வழிகாட்டல்"
        AppLanguage.TANGLISH -> "Real-time weather data vachu personalized lifestyle insights"
        AppLanguage.ENGLISH -> "Personalized actionable weather indices grounded in real-time atmosphere variables"
    }

    // Travel Screen
    fun getTravelTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "✈️ ஸ்மார்ட் பயணம் & பாதை வானிலை"
        AppLanguage.TANGLISH -> "✈️ Smart Travel & Route Weather"
        AppLanguage.ENGLISH -> "✈️ Smart Travel & Route Weather"
    }

    fun getTravelSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "குறுகிய பாதை, கூகுள் நேரடி போக்குவரத்து, பாதை வானிலை மற்றும் ஜிபிஎஸ் கேமரா"
        AppLanguage.TANGLISH -> "Shortest path, live Google traffic, route weather & GPS camera"
        AppLanguage.ENGLISH -> "Shortest path, live Google traffic detection, route weather checkpoints, and GPS camera"
    }

    fun getChooseRoute(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "பயண பாதையை தேர்வு செய்க"
        AppLanguage.TANGLISH -> "Journey Route Choose Pannunga"
        AppLanguage.ENGLISH -> "Choose Journey Route"
    }

    fun getFromSource(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "புறப்படும் இடம் (தொடக்க இடம்)"
        AppLanguage.TANGLISH -> "FROM (Source)"
        AppLanguage.ENGLISH -> "FROM (Source)"
    }

    fun getToDestination(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "சேருமிடம்"
        AppLanguage.TANGLISH -> "TO (Destination)"
        AppLanguage.ENGLISH -> "TO (Destination)"
    }

    fun getOpenInMaps(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🗺️ கூகுள் மேப்ஸ் & நேரடி போக்குவரத்தில் திறக்கவும்"
        AppLanguage.TANGLISH -> "🗺️ Open in Google Maps & Live Traffic"
        AppLanguage.ENGLISH -> "🗺️ Open in Google Maps & Live Traffic"
    }

    fun getShortestPathIdentified(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🏆 குறுகிய பாதை கண்டறியப்பட்டது"
        AppLanguage.TANGLISH -> "🏆 SHORTEST PATH IDENTIFIED"
        AppLanguage.ENGLISH -> "🏆 SHORTEST PATH IDENTIFIED"
    }

    fun getDistance(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "தொலைவு"
        AppLanguage.TANGLISH -> "Distance"
        AppLanguage.ENGLISH -> "Distance"
    }

    fun getTravelTime(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "பயண நேரம்"
        AppLanguage.TANGLISH -> "Travel Time"
        AppLanguage.ENGLISH -> "Travel Time"
    }

    fun getTrackWeatherTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🌦️ உங்கள் பாதையில் நிலவும் வானிலை சோதனைகள்"
        AppLanguage.TANGLISH -> "🌦️ Route Track Weather Checkpoints"
        AppLanguage.ENGLISH -> "🌦️ Weather Conditions Along Your Track"
    }

    fun getGpsRoadCam(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "📷 ஜிபிஎஸ் சாலை கேமரா"
        AppLanguage.TANGLISH -> "📷 GPS Road Cam"
        AppLanguage.ENGLISH -> "📷 GPS Road Cam"
    }

    fun getLiveGpsTrack(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "📍 நேரடி ஜிபிஎஸ் கண்காணிப்பு"
        AppLanguage.TANGLISH -> "📍 Live GPS Track"
        AppLanguage.ENGLISH -> "📍 Live GPS Track"
    }

    // Agriculture Screen
    fun getAgriTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🌱 ஸ்மார்ட் விவசாயம் & பயிர் இடர் மேலாண்மை"
        AppLanguage.TANGLISH -> "🌱 Smart Agriculture & Field Risk"
        AppLanguage.ENGLISH -> "🌱 Smart Agriculture & Field Risk"
    }

    fun getAgriSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "வயல் புகைப்பட நோய் கண்டறிதல், மண் ஈரப்பதம் மற்றும் தெளிப்பு வழிகாட்டல்"
        AppLanguage.TANGLISH -> "Field photo diagnosis, soil moisture & spray advisory"
        AppLanguage.ENGLISH -> "Field photo diagnosis, soil moisture analysis, rain impact, and spray advisory"
    }

    fun getUploadFieldPhoto(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "📸 வயல் புகைப்படத்தை பதிவேற்றவும்"
        AppLanguage.TANGLISH -> "📸 Field Photo Upload Pannunga"
        AppLanguage.ENGLISH -> "📸 Capture / Upload Field Photo"
    }

    // Beach Screen
    fun getBeachTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "🏖 கடற்கரை வழிகாட்டி & அலை பாதுகாப்பு"
        AppLanguage.TANGLISH -> "🏖 Beach Finder & Surf Safety"
        AppLanguage.ENGLISH -> "🏖 Coastal Beach Finder & Surf Safety"
    }

    fun getBeachSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "நிகழ்நேர அலை உயரம், கடற்கரை காற்று வேகம் மற்றும் நீச்சல் பாதுகாப்பு"
        AppLanguage.TANGLISH -> "Live wave heights, wind speed & safe swimming timing"
        AppLanguage.ENGLISH -> "Live wave heights, coastal wind speed, surf safety status, and safe swimming timing"
    }

    // Alerts Screen
    fun getAlertsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "⚠️ வானிலை எச்சரிக்கைகள் & அறிவிப்புகள்"
        AppLanguage.TANGLISH -> "⚠️ Weather Alerts & Warnings"
        AppLanguage.ENGLISH -> "⚠️ Weather Alerts & Warnings"
    }

    fun getNoAlerts(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "வானிலை சீராக உள்ளது • தீவிர எச்சரிக்கைகள் இல்லை"
        AppLanguage.TANGLISH -> "All Clear • No Severe Alerts"
        AppLanguage.ENGLISH -> "All Clear • No Severe Alerts"
    }

    // Forecast Screen
    fun getForecastTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "📅 7 நாள் மற்றும் மணிநேர வானிலை"
        AppLanguage.TANGLISH -> "📅 7-Day & Hourly Forecast"
        AppLanguage.ENGLISH -> "📅 7-Day & Hourly Forecast"
    }

    // Chatbot strings
    fun getChatPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "வானிலை பற்றி எதையும் கேட்கலாம்..."
        AppLanguage.TANGLISH -> "Weather pathi ethu venalum kelunga..."
        AppLanguage.ENGLISH -> "Ask MAUSAM anything..."
    }

    fun getVoiceListeningPrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.TAMIL -> "வானிலை பற்றி பேசவும்..."
        AppLanguage.TANGLISH -> "Weather pathi pesunga..."
        AppLanguage.ENGLISH -> "Speak your weather question..."
    }

    fun getQuickQuestions(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.TAMIL -> listOf(
            "இன்று மழை வருமா?",
            "நாளை வானிலை எப்படி?",
            "ஓட சிறந்த நேரம் எது?",
            "பயிருக்கு ஆபத்து உள்ளதா?",
            "கடற்கரை செல்லலாமா?"
        )
        AppLanguage.TANGLISH -> listOf(
            "Inaiku rain varuma?",
            "Naalaiku weather eppadi?",
            "Running pogalama bro?",
            "Payir ku danger irukka?",
            "Beach safe-ah inaiku?"
        )
        AppLanguage.ENGLISH -> listOf(
            "Will it rain today?",
            "How is tomorrow's forecast?",
            "Can I go for a run?",
            "Is my crop at risk?",
            "Is the beach safe today?"
        )
    }
}
