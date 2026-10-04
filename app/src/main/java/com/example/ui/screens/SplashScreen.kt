package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    // Floating sun/cloud infinite bounce
    val infiniteTransition = rememberInfiniteTransition(label = "cloud_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2600)
        onSplashFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepIndigoBackground, Color(0xFF1E1B4B), Color(0xFF0F172A))
                )
            )
            .testTag("splash_screen_container")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Weather Icon Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .offset(y = offsetY.dp)
            ) {
                Text(
                    text = "⛅",
                    fontSize = 72.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MAUSAM",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                ),
                color = SkyBlueLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Weather that fits your life.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = SkyBlueContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Subtle animated weather dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("☀️", "🌧️", "🌱", "✈️", "🏖️").forEach { icon ->
                    Text(text = icon, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onSplashFinished,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.testTag("skip_splash_button")
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
