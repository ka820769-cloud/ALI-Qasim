package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthBiometrics
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseCoral
import com.example.ui.theme.PulseCoralGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun HeartRatePulseCard(
    biometrics: HealthBiometrics,
    ecgWave: List<Float>,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val heartScale = remember { Animatable(1f) }

    // Pulsing heartbeat animation synced with biometrics BPM
    LaunchedEffect(biometrics.heartRate, isConnected) {
        if (isConnected) {
            val bpm = biometrics.heartRate.coerceIn(40, 220)
            val intervalMs = (60000L / bpm).coerceIn(280L, 1500L)
            while (true) {
                heartScale.animateTo(
                    targetValue = 1.25f,
                    animationSpec = tween(120, easing = FastOutSlowInEasing)
                )
                heartScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
                val remainingDelay = (intervalMs - 320L).coerceAtLeast(50L)
                delay(remainingDelay)
            }
        } else {
            heartScale.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(DarkSurface, DarkSurfaceVariant)
                )
            )
            .border(
                1.dp,
                if (isConnected) biometrics.hrZone.color.copy(alpha = 0.4f) else DarkSurfaceVariant,
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
            .testTag("heart_rate_card")
    ) {
        Column {
            // Header Row: Title, Pulsing Heart Icon, Zone Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PulseCoralGlow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heartbeat",
                            tint = PulseCoral,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(heartScale.value)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "LIVE HEART RATE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (isConnected) "Wearable Sensor Stream" else "Sensor Disconnected",
                            fontSize = 11.sp,
                            color = if (isConnected) PulseCoral else TextSecondary
                        )
                    }
                }

                // HR Zone Pill
                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(biometrics.hrZone.color.copy(alpha = 0.18f))
                            .border(1.dp, biometrics.hrZone.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = biometrics.hrZone.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = biometrics.hrZone.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // BPM Counter & Range stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (isConnected) biometrics.heartRate.toString() else "--",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-1.5).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BPM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PulseCoral
                    )
                }

                // Min / Max Stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "MIN", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isConnected) "${biometrics.minHeartRateToday} bpm" else "--",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "MAX", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isConnected) "${biometrics.maxHeartRateToday} bpm" else "--",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = biometrics.hrZone.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-Time ECG Waveform Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF090D1A))
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                EcgWaveformCanvas(
                    wavePoints = ecgWave,
                    waveColor = if (isConnected) biometrics.hrZone.color else TextSecondary,
                    isConnected = isConnected
                )
            }
        }
    }
}
