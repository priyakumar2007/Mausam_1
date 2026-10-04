package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// -------------------------------------------------------------
// Open-Meteo Weather API Raw Responses
// -------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class OpenMeteoWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val current: OpenMeteoCurrentWeather? = null,
    val hourly: OpenMeteoHourlyWeather? = null,
    val daily: OpenMeteoDailyWeather? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrentWeather(
    val time: String? = null,
    @Json(name = "temperature_2m") val temperature2m: Double? = 0.0,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double? = 0.0,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = 0.0,
    val precipitation: Double? = 0.0,
    @Json(name = "weather_code") val weatherCode: Int? = 0,
    @Json(name = "cloud_cover") val cloudCover: Double? = 0.0,
    @Json(name = "surface_pressure") val surfacePressure: Double? = 1013.0,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = 0.0,
    @Json(name = "wind_direction_10m") val windDirection10m: Double? = 0.0,
    @Json(name = "uv_index") val uvIndex: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourlyWeather(
    val time: List<String>? = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Double>? = emptyList(),
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Double>? = emptyList(),
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int>? = emptyList(),
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>? = emptyList(),
    @Json(name = "uv_index") val uvIndex: List<Double>? = emptyList(),
    val visibility: List<Double>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDailyWeather(
    val time: List<String>? = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int>? = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = emptyList(),
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>? = emptyList(),
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = emptyList(),
    val sunrise: List<String>? = emptyList(),
    val sunset: List<String>? = emptyList()
)

// -------------------------------------------------------------
// Open-Meteo Air Quality Raw Response
// -------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    val current: OpenMeteoCurrentAirQuality? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrentAirQuality(
    @Json(name = "european_aqi") val europeanAqi: Double? = null,
    @Json(name = "us_aqi") val usAqi: Double? = null,
    val pm10: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null
)

// -------------------------------------------------------------
// Open-Meteo Marine Raw Response
// -------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class OpenMeteoMarineResponse(
    val latitude: Double,
    val longitude: Double,
    val current: OpenMeteoCurrentMarine? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrentMarine(
    @Json(name = "wave_height") val waveHeight: Double? = null,
    @Json(name = "wave_direction") val waveDirection: Double? = null,
    @Json(name = "wave_period") val wavePeriod: Double? = null,
    @Json(name = "wind_wave_height") val windWaveHeight: Double? = null
)

// -------------------------------------------------------------
// Geocoding Responses
// -------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class GeocodingSearchResponse(
    val results: List<GeocodingResultItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeocodingResultItem(
    val id: Long? = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null, // State
    val admin2: String? = null, // District
    val admin3: String? = null, // Taluk
    val admin4: String? = null  // Locality / Sub-district
)

@JsonClass(generateAdapter = true)
data class NominatimReverseResponse(
    val name: String? = null,
    @Json(name = "display_name") val displayName: String? = null,
    val address: NominatimAddress? = null
)

@JsonClass(generateAdapter = true)
data class NominatimSearchResult(
    val place_id: Long? = null,
    val lat: String? = null,
    val lon: String? = null,
    val name: String? = null,
    @Json(name = "display_name") val displayName: String? = null,
    val address: NominatimAddress? = null
)

@JsonClass(generateAdapter = true)
data class NominatimAddress(
    val village: String? = null,
    val town: String? = null,
    val city: String? = null,
    val suburb: String? = null,
    val neighbourhood: String? = null,
    val county: String? = null,
    val state_district: String? = null,
    val state: String? = null,
    val country: String? = null
)

// -------------------------------------------------------------
// Domain / UI Models
// -------------------------------------------------------------

data class ExactLocation(
    val locality: String,
    val district: String,
    val state: String,
    val country: String = "India",
    val latitude: Double,
    val longitude: Double
) {
    val fullDisplayName: String
        get() = buildString {
            append(locality)
            if (district.isNotBlank() && !district.equals(locality, ignoreCase = true)) {
                append(", $district")
            }
            if (state.isNotBlank()) {
                append(", $state")
            }
        }
}

data class CurrentWeather(
    val temperatureC: Double,
    val feelsLikeC: Double,
    val weatherCode: Int,
    val conditionText: String,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Double,
    val uvIndex: Double,
    val visibilityKm: Double,
    val surfacePressureHpa: Double,
    val cloudCoverPercent: Int,
    val precipitationMm: Double,
    val sunrise: String,
    val sunset: String
)

data class HourlyForecastItem(
    val timeIso: String,
    val hourLabel: String,
    val tempC: Double,
    val rainProbability: Int,
    val windSpeedKmh: Double,
    val humidityPercent: Int,
    val uvIndex: Double,
    val weatherCode: Int
)

data class DailyForecastItem(
    val dateIso: String,
    val dayOfWeek: String,
    val weatherCode: Int,
    val conditionText: String,
    val maxTempC: Double,
    val minTempC: Double,
    val rainProbMax: Int,
    val windSpeedMaxKmh: Double,
    val uvMax: Double,
    val sunrise: String,
    val sunset: String
)

data class AirQualityInfo(
    val aqi: Int,
    val aqiCategory: String, // Good, Moderate, Poor, Unhealthy
    val pm25: Double,
    val pm10: Double,
    val uvIndex: Double,
    val comfortScore: Int, // 0 - 100
    val comfortExplanation: String
)

data class BeachSafetyInfo(
    val beachId: String,
    val name: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val airTempC: Double,
    val waterTempC: Double?,
    val waveHeightM: Double?,
    val wavePeriodSec: Double?,
    val waveDirectionDeg: Double?,
    val windSpeedKmh: Double,
    val rainProb: Int,
    val uvIndex: Double,
    val safetyStatus: SafetyStatus,
    val safetyReasons: List<String>,
    val isRecommendedAlternative: Boolean = false
)

enum class SafetyStatus {
    SUITABLE,
    CAUTION,
    HIGH_RISK
}

data class RouteCheckpoint(
    val name: String,
    val tempC: Double,
    val rainProb: Int,
    val condition: String
)

data class TravelRouteInfo(
    val title: String, // "Shortest Route", "Longer Alternative Route"
    val isShortest: Boolean,
    val distanceKm: Double,
    val durationMin: Int,
    val trafficStatus: String, // "Low Traffic", "Moderate Traffic", "Heavy Traffic", "Live traffic data unavailable"
    val isTrafficAvailable: Boolean,
    val avgRainProb: Int,
    val avgTempC: Double,
    val checkpoints: List<RouteCheckpoint>,
    val advisory: String
)

enum class CropRiskLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

data class CropRiskAssessment(
    val riskLevel: CropRiskLevel,
    val reasons: List<String>,
    val recommendations: List<String>,
    val visualObservation: String,
    val weatherSummary: String,
    val isImageQualitySufficient: Boolean,
    val qualityWarning: String? = null,
    val fieldLocation: ExactLocation
)

enum class AlertCategory {
    RAIN, STORM, WIND, HEAT, UV, AQI, MARINE, AGRICULTURE, TRAVEL, GENERAL
}

enum class AlertSeverity {
    INFO, MODERATE, SEVERE
}

data class MausamAlert(
    val id: String,
    val title: String,
    val category: AlertCategory,
    val severity: AlertSeverity,
    val isOfficial: Boolean,
    val source: String,
    val description: String,
    val timeLabel: String
)

enum class AppLanguage {
    ENGLISH,
    TAMIL,
    TANGLISH
}

enum class LifestyleType(val label: String, val emoji: String) {
    HEALTH("Health", "❤️"),
    FITNESS("Fitness", "🏃"),
    BEACH("Beach", "🏖"),
    TRAVEL("Travel", "✈️"),
    FAMILY("Family", "👨‍👩‍👧"),
    AGRICULTURE("Agriculture", "🌱"),
    COMMUTER("Commuter", "🚗"),
    EVENTS("Events", "🎉")
}
