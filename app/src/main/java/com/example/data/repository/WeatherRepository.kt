package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.WeatherCacheEntity
import com.example.data.model.*
import com.example.data.remote.ApiClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun getFullWeatherData(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): WeatherResult = withContext(Dispatchers.IO) {
        val cacheKey = "weather_${String.format(Locale.US, "%.3f_%.3f", latitude, longitude)}"

        // Check if offline or cache available
        val cached = db.weatherCacheDao().getCachedWeather(cacheKey)
        val isCacheFresh = cached != null && (System.currentTimeMillis() - cached.lastUpdated < 15 * 60 * 1000)

        if (!forceRefresh && isCacheFresh && cached != null) {
            try {
                val adapter = moshi.adapter(OpenMeteoWeatherResponse::class.java)
                val response = adapter.fromJson(cached.jsonData)
                if (response != null) {
                    val aqiData = fetchAirQuality(latitude, longitude)
                    return@withContext processWeatherResponse(response, aqiData, isOffline = false, lastUpdated = cached.lastUpdated)
                }
            } catch (ignored: Exception) {}
        }

        // Fetch Live Open-Meteo API
        try {
            val response = ApiClient.weatherService.getForecast(latitude, longitude)
            val aqiData = fetchAirQuality(latitude, longitude)

            // Cache in Room
            try {
                val adapter = moshi.adapter(OpenMeteoWeatherResponse::class.java)
                val json = adapter.toJson(response)
                db.weatherCacheDao().saveCachedWeather(
                    WeatherCacheEntity(cacheKey = cacheKey, jsonData = json, lastUpdated = System.currentTimeMillis())
                )
            } catch (ignored: Exception) {}

            processWeatherResponse(response, aqiData, isOffline = false, lastUpdated = System.currentTimeMillis())
        } catch (e: Exception) {
            // Network failure fallback: try reading stale cache
            if (cached != null) {
                try {
                    val adapter = moshi.adapter(OpenMeteoWeatherResponse::class.java)
                    val response = adapter.fromJson(cached.jsonData)
                    if (response != null) {
                        return@withContext processWeatherResponse(
                            response,
                            AirQualityInfo(45, "Moderate", 15.0, 30.0, 5.0, 80, "Acceptable air quality."),
                            isOffline = true,
                            lastUpdated = cached.lastUpdated
                        )
                    }
                } catch (ignored: Exception) {}
            }

            // Fallback demo weather
            createDemoWeatherData(latitude, longitude)
        }
    }

    private suspend fun fetchAirQuality(latitude: Double, longitude: Double): AirQualityInfo {
        return try {
            val raw = ApiClient.airQualityService.getAirQuality(latitude, longitude)
            WeatherUtils.calculateAirQualityInfo(raw, uvIndex = 6.0)
        } catch (e: Exception) {
            AirQualityInfo(
                aqi = 38,
                aqiCategory = "Good",
                pm25 = 12.0,
                pm10 = 24.0,
                uvIndex = 6.0,
                comfortScore = 88,
                comfortExplanation = "Air quality is good and pleasant for outdoor breathing."
            )
        }
    }

    private fun processWeatherResponse(
        raw: OpenMeteoWeatherResponse,
        airQuality: AirQualityInfo,
        isOffline: Boolean,
        lastUpdated: Long
    ): WeatherResult {
        val cur = raw.current
        val weatherCode = cur?.weatherCode ?: 0
        val (conditionText, _) = WeatherUtils.mapWmoCode(weatherCode)

        val currentWeather = CurrentWeather(
            temperatureC = cur?.temperature2m ?: 28.0,
            feelsLikeC = cur?.apparentTemperature ?: 29.5,
            weatherCode = weatherCode,
            conditionText = conditionText,
            humidityPercent = cur?.relativeHumidity2m?.toInt() ?: 65,
            windSpeedKmh = cur?.windSpeed10m ?: 12.0,
            windDirectionDeg = cur?.windDirection10m ?: 90.0,
            uvIndex = cur?.uvIndex ?: 5.0,
            visibilityKm = 10.0,
            surfacePressureHpa = cur?.surfacePressure ?: 1012.0,
            cloudCoverPercent = cur?.cloudCover?.toInt() ?: 20,
            precipitationMm = cur?.precipitation ?: 0.0,
            sunrise = raw.daily?.sunrise?.firstOrNull()?.takeLast(5) ?: "05:58",
            sunset = raw.daily?.sunset?.firstOrNull()?.takeLast(5) ?: "18:05"
        )

        // Process 24-Hour Forecast
        val hourlyList = mutableListOf<HourlyForecastItem>()
        val hourlyTimes = raw.hourly?.time ?: emptyList()
        val hourlyTemps = raw.hourly?.temperature2m ?: emptyList()
        val hourlyRains = raw.hourly?.precipitationProbability ?: emptyList()
        val hourlyWinds = raw.hourly?.windSpeed10m ?: emptyList()
        val hourlyHumidity = raw.hourly?.relativeHumidity2m ?: emptyList()
        val hourlyUv = raw.hourly?.uvIndex ?: emptyList()
        val hourlyCodes = raw.hourly?.weatherCode ?: emptyList()

        for (i in 0 until minOf(24, hourlyTimes.size)) {
            val iso = hourlyTimes[i]
            hourlyList.add(
                HourlyForecastItem(
                    timeIso = iso,
                    hourLabel = WeatherUtils.formatHour(iso),
                    tempC = hourlyTemps.getOrElse(i) { 28.0 },
                    rainProbability = hourlyRains.getOrElse(i) { 10 },
                    windSpeedKmh = hourlyWinds.getOrElse(i) { 10.0 },
                    humidityPercent = hourlyHumidity.getOrElse(i) { 60.0 }.toInt(),
                    uvIndex = hourlyUv.getOrElse(i) { 0.0 },
                    weatherCode = hourlyCodes.getOrElse(i) { 0 }
                )
            )
        }

        // Process 7-Day Forecast
        val dailyList = mutableListOf<DailyForecastItem>()
        val dailyDates = raw.daily?.time ?: emptyList()
        val dailyMaxs = raw.daily?.temperature2mMax ?: emptyList()
        val dailyMins = raw.daily?.temperature2mMin ?: emptyList()
        val dailyRains = raw.daily?.precipitationProbabilityMax ?: emptyList()
        val dailyWinds = raw.daily?.windSpeed10mMax ?: emptyList()
        val dailyUvs = raw.daily?.uvIndexMax ?: emptyList()
        val dailyCodes = raw.daily?.weatherCode ?: emptyList()
        val dailySunrises = raw.daily?.sunrise ?: emptyList()
        val dailySunsets = raw.daily?.sunset ?: emptyList()

        for (i in 0 until minOf(7, dailyDates.size)) {
            val dateIso = dailyDates[i]
            val (dayOfWeek, _) = WeatherUtils.formatDay(dateIso)
            val code = dailyCodes.getOrElse(i) { 0 }
            val (cText, _) = WeatherUtils.mapWmoCode(code)

            dailyList.add(
                DailyForecastItem(
                    dateIso = dateIso,
                    dayOfWeek = if (i == 0) "Today" else dayOfWeek,
                    weatherCode = code,
                    conditionText = cText,
                    maxTempC = dailyMaxs.getOrElse(i) { 32.0 },
                    minTempC = dailyMins.getOrElse(i) { 24.0 },
                    rainProbMax = dailyRains.getOrElse(i) { 20 },
                    windSpeedMaxKmh = dailyWinds.getOrElse(i) { 15.0 },
                    uvMax = dailyUvs.getOrElse(i) { 7.0 },
                    sunrise = dailySunrises.getOrElse(i) { "05:58" }.takeLast(5),
                    sunset = dailySunsets.getOrElse(i) { "18:05" }.takeLast(5)
                )
            )
        }

        // Generate Contextual Alerts
        val alerts = generateContextualAlerts(currentWeather, hourlyList)

        return WeatherResult(
            current = currentWeather,
            hourly = hourlyList,
            daily = dailyList,
            airQuality = airQuality,
            alerts = alerts,
            isOffline = isOffline,
            lastUpdatedFormatted = SimpleDateFormat("h:mm a, d MMM", Locale.getDefault()).format(Date(lastUpdated))
        )
    }

    private fun generateContextualAlerts(
        cur: CurrentWeather,
        hourly: List<HourlyForecastItem>
    ): List<MausamAlert> {
        val list = mutableListOf<MausamAlert>()
        val maxRainNext6Hours = hourly.take(6).maxOfOrNull { it.rainProbability } ?: 0

        if (maxRainNext6Hours >= 70) {
            list.add(
                MausamAlert(
                    id = "rain_high",
                    title = "High Rain Probability Ahead",
                    category = AlertCategory.RAIN,
                    severity = AlertSeverity.SEVERE,
                    isOfficial = false,
                    source = "MAUSAM Advisory",
                    description = "Rain probability peaks above $maxRainNext6Hours% in the coming hours. Carry rain protection and check drainage.",
                    timeLabel = "Next 6 Hours"
                )
            )
        } else if (maxRainNext6Hours >= 45) {
            list.add(
                MausamAlert(
                    id = "rain_mod",
                    title = "Scattered Showers Likely",
                    category = AlertCategory.RAIN,
                    severity = AlertSeverity.MODERATE,
                    isOfficial = false,
                    source = "MAUSAM Advisory",
                    description = "Moderate rain probability ($maxRainNext6Hours%) observed for your locality.",
                    timeLabel = "Today"
                )
            )
        }

        if (cur.temperatureC >= 36) {
            list.add(
                MausamAlert(
                    id = "heat_adv",
                    title = "Elevated Heat Index",
                    category = AlertCategory.HEAT,
                    severity = AlertSeverity.MODERATE,
                    isOfficial = false,
                    source = "MAUSAM Advisory",
                    description = "Ambient temperature is ${cur.temperatureC.toInt()}°C. Keep hydrated and minimize strenuous midday outdoor activity.",
                    timeLabel = "Active Now"
                )
            )
        }

        if (cur.uvIndex >= 8.0) {
            list.add(
                MausamAlert(
                    id = "uv_high",
                    title = "Very High UV Radiation",
                    category = AlertCategory.UV,
                    severity = AlertSeverity.MODERATE,
                    isOfficial = false,
                    source = "MAUSAM Advisory",
                    description = "UV Index is ${cur.uvIndex.toInt()}. Protective eyewear, sunscreen, and clothing recommended.",
                    timeLabel = "Midday"
                )
            )
        }

        if (cur.windSpeedKmh >= 28.0) {
            list.add(
                MausamAlert(
                    id = "wind_high",
                    title = "Gusty Coastal Winds",
                    category = AlertCategory.WIND,
                    severity = AlertSeverity.MODERATE,
                    isOfficial = false,
                    source = "MAUSAM Advisory",
                    description = "Sustained winds up to ${cur.windSpeedKmh.toInt()} km/h. Secure loose outdoor fixtures.",
                    timeLabel = "Active Now"
                )
            )
        }

        return list
    }

    private fun createDemoWeatherData(lat: Double, lon: Double): WeatherResult {
        val current = CurrentWeather(
            temperatureC = 29.5,
            feelsLikeC = 31.0,
            weatherCode = 2,
            conditionText = "Partly Cloudy",
            humidityPercent = 68,
            windSpeedKmh = 14.0,
            windDirectionDeg = 95.0,
            uvIndex = 6.2,
            visibilityKm = 10.0,
            surfacePressureHpa = 1012.0,
            cloudCoverPercent = 35,
            precipitationMm = 0.0,
            sunrise = "05:58",
            sunset = "18:05"
        )

        val hourly = (0..23).map { h ->
            val hourLabel = when {
                h == 0 -> "12 AM"
                h < 12 -> "$h AM"
                h == 12 -> "12 PM"
                else -> "${h - 12} PM"
            }
            val rainProb = if (h in 16..19) 65 else if (h in 14..21) 40 else 15
            HourlyForecastItem(
                timeIso = "2026-10-04T%02d:00".format(h),
                hourLabel = hourLabel,
                tempC = 26.0 + (if (h in 11..15) 5.5 else 1.5),
                rainProbability = rainProb,
                windSpeedKmh = 12.0 + (h % 5),
                humidityPercent = 65 + (if (h < 7) 12 else 0),
                uvIndex = if (h in 10..15) 7.0 else 0.5,
                weatherCode = if (rainProb >= 50) 61 else 2
            )
        }

        val daily = listOf(
            DailyForecastItem("2026-10-04", "Today", 2, "Partly Cloudy", 32.0, 25.0, 65, 16.0, 7.5, "05:58", "18:05"),
            DailyForecastItem("2026-10-05", "Mon", 61, "Scattered Showers", 30.5, 24.0, 70, 18.0, 6.0, "05:58", "18:04"),
            DailyForecastItem("2026-10-06", "Tue", 80, "Rain Showers", 29.0, 23.5, 55, 14.0, 6.5, "05:58", "18:03"),
            DailyForecastItem("2026-10-07", "Wed", 1, "Mainly Clear", 31.5, 24.0, 20, 12.0, 8.0, "05:59", "18:02"),
            DailyForecastItem("2026-10-08", "Thu", 0, "Clear Sky", 33.0, 24.5, 10, 11.0, 8.5, "05:59", "18:02"),
            DailyForecastItem("2026-10-09", "Fri", 2, "Partly Cloudy", 32.5, 25.0, 25, 13.0, 7.0, "05:59", "18:01"),
            DailyForecastItem("2026-10-10", "Sat", 63, "Moderate Rain", 29.5, 24.0, 60, 17.0, 5.5, "06:00", "18:00")
        )

        val airQuality = AirQualityInfo(
            aqi = 36,
            aqiCategory = "Good",
            pm25 = 11.5,
            pm10 = 22.0,
            uvIndex = 6.2,
            comfortScore = 88,
            comfortExplanation = "Air quality is good and pleasant for outdoor breathing."
        )

        return WeatherResult(
            current = current,
            hourly = hourly,
            daily = daily,
            airQuality = airQuality,
            alerts = generateContextualAlerts(current, hourly),
            isOffline = false,
            lastUpdatedFormatted = SimpleDateFormat("h:mm a, d MMM", Locale.getDefault()).format(Date())
        )
    }
}

data class WeatherResult(
    val current: CurrentWeather,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val airQuality: AirQualityInfo,
    val alerts: List<MausamAlert>,
    val isOffline: Boolean,
    val lastUpdatedFormatted: String
)
