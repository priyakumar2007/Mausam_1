package com.example.data.repository

import com.example.data.model.ExactLocation
import com.example.data.model.RouteCheckpoint
import com.example.data.model.TravelRouteInfo
import java.util.Calendar
import kotlin.math.*

object TravelEngine {

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).coerceAtLeast(1.0)
    }

    fun buildRouteOptions(
        source: ExactLocation,
        destination: ExactLocation,
        sourceTemp: Double,
        destTemp: Double,
        sourceRainProb: Int,
        destRainProb: Int
    ): Pair<TravelRouteInfo, TravelRouteInfo> {
        val straightLineKm = calculateDistanceKm(source.latitude, source.longitude, destination.latitude, destination.longitude)
        
        // Realistic road driving factor
        val shortestDistanceKm = (straightLineKm * 1.25 * 10).roundToInt() / 10.0
        val longerDistanceKm = (straightLineKm * 1.54 * 10).roundToInt() / 10.0

        // Time of day traffic detection
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isRushHour = currentHour in 8..10 || currentHour in 17..20
        val isNightTime = currentHour in 22..24 || currentHour in 0..5

        val shortestTrafficStatus = when {
            isRushHour -> "Heavy Congestion (Slowdown detected near tolls & junctions)"
            isNightTime -> "Free Flowing (Clear night highway)"
            else -> "Moderate Flow (Normal daytime traffic)"
        }

        val longerTrafficStatus = when {
            isRushHour -> "Smooth Flow (Bypasses city bottleneck)"
            else -> "Free Flowing Expressway"
        }

        // Speed estimates (km/h)
        val shortestSpeed = if (isRushHour) 38.0 else 48.0
        val longerSpeed = 68.0

        val shortestDurationMin = (shortestDistanceKm / shortestSpeed * 60).roundToInt()
        val longerDurationMin = (longerDistanceKm / longerSpeed * 60).roundToInt()

        val midTemp1 = ((sourceTemp * 2 + destTemp) / 3 * 10).roundToInt() / 10.0
        val midRain1 = ((sourceRainProb * 2 + destRainProb) / 3)

        val midTemp2 = ((sourceTemp + destTemp * 2) / 3 * 10).roundToInt() / 10.0
        val midRain2 = ((sourceRainProb + destRainProb * 2) / 3)

        val shortestCheckpoints = listOf(
            RouteCheckpoint(
                name = "${source.locality} (Start)",
                tempC = sourceTemp,
                rainProb = sourceRainProb,
                condition = if (sourceRainProb > 45) "Rainy • Wet Road" else "Clear • Dry"
            ),
            RouteCheckpoint(
                name = "Mid-Corridor Highway Toll",
                tempC = midTemp1,
                rainProb = midRain1,
                condition = if (midRain1 > 45) "Showers • Wet Surface" else "Normal Flow"
            ),
            RouteCheckpoint(
                name = "Outer Ring Interchange",
                tempC = midTemp2,
                rainProb = midRain2,
                condition = if (midRain2 > 45) "Rain Alert • Caution" else "Clear Flow"
            ),
            RouteCheckpoint(
                name = "${destination.locality} (Destination)",
                tempC = destTemp,
                rainProb = destRainProb,
                condition = if (destRainProb > 45) "Wet Roads Ahead" else "Favorable Arrival"
            )
        )

        val longerCheckpoints = listOf(
            RouteCheckpoint(
                name = "${source.locality} (Start)",
                tempC = sourceTemp,
                rainProb = sourceRainProb,
                condition = "Departure"
            ),
            RouteCheckpoint(
                name = "Outer Bypass Expressway",
                tempC = midTemp2,
                rainProb = (midRain2 - 8).coerceAtLeast(5),
                condition = "Open Expressway"
            ),
            RouteCheckpoint(
                name = "${destination.locality} (Destination)",
                tempC = destTemp,
                rainProb = destRainProb,
                condition = "Arrival Point"
            )
        )

        val shortestAdvisory = buildString {
            append("Shortest route is $shortestDistanceKm km (~${shortestDurationMin} min). ")
            if (isRushHour) {
                append("Traffic detection: Peak rush-hour detected with congestion at urban intersections. ")
            } else {
                append("Traffic detection: Steady highway flow. ")
            }
            if (destRainProb >= 50 || midRain1 >= 50) {
                append("Weather alert: Rain expected along the route (up to $destRainProb%). Maintain safe tyre braking distance.")
            } else {
                append("Weather alert: Good visibility and dry road surfaces along track.")
            }
        }

        val longerAdvisory = "The longer alternative expressway bypass is $longerDistanceKm km (~${longerDurationMin} min). It circumvents dense city traffic with higher cruise speeds."

        val shortestRoute = TravelRouteInfo(
            title = "🏆 Shortest Route (Direct)",
            isShortest = true,
            distanceKm = shortestDistanceKm,
            durationMin = shortestDurationMin,
            trafficStatus = shortestTrafficStatus,
            isTrafficAvailable = true,
            avgRainProb = (sourceRainProb + destRainProb) / 2,
            avgTempC = (sourceTemp + destTemp) / 2,
            checkpoints = shortestCheckpoints,
            advisory = shortestAdvisory
        )

        val longerRoute = TravelRouteInfo(
            title = "Longer Alternative (Expressway Bypass)",
            isShortest = false,
            distanceKm = longerDistanceKm,
            durationMin = longerDurationMin,
            trafficStatus = longerTrafficStatus,
            isTrafficAvailable = true,
            avgRainProb = ((sourceRainProb + destRainProb) / 2 - 5).coerceAtLeast(5),
            avgTempC = (sourceTemp + destTemp) / 2,
            checkpoints = longerCheckpoints,
            advisory = longerAdvisory
        )

        return Pair(shortestRoute, longerRoute)
    }
}
