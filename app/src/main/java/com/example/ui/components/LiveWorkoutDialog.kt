package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FitnessViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCoral
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveWorkoutDialog(
    viewModel: FitnessViewModel,
    onDismiss: () -> Unit
) {
    val isLiveActive by viewModel.isLiveWorkoutActive.collectAsStateWithLifecycle()
    val isPaused by viewModel.isLiveWorkoutPaused.collectAsStateWithLifecycle()
    val exercise by viewModel.liveWorkoutExercise.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.liveWorkoutSeconds.collectAsStateWithLifecycle()
    val burnedCalories by viewModel.liveWorkoutCalories.collectAsStateWithLifecycle()
    val biometrics by viewModel.biometrics.collectAsStateWithLifecycle()

    if (!isLiveActive) return

    val formattedTime = String.format(
        "%02d:%02d:%02d",
        elapsedSeconds / 3600,
        (elapsedSeconds % 3600) / 60,
        elapsedSeconds % 60
    )

    AlertDialog(
        onDismissRequest = { /* Force explicit pause or stop */ },
        containerColor = DarkSurface,
        modifier = Modifier.testTag("live_workout_modal"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = exercise.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Live ${exercise.displayName}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isPaused) "Session Paused" else "Wearable Sync Active",
                            fontSize = 11.sp,
                            color = if (isPaused) PulseAmber else PulseCyan
                        )
                    }
                }

                IconButton(onClick = { viewModel.cancelLiveWorkout() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Timer
                Text(
                    text = formattedTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Real-time biometrics banner (Live BPM + Live Calories)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Heart Rate
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Live Heart Rate",
                                tint = PulseCoral,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "HEART RATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${biometrics.heartRate}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseCoral
                        )
                        Text(
                            text = biometrics.hrZone.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = biometrics.hrZone.color
                        )
                    }

                    Box(modifier = Modifier.size(1.dp, 40.dp).background(DarkSurfaceVariant))

                    // Calories Burned
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Live Calories",
                                tint = PulseAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "CALORIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$burnedCalories",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseAmber
                        )
                        Text(
                            text = "kcal burned",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.stopAndSaveLiveWorkout() },
                colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = DarkBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = "Finish")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Finish & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.togglePauseLiveWorkout() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isPaused) "Resume" else "Pause")
            }
        }
    )
}
