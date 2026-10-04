package com.example.data.repository

import com.example.data.model.*
import java.util.Calendar

object AskMausamEngine {

    data class ConversationMessage(
        val isUser: Boolean,
        val text: String,
        val timestamp: Long = System.currentTimeMillis(),
        val contextTag: String? = null
    )

    fun answerQuery(
        query: String,
        activeContext: String, // "HOME", "AGRICULTURE", "TRAVEL", "BEACH"
        currentWeather: CurrentWeather?,
        hourly: List<HourlyForecastItem>,
        daily: List<DailyForecastItem>,
        location: ExactLocation,
        cropAssessment: CropRiskAssessment?,
        selectedBeach: BeachSafetyInfo?,
        travelRoutes: Pair<TravelRouteInfo, TravelRouteInfo>?,
        language: AppLanguage
    ): String {
        val raw = query.lowercase().trim()
        val temp = currentWeather?.temperatureC?.toInt() ?: 29
        val condition = currentWeather?.conditionText ?: "Partly Cloudy"
        val humidity = currentWeather?.humidityPercent ?: 65
        val wind = currentWeather?.windSpeedKmh ?: 14.0
        val uv = currentWeather?.uvIndex ?: 5.0
        val rainMax6h = hourly.take(6).maxOfOrNull { it.rainProbability } ?: 20
        val rainHours = hourly.take(12).filter { it.rainProbability >= 45 }
        val tomorrow = daily.getOrNull(1)

        // 1. GREETINGS & CASUAL TALK
        if (raw.matches(Regex(".*\\b(hi|hello|hey|vanakkam|namaste|good morning|good evening|good afternoon|enna panra|eppadi irukka|how are you|who are you|sup)\\b.*"))) {
            return when (language) {
                AppLanguage.TAMIL -> "வணக்கம்! நான் உங்கள் MAUSAM வானிலை நண்பன். ${location.locality}-ல் தற்போது $temp°C, $condition. மழை, உடற்பயிற்சி, பயணம், பயிர் பாதுகாப்பு அல்லது கடற்கரை பற்றி எதை வேண்டுமானாலும் கேளுங்கள்!"
                AppLanguage.TANGLISH -> "Vanakkam! Naan unga MAUSAM smart weather buddy. ${location.locality}-la ipo $temp°C, $condition. Rain, tomorrow forecast, travel route, crop risk, or running fitness pathi ethu venalum kelunga!"
                AppLanguage.ENGLISH -> "Hello! I am your MAUSAM personal weather assistant. Currently in ${location.locality}, it is $temp°C and $condition. Feel free to ask me anything about rain timings, tomorrow's forecast, travel routes, crop care, beach safety, or running conditions!"
            }
        }

        // 2. TOMORROW'S FORECAST & UPCOMING DAYS
        if (raw.contains("tomorrow") || raw.contains("naalaiku") || raw.contains("nalaiku") || raw.contains("adutha naal") || raw.contains("next day")) {
            val tMax = tomorrow?.maxTempC?.toInt() ?: (temp + 1)
            val tMin = tomorrow?.minTempC?.toInt() ?: (temp - 5)
            val tRain = tomorrow?.rainProbMax ?: 25
            val tCond = tomorrow?.conditionText ?: "Partly Cloudy"

            return when (language) {
                AppLanguage.TAMIL -> "நாளை (${tomorrow?.dayOfWeek ?: "நாளை"}) ${location.locality}-ல் வானிலை: $tCond. அதிகபட்ச வெப்பநிலை $tMax°C, குறைந்தபட்சம் $tMin°C. மழைக்கான சாத்தியக்கூறு $tRain%."
                AppLanguage.TANGLISH -> "Naalaiku ${location.locality}-la weather $tCond. Max temp $tMax°C, min $tMin°C. Rain chance $tRain% irukku."
                AppLanguage.ENGLISH -> "Tomorrow in ${location.locality} (${tomorrow?.dayOfWeek ?: "Tomorrow"}), expect $tCond with high of $tMax°C and low of $tMin°C. Peak rain probability is $tRain%."
            }
        }

        // 3. WEEKEND / 7-DAY OUTLOOK
        if (raw.contains("weekend") || raw.contains("week") || raw.contains("7 day") || raw.contains("varam") || raw.contains("days")) {
            val avgMax = daily.take(5).map { it.maxTempC }.average().toInt()
            val maxRainDay = daily.take(5).maxByOrNull { it.rainProbMax }
            return when (language) {
                AppLanguage.TAMIL -> "அடுத்த 7 நாட்களுக்கு சராசரி வெப்பநிலை $avgMax°C. ${maxRainDay?.dayOfWeek ?: "வார இறுதி"}-ல் அதிகபட்சமாக ${maxRainDay?.rainProbMax ?: 30}% வரை மழை வாய்ப்புள்ளது."
                AppLanguage.TANGLISH -> "Next 7 days average temperature around $avgMax°C irukum. ${maxRainDay?.dayOfWeek ?: "Weekend"}-la rain chance highest (${maxRainDay?.rainProbMax ?: 30}%)."
                AppLanguage.ENGLISH -> "Looking at the 7-day outlook for ${location.locality}, daily highs average around $avgMax°C. The highest rain potential occurs on ${maxRainDay?.dayOfWeek ?: "the weekend"} with up to ${maxRainDay?.rainProbMax ?: 30}% probability."
            }
        }

        // 4. RAIN, DRIZZLE, STORMS & UMBRELLA
        if (raw.contains("rain") || raw.contains("mazhai") || raw.contains("mala") || raw.contains("umbrella") ||
            raw.contains("koda") || raw.contains("shower") || raw.contains("drizzle") || raw.contains("thunder") || raw.contains("storm")) {
            return when (language) {
                AppLanguage.TAMIL -> {
                    if (rainHours.isNotEmpty()) {
                        "ஆம்! ${rainHours.first().hourLabel} முதல் ${rainHours.last().hourLabel} வரை மழை பெய்ய ${rainHours.maxOf { it.rainProbability }}% வாய்ப்புள்ளது. வெளியில் செல்லும்போது குடை அல்லது மழைக்கோட் கட்டாயம் எடுத்துச் செல்லவும்."
                    } else {
                        "அடுத்த 12 மணிநேரத்தில் ${location.locality}-ல் குறிப்பிடத்தக்க மழைக்கான சாத்தியக்கூறு இல்லை (மழை வாய்ப்பு < 25%)."
                    }
                }
                AppLanguage.TANGLISH -> {
                    if (rainHours.isNotEmpty()) {
                        "Aama! ${rainHours.first().hourLabel} to ${rainHours.last().hourLabel} kulla rain vara chance ${rainHours.maxOf { it.rainProbability }}% irukku. Veliya poranga na umbrella or raincoat kandippa eduthukonga!"
                    } else {
                        "Next 12 hours-la heavy rain chances illa (${location.locality}-la rain probability romba kammi < 25%). Free-ah veliya polam."
                    }
                }
                AppLanguage.ENGLISH -> {
                    if (rainHours.isNotEmpty()) {
                        "Yes, precipitation chances rise up to ${rainHours.maxOf { it.rainProbability }}% between ${rainHours.first().hourLabel} and ${rainHours.last().hourLabel} in ${location.locality}. Carrying an umbrella or raincoat is strongly recommended."
                    } else {
                        "No significant rainfall expected in ${location.locality} over the next 12 hours. Maximum precipitation probability remains gentle under 25%."
                    }
                }
            }
        }

        // 5. TEMPERATURE & HEAT SENSATION
        if (raw.contains("temp") || raw.contains("temperature") || raw.contains("heat") || raw.contains("hot") ||
            raw.contains("veyil") || raw.contains("soodu") || raw.contains("veppam") || raw.contains("cold") || raw.contains("kulir")) {
            val feelsLike = currentWeather?.feelsLikeC?.toInt() ?: temp
            return when (language) {
                AppLanguage.TAMIL -> "தற்போதைய வெப்பநிலை $temp°C (உணர்வது $feelsLike°C). ஈரப்பதம் $humidity%. நண்பகல் வேளையில் வெயில் தாகம் எடுக்கலாம்; நிறைய தண்ணீர் குடிக்கவும்."
                AppLanguage.TANGLISH -> "Current temperature $temp°C (feels like $feelsLike°C). Humidity $humidity%. Veyil sooda irukum, nalla water kudinga."
                AppLanguage.ENGLISH -> "Current temperature in ${location.locality} is $temp°C (feels like $feelsLike°C) with $humidity% humidity. Stay well-hydrated throughout the day."
            }
        }

        // 6. AIR QUALITY & BREATHING COMFORT
        if (raw.contains("aqi") || raw.contains("air") || raw.contains("pollution") || raw.contains("kaathu") ||
            raw.contains("smog") || raw.contains("dust") || raw.contains("breathe") || raw.contains("asthma")) {
            return when (language) {
                AppLanguage.TAMIL -> "காற்றின் தரம் (AQI) பொதுவாக ஆரோக்கியமாக உள்ளது. வெளியில் சுதந்திரமாக சுவாசிக்கலாம் மற்றும் உடற்பயிற்சி செய்யலாம்."
                AppLanguage.TANGLISH -> "Air quality (AQI) good condition-la irukku. Veliya walk porathukku and breathing-ku romba safe."
                AppLanguage.ENGLISH -> "Air quality index (AQI) is in a healthy, favorable range in ${location.locality}. Safe for outdoor breathing, jogging, and general activities."
            }
        }

        // 7. UV & SUNBURN
        if (raw.contains("uv") || raw.contains("sunscreen") || raw.contains("sun") || raw.contains("tan") || raw.contains("skin")) {
            val uvVal = uv.toInt()
            return when (language) {
                AppLanguage.TAMIL -> "இன்றைய UV குறியீடு: $uvVal/11. ${if (uvVal >= 6) "வெயில் சுட்டெரிக்க கூடும். SPF 30+ சன்ஸ்கிரீன் பூசவும் மற்றும் தொப்பி அணியவும்." else "UV அளவு மிதமானது. மிதமான வெயில் பாதுகாப்பு போதுமானது."}"
                AppLanguage.TANGLISH -> "Inaiku UV index $uvVal/11. ${if (uvVal >= 6) "Veyil athigama irukku, sun protection or sunscreen use pannunga." else "Moderate UV thaan, safe to roam."}"
                AppLanguage.ENGLISH -> "Current UV index is $uvVal/11. ${if (uvVal >= 6) "UV exposure is high during midday; apply SPF 30+ sunscreen and wear sunglasses or caps." else "UV levels are moderate, safe for normal exposure."}"
            }
        }

        // 8. FITNESS, RUNNING & WORKOUTS
        if (raw.contains("run") || raw.contains("jog") || raw.contains("walk") || raw.contains("fitness") ||
            raw.contains("workout") || raw.contains("exercise") || raw.contains("oda")) {
            val score = WeatherUtils.calculateFitnessScore(
                currentWeather?.temperatureC ?: 29.0,
                humidity,
                wind,
                rainMax6h,
                uv
            )
            return when (language) {
                AppLanguage.TAMIL -> "உடற்பயிற்சி வசதி மதிப்பீடு: ${score.first}/100. ${score.second} ஓடுவதற்கு சிறந்த நேரம் காலை 6:00 - 7:30 மணி அல்லது மாலை 5:30 - 6:45 மணி."
                AppLanguage.TANGLISH -> "Running comfort score ${score.first}/100. ${score.second} Best run time: Morning 6:00 to 7:30 AM or Evening 5:30 to 6:45 PM."
                AppLanguage.ENGLISH -> "Fitness Comfort Score is ${score.first}/100 in ${location.locality}. ${score.second} Recommended running windows are early morning (6:00–7:30 AM) and dusk (5:30–6:45 PM)."
            }
        }

        // 9. CROPS, AGRICULTURE & SOIL
        if (raw.contains("crop") || raw.contains("field") || raw.contains("payir") || raw.contains("farm") ||
            raw.contains("vivasayam") || raw.contains("soil") || raw.contains("paddy") || raw.contains("irrigation") || raw.contains("thanni")) {
            if (cropAssessment != null) {
                val fLoc = cropAssessment.fieldLocation.locality
                return when (language) {
                    AppLanguage.TAMIL -> "உங்கள் வயல் ($fLoc): இடர் அளவு ${cropAssessment.riskLevel}. வானிலை: ${cropAssessment.weatherSummary}. பரிந்துரை: ${cropAssessment.recommendations.firstOrNull() ?: "வடிகாலை சரிபார்க்கவும்."}"
                    AppLanguage.TANGLISH -> "Unga field ($fLoc): Risk level ${cropAssessment.riskLevel}. Weather: ${cropAssessment.weatherSummary}. Suggestion: ${cropAssessment.recommendations.firstOrNull() ?: "Drainage channels clear pannunga."}"
                    AppLanguage.ENGLISH -> "For your active field at $fLoc: Assessed risk level is ${cropAssessment.riskLevel}. Local condition: ${cropAssessment.weatherSummary}. Recommendation: ${cropAssessment.recommendations.firstOrNull() ?: "Ensure unblocked drainage bunds."}"
                }
            } else {
                return when (language) {
                    AppLanguage.TAMIL -> "மண் ஈரப்பதம் $humidity%. அடுத்த 24 மணிநேரத்தில் மழை வாய்ப்பு $rainMax6h%. குறிப்பிட்ட பயிர் இடர் அறிக்கைக்கு வயல் புகைப்படத்தை 'பயிர்' பகுதியில் பதிவேற்றவும்."
                    AppLanguage.TANGLISH -> "Soil humidity $humidity%. Next 24 hours rain chance $rainMax6h%. Specific crop report-ku Agriculture tab-la leaf photo upload pannunga."
                    AppLanguage.ENGLISH -> "Soil humidity is around $humidity% with $rainMax6h% rain potential. To diagnose leaf spots or fungal risk, take a field photo in the Crops tab."
                }
            }
        }

        // 10. TRAVEL, DRIVING, HIGHWAY & TRAFFIC
        if (raw.contains("travel") || raw.contains("drive") || raw.contains("route") || raw.contains("vazhi") ||
            raw.contains("traffic") || raw.contains("highway") || raw.contains("nh45") || raw.contains("trip") || raw.contains("car") || raw.contains("road")) {
            if (travelRoutes != null) {
                val shortest = travelRoutes.first
                return when (language) {
                    AppLanguage.TAMIL -> "நேரடி வழி: ${shortest.distanceKm} km (${shortest.durationMin} நிமிடம், ${shortest.trafficStatus}). மழை வாய்ப்பு: ${shortest.avgRainProb}%. Google Maps-ல் திறக்க 'பயணம்' பிரிவைப் பார்க்கவும்."
                    AppLanguage.TANGLISH -> "Shortest route: ${shortest.distanceKm} km (${shortest.durationMin} min). Traffic: ${shortest.trafficStatus}. Rain chance: ${shortest.avgRainProb}%. Google Maps button Travel tab-la irukku!"
                    AppLanguage.ENGLISH -> "Shortest path is ${shortest.distanceKm} km (~${shortest.durationMin} min, ${shortest.trafficStatus}). Route rain probability averages ${shortest.avgRainProb}%. Tap 'Open in Google Maps' in Travel tab for turn-by-turn navigation."
                }
            } else {
                return when (language) {
                    AppLanguage.TAMIL -> "பயணத்தின் தொடக்க மற்றும் சேருமிடத்தை தேர்வு செய்ய 'பயணம்' பிரிவை பயன்படுத்தவும். நேரடி வழி மற்றும் வானிலை தானாக காட்டப்படும்."
                    AppLanguage.TANGLISH -> "Travel tab-la unga source and destination choose pannunga. Shortest path, traffic and weather automatic-ah theriyum."
                    AppLanguage.ENGLISH -> "Select your Source and Destination in the Travel tab to view shortest path, live Google traffic status, and track weather checkpoints."
                }
            }
        }

        // 11. BEACH, SURF & SEA
        if (raw.contains("beach") || raw.contains("sea") || raw.contains("kadal") || raw.contains("wave") ||
            raw.contains("alai") || raw.contains("surf") || raw.contains("swim") || raw.contains("marina") || raw.contains("kovalam")) {
            if (selectedBeach != null) {
                val waveH = String.format(java.util.Locale.US, "%.1f", selectedBeach.waveHeightM ?: 1.0)
                return when (language) {
                    AppLanguage.TAMIL -> "${selectedBeach.name}: அலை உயரம் ${waveH}m, காற்று ${selectedBeach.windSpeedKmh.toInt()} km/h. பாதுகாப்பு நிலை: ${selectedBeach.safetyStatus}."
                    AppLanguage.TANGLISH -> "${selectedBeach.name}-la waves ${waveH}m, wind ${selectedBeach.windSpeedKmh.toInt()} km/h. Safety status: ${selectedBeach.safetyStatus}."
                    AppLanguage.ENGLISH -> "At ${selectedBeach.name} (${selectedBeach.distanceKm} km away), wave height is ${waveH} m with winds at ${selectedBeach.windSpeedKmh.toInt()} km/h. Surf status: ${selectedBeach.safetyStatus}."
                }
            } else {
                return when (language) {
                    AppLanguage.TAMIL -> "கடற்கரை அலை உயரம் மற்றும் பாதுகாப்பு நிலையை காண 'கடற்கரை' பிரிவை திறக்கவும்."
                    AppLanguage.TANGLISH -> "Beach wave heights and safe swimming timing paaka Beach tab open pannunga."
                    AppLanguage.ENGLISH -> "Open the Beach Finder tab to inspect live coastal wave heights, water temperatures, and surf suitability."
                }
            }
        }

        // 12. FAMILY & KIDS OUTINGS
        if (raw.contains("family") || raw.contains("kids") || raw.contains("children") || raw.contains("baby") ||
            raw.contains("park") || raw.contains("playground") || raw.contains("kuzhandhai")) {
            return when (language) {
                AppLanguage.TAMIL -> "குடும்பம் மற்றும் குழந்தைகளுடன் பூங்கா செல்ல உகந்த நேரம் மாலை 5:00 மணி முதல் 6:45 மணி வரை. நண்பகல் வெயில் சுட்டெரிக்கலாம்."
                AppLanguage.TANGLISH -> "Kids and family outdoor park outing-ku evening 5:00 to 6:45 PM romba pleasant-ah irukum. Midday veyil avoid pannunga."
                AppLanguage.ENGLISH -> "Ideal outdoor playground hours for family and children in ${location.locality} are between 5:00 PM and 6:45 PM when ambient heat cools down."
            }
        }

        // 13. COMMUTE, OFFICE & TWO-WHEELERS
        if (raw.contains("commute") || raw.contains("office") || raw.contains("work") || raw.contains("bike") ||
            raw.contains("scooter") || raw.contains("two wheeler")) {
            return when (language) {
                AppLanguage.TAMIL -> "தினசரி அலுவலக பயணத்திற்கு வானிலை சாதகமாக உள்ளது. மாலை நேரங்களில் லேசான மழை வாய்ப்பு இருக்கலாம்; இருசக்கர வாகன ஓட்டிகள் மழை பாதுகாப்புடன் செல்லவும்."
                AppLanguage.TANGLISH -> "Daily office commute smooth-ah irukum. Evening return time-la light rain chance irukalaam, bike riders raincoat vachukonga."
                AppLanguage.ENGLISH -> "Commuter conditions are smooth in ${location.locality}. Moderate rain chances during evening rush hours (5–8 PM); two-wheeler riders should keep rain gear handy."
            }
        }

        // 14. OUTDOOR EVENTS & CELEBRATIONS
        if (raw.contains("event") || raw.contains("function") || raw.contains("party") || raw.contains("wedding") ||
            raw.contains("kalyanam") || raw.contains("reception") || raw.contains("lawn")) {
            return when (language) {
                AppLanguage.TAMIL -> "திறந்தவெளி நிகழ்ச்சிகளுக்கு மாலை வேளையில் இனிமையான சூழல் நிலவும். மழை வாய்ப்பு $rainMax6h%. பந்தல் அல்லது கூடாரங்களை காற்றுக்கு ஏற்றவாறு அமைத்துக் கொள்ளவும்."
                AppLanguage.TANGLISH -> "Outdoor function & evening event-ku weather super-ah irukku. Rain chance $rainMax6h%. Safe to plan open lawns."
                AppLanguage.ENGLISH -> "Outdoor gatherings and evening events have favorable conditions in ${location.locality} with rain risk at $rainMax6h%. Pleasant climate for open lawns."
            }
        }

        // 15. CLOTHES & WHAT TO WEAR
        if (raw.contains("wear") || raw.contains("dress") || raw.contains("cloth") || raw.contains("jacket") || raw.contains("cotton")) {
            return when (language) {
                AppLanguage.TAMIL -> "வெப்பநிலை $temp°C என்பதால் பருத்தி (cotton) மற்றும் காற்றோட்டமான ஆடைகள் அணிவது வசதியாக இருக்கும்."
                AppLanguage.TANGLISH -> "Temp $temp°C irukuradhala light cotton clothes wear pannunga, romba comfortable-ah irukum."
                AppLanguage.ENGLISH -> "With current temperature at $temp°C and $humidity% humidity, lightweight breathable cotton attire is ideal."
            }
        }

        // 16. HUMIDITY & SULTRINESS
        if (raw.contains("humidity") || raw.contains("sweat") || raw.contains("eeram") || raw.contains("eerapatham") || raw.contains("humid")) {
            return when (language) {
                AppLanguage.TAMIL -> "தற்போதைய ஈரப்பதம் $humidity%. வியர்வை வரலாம்; குளிர்ந்த நீர் அல்லது இளநீர் பருகுவது நல்லது."
                AppLanguage.TANGLISH -> "Current humidity $humidity%. Konjam sweat aagum, stay hydrated with tender coconut or water."
                AppLanguage.ENGLISH -> "Relative humidity is currently $humidity% in ${location.locality}. Expect mild perspiration during physical activity; stay hydrated."
            }
        }

        // 17. WIND SPEED & CYCLONE
        if (raw.contains("wind") || raw.contains("kaathu") || raw.contains("cyclone") || raw.contains("puyal") || raw.contains("gust")) {
            val windSpeed = wind.toInt()
            return when (language) {
                AppLanguage.TAMIL -> "தற்போதைய காற்றின் வேகம்: $windSpeed km/h. கடுமையான புயல் எச்சரிக்கை எதுவும் தற்போது இல்லை."
                AppLanguage.TANGLISH -> "Wind speed $windSpeed km/h irukku. No cyclone or severe wind warnings right now."
                AppLanguage.ENGLISH -> "Wind speed is currently $windSpeed km/h in ${location.locality}. Atmospheric winds are gentle with no cyclone warnings in effect."
            }
        }

        // 18. GENERAL / DEFAULT INTELLIGENT SYNTHESIS
        return when (language) {
            AppLanguage.TAMIL -> "${location.locality}-ல் தற்போது $temp°C, $condition, காற்றின் வேகம் ${wind.toInt()} km/h, மழை சாத்தியக்கூறு $rainMax6h%. உங்கள் நாள் இனிதாக அமையட்டும்! வேறு ஏதேனும் சந்தேகம் இருந்தால் தாராளமாக கேளுங்கள்."
            AppLanguage.TANGLISH -> "${location.locality}-la ipo $temp°C, $condition, wind ${wind.toInt()} km/h, rain chance $rainMax6h%. Unga day super-ah pogum! Vera ethum question irundhalum direct-ah kelunga."
            AppLanguage.ENGLISH -> "Currently in ${location.locality}, it is $temp°C with $condition, humidity at $humidity%, and a $rainMax6h% rain probability. Feel free to ask me about any location, activity, or weather detail!"
        }
    }
}
