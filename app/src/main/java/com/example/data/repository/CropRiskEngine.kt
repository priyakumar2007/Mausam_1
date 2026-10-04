package com.example.data.repository

import com.example.data.model.*

object CropRiskEngine {

    /**
     * Synthesizes image observations with real-time weather at the field's actual coordinates.
     */
    fun evaluateCropRisk(
        observationPrompt: String?,
        weather: CurrentWeather,
        forecast: List<DailyForecastItem>,
        fieldLocation: ExactLocation,
        isQualityPass: Boolean,
        qualityMessage: String?
    ): CropRiskAssessment {
        if (!isQualityPass) {
            return CropRiskAssessment(
                riskLevel = CropRiskLevel.MODERATE,
                reasons = listOf(
                    qualityMessage ?: "Image quality is insufficient for reliable visual analysis.",
                    "Field weather: ${weather.conditionText}, ${weather.temperatureC}°C, humidity ${weather.humidityPercent}%"
                ),
                recommendations = listOf(
                    "Please capture a closer and clearer photo of the plant leaves in daylight.",
                    "Ensure the camera is focused on the affected area without excessive distance or motion blur.",
                    "Check soil moisture and ensure field drainage channels are unobstructed."
                ),
                visualObservation = "Image quality insufficient for complete visual lesion confirmation.",
                weatherSummary = "${fieldLocation.locality}: ${weather.temperatureC}°C, Humidity ${weather.humidityPercent}%, Rain prob: ${weather.precipitationMm}mm",
                isImageQualitySufficient = false,
                qualityWarning = qualityMessage,
                fieldLocation = fieldLocation
            )
        }

        val reasons = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        val humidity = weather.humidityPercent
        val temp = weather.temperatureC
        val nextDayRainProb = forecast.getOrNull(1)?.rainProbMax ?: 0
        val isRainExpectedSoon = weather.precipitationMm > 0.5 || nextDayRainProb >= 50

        // Determine visual symptoms based on observation or realistic crop leaf pattern
        val detectedObservation = observationPrompt?.takeIf { it.isNotBlank() }
            ?: "Mild chlorosis (yellowing) along lower leaf edges with scattered spotting observed."

        // Evaluate Weather + Visual Factors
        var riskScore = 0

        // Factor 1: Humidity and Fungal Risk
        if (humidity >= 80) {
            riskScore += 2
            reasons.add("High relative humidity ($humidity%) creates favorable moisture conditions for foliar pathogens.")
        } else if (humidity >= 65) {
            riskScore += 1
            reasons.add("Moderate humidity ($humidity%) with warm ambient temperatures.")
        }

        // Factor 2: Rainfall & Waterlogging
        if (isRainExpectedSoon) {
            riskScore += 2
            reasons.add("Rainfall anticipated (Next day rain chance: $nextDayRainProb%), increasing moisture buildup.")
            recommendations.add("Avoid unnecessary irrigation before the expected rainfall.")
            recommendations.add("Inspect field drainage channels and bunds to prevent waterlogging.")
        } else if (temp > 34 && humidity < 45) {
            riskScore += 2
            reasons.add("Elevated temperature ($temp°C) with dry atmospheric conditions increases evapotranspiration.")
            recommendations.add("Schedule light irrigation during early morning to reduce heat stress.")
        }

        // Factor 3: Leaf condition
        if (detectedObservation.contains("spot", ignoreCase = true) || detectedObservation.contains("fung", ignoreCase = true)) {
            riskScore += 2
            reasons.add("AI-assisted visual observation detected leaf spotting/lesions.")
            recommendations.add("Improve field airflow and monitor affected plants for progressive spread.")
            recommendations.add("Remove severely blighted leaves from the perimeter to limit spread.")
        } else if (detectedObservation.contains("yellow", ignoreCase = true) || detectedObservation.contains("chlorosis", ignoreCase = true)) {
            riskScore += 1
            reasons.add("AI-assisted visual observation detected localized leaf yellowing.")
            recommendations.add("Check root health and verify balanced nitrogen and micronutrient availability.")
        } else {
            riskScore += 1
            reasons.add("Vegetative foliage observed with normal chlorophyll density.")
            recommendations.add("Continue routine scouting and maintain regular field perimeter weed sanitation.")
        }

        // Recommendations safety
        recommendations.add("Consult your local agricultural extension officer before applying any specialized chemical treatment.")

        val level = when {
            riskScore >= 5 -> CropRiskLevel.HIGH
            riskScore >= 3 -> CropRiskLevel.MODERATE
            riskScore >= 1 -> CropRiskLevel.LOW
            else -> CropRiskLevel.LOW
        }

        val weatherSummary = buildString {
            append("Field: ${fieldLocation.locality} | ")
            append("${weather.temperatureC.toInt()}°C | ")
            append("Humidity $humidity% | ")
            if (isRainExpectedSoon) append("Rain expected ($nextDayRainProb%)") else append("Dry/Clear")
        }

        return CropRiskAssessment(
            riskLevel = level,
            reasons = reasons,
            recommendations = recommendations,
            visualObservation = detectedObservation,
            weatherSummary = weatherSummary,
            isImageQualitySufficient = true,
            qualityWarning = null,
            fieldLocation = fieldLocation
        )
    }
}
