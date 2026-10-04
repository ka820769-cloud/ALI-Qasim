package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExerciseType
import com.example.ui.FitnessViewModel
import com.example.ui.components.BiometricsGrid
import com.example.ui.components.CalorieProgressCard
import com.example.ui.components.DailyGoalsProgressCard
import com.example.ui.components.HeartRatePulseCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCoral
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    viewModel: FitnessViewModel,
    onNavigateToLog: () -> Unit,
    onNavigateToWearable: () -> Unit,
    onStartLiveWorkout: (ExerciseType) -> Unit,
    onNavigateToTrends: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val biometrics by viewModel.biometrics.collectAsStateWithLifecycle()
    val ecgWave by viewModel.ecgWave.collectAsStateWithLifecycle()
    val todaySnapshot by viewModel.todaySnapshot.collectAsStateWithLifecycle()
    val dailyCalorieGoal by viewModel.dailyCalorieGoal.collectAsStateWithLifecycle()
    val dailyDurationGoal by viewModel.dailyDurationGoal.collectAsStateWithLifecycle()
    val todayWorkoutMinutes by viewModel.todayWorkoutMinutes.collectAsStateWithLifecycle()
    val dailyStepGoal by viewModel.dailyStepGoal.collectAsStateWithLifecycle()

    var showLivePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top App Bar: App Brand & Wearable Quick Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PulseSync",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PulseCyan,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Real-Time Health Monitoring",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Connected Wearable Pill
                if (connectedDevice != null) {
                    val dev = connectedDevice!!
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, PulseCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { onNavigateToWearable() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = dev.type.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dev.name.take(12),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Battery5Bar,
                                contentDescription = "Battery",
                                tint = PulseMint,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${dev.batteryLevel}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(14.dp))
                            .clickable { onNavigateToWearable() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Connect Wearable ⌚",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PulseCyan
                        )
                    }
                }
            }
        }

        // Live Heart Rate & ECG Monitor Card
        item {
            HeartRatePulseCard(
                biometrics = biometrics,
                ecgWave = ecgWave,
                isConnected = connectedDevice != null
            )
        }

        // Quick Action Buttons (+ Log Workout & Start Live Session)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToLog,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(50.dp)
                        .testTag("dashboard_log_workout_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PulseCyan,
                        contentColor = DarkBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Manual Workout",
                        tint = DarkBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Log Workout",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showLivePicker = !showLivePicker },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("dashboard_live_tracker_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Live Session",
                        tint = PulseCoral
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Track",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live workout quick picker popup / drawer
        item {
            AnimatedVisibility(visible = showLivePicker) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .border(1.dp, PulseCoral.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "START LIVE WEARABLE TRACKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PulseCoral
                        )
                        Text(
                            text = "Wearable monitors heart rate live and computes real-time calorie burn",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                ExerciseType.RUNNING,
                                ExerciseType.CYCLING,
                                ExerciseType.HIIT,
                                ExerciseType.WEIGHTLIFTING
                            ).forEach { exercise ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkSurfaceElevated)
                                        .clickable {
                                            showLivePicker = false
                                            onStartLiveWorkout(exercise)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = exercise.iconEmoji, fontSize = 20.sp)
                                        Text(
                                            text = exercise.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Goals & Progress Bars (Calorie burn + Exercise duration)
        item {
            val totalBurned = todaySnapshot?.totalCaloriesBurned ?: 380
            val totalDuration = maxOf(todayWorkoutMinutes, todaySnapshot?.activeMinutes ?: 0)

            DailyGoalsProgressCard(
                currentCalories = totalBurned,
                targetCalories = dailyCalorieGoal,
                currentDurationMinutes = totalDuration,
                targetDurationMinutes = dailyDurationGoal,
                onSaveGoals = { cal, dur ->
                    viewModel.updateDailyGoals(cal, dur)
                },
                onLogWorkoutClick = onNavigateToLog
            )
        }

        // Weekly Trends Card Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.dp, PulseCyan.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .clickable { onNavigateToTrends() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("dashboard_view_trends_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PulseCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📊", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Weekly Duration & Calorie Trends",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Interactive 7-day workout analytics & chart",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = "View →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PulseCyan
                    )
                }
            }
        }

        // Biometrics Grid (Steps, SpO2, Stress, Cadence)
        item {
            BiometricsGrid(
                biometrics = biometrics,
                stepGoal = dailyStepGoal,
                isConnected = connectedDevice != null
            )
        }
    }
}
