package com.example.data.repository

import com.example.data.model.AppLanguage
import com.example.data.model.CurrentWeather
import com.example.data.model.ExactLocation

data class OfflineSuggestionItem(
    val id: String,
    val icon: String,
    val category: String,
    val title: String,
    val description: String,
    val actionableTip: String
)

object OfflineAdvisorEngine {

    fun getOfflineSuggestions(
        weather: CurrentWeather?,
        location: ExactLocation,
        language: AppLanguage
    ): List<OfflineSuggestionItem> {
        val temp = weather?.temperatureC ?: 30.0
        val humidity = weather?.humidityPercent ?: 70
        val isRainLikely = (weather?.precipitationMm ?: 0.0) > 0.5 || (weather?.weatherCode ?: 0) in listOf(61, 63, 65, 80, 81, 82, 95)

        val items = mutableListOf<OfflineSuggestionItem>()

        // 1. Travel & Road Safety (Offline Advice)
        val travelTitle = when (language) {
            AppLanguage.TAMIL -> "🚗 ஆஃப்லைன் சாலை பாதுகாப்பு"
            AppLanguage.TANGLISH -> "🚗 Offline Road Safety"
            AppLanguage.ENGLISH -> "🚗 Offline Road Safety"
        }
        val travelDesc = when (language) {
            AppLanguage.TAMIL -> "இணைய இணைப்பு இல்லாத போதும் பாதுகாப்பாக பயணிக்கவும். மழைக்காலங்களில் தேசிய நெடுஞ்சாலை மற்றும் சுரங்கப்பாதைகளில் நீர் தேங்க வாய்ப்புள்ளது."
            AppLanguage.TANGLISH -> "Offline-la irukkumbothu highway travel caution theva. Wet road-la tyre braking distance maintain pannunga."
            AppLanguage.ENGLISH -> "Safeguard offline travel: Maintain generous stopping distances on wet highway surfaces and avoid submerged railway underpasses."
        }
        val travelTip = when (language) {
            AppLanguage.TAMIL -> "வாகன முகப்பு விளக்குகளை (headlights) எரியவிட்டு செல்லவும்; டயர் அழுத்தத்தை சரிபார்க்கவும்."
            AppLanguage.TANGLISH -> "Headlights on panni pogavum, slow drive pannunga."
            AppLanguage.ENGLISH -> "Keep headlights on during reduced visibility and check tyre pressure."
        }
        items.add(OfflineSuggestionItem("travel", "🚗", "Travel", travelTitle, travelDesc, travelTip))

        // 2. Agriculture & Crop Protection (Offline Advice)
        val agriTitle = when (language) {
            AppLanguage.TAMIL -> "🌱 ஆஃப்லைன் பயிர் பராமரிப்பு"
            AppLanguage.TANGLISH -> "🌱 Offline Crop Care"
            AppLanguage.ENGLISH -> "🌱 Offline Crop Care"
        }
        val agriDesc = when (language) {
            AppLanguage.TAMIL -> "வயல் வரப்புகளில் நீர் தேங்காமல் வடிகால்களை (drainage channels) உடனே திறந்துவிடவும். அதிக ஈரப்பதம் பூஞ்சை நோயை உருவாக்கலாம்."
            AppLanguage.TANGLISH -> "Field bunds-la thanni thengaama drainage check pannunga. High humidity fungus disease create pannalaam."
            AppLanguage.ENGLISH -> "Clear field drainage trenches immediately to prevent waterlogging around root systems and reduce fungal blight."
        }
        val agriTip = when (language) {
            AppLanguage.TAMIL -> "மழை பெய்யும் நேரத்தில் பூச்சிக்கொல்லி மருந்துகள் தெளிப்பதை தவிர்க்கவும்."
            AppLanguage.TANGLISH -> "Rain varumbothu pesticide spray panna vendam."
            AppLanguage.ENGLISH -> "Postpone pesticide spraying if rain or heavy dew is anticipated."
        }
        items.add(OfflineSuggestionItem("agri", "🌱", "Agriculture", agriTitle, agriDesc, agriTip))

        // 3. Health & Dehydration Guide (Offline Advice)
        val healthTitle = when (language) {
            AppLanguage.TAMIL -> "❤️ உடல்நலம் & நீரேற்றம்"
            AppLanguage.TANGLISH -> "❤️ Health & Hydration"
            AppLanguage.ENGLISH -> "❤️ Health & Hydration"
        }
        val healthDesc = when (language) {
            AppLanguage.TAMIL -> "சூடான வானிலையில் உடல் நீர்ச்சத்து குறையலாம். உப்பு சர்க்கரை கரைசல் (ORS) அல்லது இளநீர் அருந்துவது நல்லது."
            AppLanguage.TANGLISH -> "Heat and humidity-la hydration romba mukkiyam. ORS or tender coconut nalladhu."
            AppLanguage.ENGLISH -> "Atmospheric warmth and humidity demand proactive hydration. Drink boiled water or electrolyte solutions regularly."
        }
        val healthTip = when (language) {
            AppLanguage.TAMIL -> "நண்பகல் 12 மணி முதல் 3 மணி வரை நேரடி வெயிலில் செல்வதை தவிர்க்கவும்."
            AppLanguage.TANGLISH -> "12 PM to 3 PM direct veyil-la poratha thavirkkavum."
            AppLanguage.ENGLISH -> "Avoid peak direct solar exposure between 12:00 PM and 3:00 PM."
        }
        items.add(OfflineSuggestionItem("health", "❤️", "Health", healthTitle, healthDesc, healthTip))

        // 4. Emergency Storm & Cyclone Preparedness
        val stormTitle = when (language) {
            AppLanguage.TAMIL -> "⚡ இடி மின்னல் & புயல் பாதுகாப்பு"
            AppLanguage.TANGLISH -> "⚡ Storm & Lightning Safety"
            AppLanguage.ENGLISH -> "⚡ Storm & Lightning Safety"
        }
        val stormDesc = when (language) {
            AppLanguage.TAMIL -> "இடி முழக்கத்தின் போது உயரமான மரங்கள், மின் கம்பங்கள் மற்றும் திறந்தவெளி மைதானங்களை விட்டு விலகி பாதுகாப்பான கட்டிடங்களில் தஞ்சமடையவும்."
            AppLanguage.TANGLISH -> "Thunder & lightning varumbothu trees and electric poles kitta nikkathinga; safe buildings-la irunga."
            AppLanguage.ENGLISH -> "During severe thunder or electrical storms, stay away from tall isolated trees, utility poles, and open fields."
        }
        val stormTip = when (language) {
            AppLanguage.TAMIL -> "செல்போன் மற்றும் எலக்ட்ரானிக் சாதனங்களை சார்ஜ் செய்து தயாராக வைக்கவும்."
            AppLanguage.TANGLISH -> "Mobile phone and emergency torch charge panni vachukonga."
            AppLanguage.ENGLISH -> "Keep mobile devices, power banks, and flashlights charged in advance."
        }
        items.add(OfflineSuggestionItem("storm", "⚡", "Safety", stormTitle, stormDesc, stormTip))

        return items
    }
}
