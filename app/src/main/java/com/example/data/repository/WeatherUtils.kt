package com.example.data.repository

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object WeatherUtils {

    fun mapWmoCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Pair("Clear Sky", "☀️")
            1 -> Pair("Mainly Clear", "🌤️")
            2 -> Pair("Partly Cloudy", "⛅")
            3 -> Pair("Overcast", "☁️")
            45, 48 -> Pair("Foggy", "🌫️")
            51, 53, 55 -> Pair("Light Drizzle", "🌦️")
            56, 57 -> Pair("Freezing Drizzle", "🌧️")
            61 -> Pair("Slight Rain", "🌧️")
            63 -> Pair("Moderate Rain", "🌧️")
            65 -> Pair("Heavy Rain", "⛈️")
            66, 67 -> Pair("Freezing Rain", "🌨️")
            71, 73, 75 -> Pair("Snowfall", "❄️")
            77 -> Pair("Snow Grains", "❄️")
            80, 81, 82 -> Pair("Rain Showers", "🌦️")
            85, 86 -> Pair("Snow Showers", "🌨️")
            95 -> Pair("Thunderstorm", "⛈️")
            96, 99 -> Pair("Thunderstorm with Hail", "⛈️")
            else -> Pair("Fair", "🌤️")
        }
    }

    fun formatHour(isoTime: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val date = parser.parse(isoTime) ?: return isoTime.takeLast(5)
            val formatter = SimpleDateFormat("h a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            isoTime.takeLast(5)
        }
    }

    fun formatDay(isoDate: String): Pair<String, String> {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(isoDate) ?: return Pair(isoDate, "Today")
            val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
            val dateFormatter = SimpleDateFormat("d MMM", Locale.getDefault())
            Pair(dayFormatter.format(date), dateFormatter.format(date))
        } catch (e: Exception) {
            Pair("Day", isoDate)
        }
    }

    fun generateMausamInsight(
        weather: CurrentWeather,
        hourly: List<HourlyForecastItem>,
        selectedLifestyles: Set<LifestyleType>,
        language: AppLanguage
    ): String {
        val highRainWindow = findRainWindow(hourly)
        val temp = weather.temperatureC.toInt()
        val humidity = weather.humidityPercent

        return when (language) {
            AppLanguage.ENGLISH -> buildEnglishInsight(temp, humidity, weather, highRainWindow, selectedLifestyles)
            AppLanguage.TAMIL -> buildTamilInsight(temp, humidity, weather, highRainWindow, selectedLifestyles)
            AppLanguage.TANGLISH -> buildTanglishInsight(temp, humidity, weather, highRainWindow, selectedLifestyles)
        }
    }

    private fun findRainWindow(hourly: List<HourlyForecastItem>): Pair<String, String>? {
        val rainHours = hourly.take(12).filter { it.rainProbability >= 50 }
        if (rainHours.isNotEmpty()) {
            val start = rainHours.first().hourLabel
            val end = rainHours.last().hourLabel
            return Pair(start, end)
        }
        return null
    }

    private fun buildEnglishInsight(
        temp: Int,
        humidity: Int,
        weather: CurrentWeather,
        rainWindow: Pair<String, String>?,
        lifestyles: Set<LifestyleType>
    ): String {
        val greeting = getGreeting()
        val base = when {
            rainWindow != null -> "$greeting It's $temp°C with ${weather.conditionText.lowercase()}. Rain probability increases between ${rainWindow.first} and ${rainWindow.second}, so carrying an umbrella would be useful."
            temp > 34 -> "$greeting It's hot at $temp°C with $humidity% humidity. Staying hydrated and reducing prolonged sun exposure is advised."
            temp < 20 -> "$greeting Crisp and cool weather at $temp°C. A great time for outdoor movement."
            humidity > 75 -> "$greeting It's $temp°C with elevated humidity ($humidity%). Outdoor activities may feel warmer than actual temperature."
            else -> "$greeting Pleasant conditions at $temp°C with good visibility. Ideal for your daily activities."
        }

        val extra = when {
            lifestyles.contains(LifestyleType.AGRICULTURE) && (weather.precipitationMm > 0 || (rainWindow != null)) ->
                " 🌱 Agriculture note: Monitor soil drainage before initiating irrigation."
            lifestyles.contains(LifestyleType.FITNESS) ->
                " 🏃 Fitness tip: Morning and late evening offer the most comfortable breeze."
            lifestyles.contains(LifestyleType.BEACH) && weather.windSpeedKmh > 20 ->
                " 🏖 Coastal note: Sea breeze is active with elevated surface chop."
            else -> ""
        }

        return base + extra
    }

    private fun buildTamilInsight(
        temp: Int,
        humidity: Int,
        weather: CurrentWeather,
        rainWindow: Pair<String, String>?,
        lifestyles: Set<LifestyleType>
    ): String {
        val base = when {
            rainWindow != null -> "வணக்கம்! தற்போதைய வெப்பநிலை $temp°C. ${rainWindow.first} மற்றும் ${rainWindow.second} இடையே மழை பெய்ய அதிக வாய்ப்புள்ளது, குடை எடுத்துச் செல்வது நல்லது."
            temp > 34 -> "வணக்கம்! வெப்பம் $temp°C ஆக உள்ளது, ஈரப்பதம் $humidity%. போதுமான அளவு தண்ணீர் குடிக்கவும்."
            else -> "வணக்கம்! இன்று இனிமையான வானிலை ($temp°C). உங்களின் அன்றாட பணிகளுக்கு ஏற்ற சூழல்."
        }
        val extra = if (lifestyles.contains(LifestyleType.AGRICULTURE)) " 🌱 விவசாய குறிப்பு: மழைக்கு முன் தேவையற்ற பாசனத்தை தவிர்க்கவும்." else ""
        return base + extra
    }

    private fun buildTanglishInsight(
        temp: Int,
        humidity: Int,
        weather: CurrentWeather,
        rainWindow: Pair<String, String>?,
        lifestyles: Set<LifestyleType>
    ): String {
        val base = when {
            rainWindow != null -> "Good day! Ipo $temp°C irukku. ${rainWindow.first} to ${rainWindow.second} kulla rain vara chance irukku, umbrella eduthukonga."
            temp > 34 -> "Good day! Veyil $temp°C irukku, humidity $humidity%. Nalla water kudinga."
            else -> "Good day! Weather nallaa irukku ($temp°C). Outside activities-ku perfect time."
        }
        val extra = if (lifestyles.contains(LifestyleType.AGRICULTURE)) " 🌱 Field note: Rain irukumbothu extra irrigation vendam." else ""
        return base + extra
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning!"
            in 12..16 -> "Good afternoon!"
            in 17..21 -> "Good evening!"
            else -> "Good night!"
        }
    }

    fun calculateFitnessScore(
        tempC: Double,
        humidityPercent: Int,
        windKmh: Double,
        rainProb: Int,
        uvIndex: Double
    ): Pair<Int, String> {
        var score = 100

        if (tempC > 30) score -= ((tempC - 30) * 4).toInt()
        else if (tempC < 15) score -= ((15 - tempC) * 3).toInt()

        if (humidityPercent > 70) score -= ((humidityPercent - 70) * 0.7).toInt()
        if (rainProb > 30) score -= (rainProb * 0.4).toInt()
        if (windKmh > 25) score -= ((windKmh - 25) * 1.2).toInt()
        if (uvIndex > 7) score -= ((uvIndex - 7) * 4).toInt()

        val finalScore = score.coerceIn(15, 100)
        val explanation = when {
            finalScore >= 80 -> "Conditions are comfortable because temperature, wind and UV are moderate."
            finalScore >= 60 -> "Moderate comfort. Humidity is elevated, keep hydration high."
            rainProb >= 60 -> "Comfort is reduced due to high rain probability."
            tempC >= 34 -> "Comfort is reduced because temperature and heat index are elevated."
            else -> "Reduced comfort. Better to schedule runs during early morning or sunset."
        }
        return Pair(finalScore, explanation)
    }

    fun calculateAirQualityInfo(raw: OpenMeteoAirQualityResponse?, uvIndex: Double): AirQualityInfo {
        val aqi = raw?.current?.europeanAqi?.toInt()
            ?: raw?.current?.usAqi?.toInt()
            ?: 42
        val pm25 = raw?.current?.pm25 ?: 14.2
        val pm10 = raw?.current?.pm10 ?: 28.5

        val category = when {
            aqi <= 30 -> "Good"
            aqi <= 50 -> "Moderate"
            aqi <= 80 -> "Poor"
            else -> "Very Poor"
        }

        val comfortScore = (100 - (aqi * 0.8) - (uvIndex * 3)).toInt().coerceIn(20, 98)
        val explanation = when {
            aqi <= 35 -> "Air quality is fresh and clean. Great conditions for outdoor time."
            aqi <= 60 -> "Acceptable air quality. Sensitive individuals should monitor prolonged exertion."
            else -> "Elevated particulate matter. Consider light indoor workouts or wearing a mask."
        }

        return AirQualityInfo(
            aqi = aqi,
            aqiCategory = category,
            pm25 = pm25,
            pm10 = pm10,
            uvIndex = uvIndex,
            comfortScore = comfortScore,
            comfortExplanation = explanation
        )
    }
}
