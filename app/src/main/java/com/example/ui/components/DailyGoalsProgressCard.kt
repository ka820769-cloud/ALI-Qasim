package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DailyGoalsProgressCard(
    currentCalories: Int,
    targetCalories: Int,
    currentDurationMinutes: Int,
    targetDurationMinutes: Int,
    onSaveGoals: (calorieGoal: Int, durationGoal: Int) -> Unit,
    onLogWorkoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalSettingsDialog by remember { mutableStateOf(false) }

    // Calorie Progress Calculation
    val safeCalorieGoal = targetCalories.coerceAtLeast(1)
    val calRatio = (currentCalories.toFloat() / safeCalorieGoal.toFloat()).coerceIn(0f, 1f)
    val animatedCalProgress by animateFloatAsState(
        targetValue = calRatio,
        animationSpec = tween(700),
        label = "cal_progress"
    )
    val isCalorieGoalAchieved = currentCalories >= safeCalorieGoal
    val calRemaining = (safeCalorieGoal - currentCalories).coerceAtLeast(0)

    // Duration Progress Calculation
    val safeDurationGoal = targetDurationMinutes.coerceAtLeast(1)
    val durRatio = (currentDurationMinutes.toFloat() / safeDurationGoal.toFloat()).coerceIn(0f, 1f)
    val animatedDurProgress by animateFloatAsState(
        targetValue = durRatio,
        animationSpec = tween(700),
        label = "dur_progress"
    )
    val isDurationGoalAchieved = currentDurationMinutes >= safeDurationGoal
    val durRemaining = (safeDurationGoal - currentDurationMinutes).coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("daily_goals_progress_card")
    ) {
        Column {
            // Header Row: Card Title & Adjust Goals Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PulseCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Goals",
                            tint = PulseCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TODAY'S GOALS & PROGRESS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Track calorie burn & exercise time",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Edit Goals Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .clickable { showGoalSettingsDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("edit_goals_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Edit Goals",
                            tint = PulseCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set Goals",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PulseCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Calorie Burn Goal Progress Section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Calories",
                            tint = PulseCoral,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Calorie Burn",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentCalories",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseCoral
                        )
                        Text(
                            text = " / $safeCalorieGoal kcal",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Calorie Progress Bar
                LinearProgressIndicator(
                    progress = { animatedCalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("calorie_goal_progress_bar"),
                    color = if (isCalorieGoalAchieved) PulseMint else PulseCoral,
                    trackColor = DarkSurfaceElevated
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCalorieGoalAchieved) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Achieved",
                                tint = PulseMint,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calorie goal met! (${(calRatio * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PulseMint
                            )
                        }
                    } else {
                        Text(
                            text = "$calRemaining kcal remaining",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "${(calRatio * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCalorieGoalAchieved) PulseMint else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Exercise Duration Goal Progress Section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Duration",
                            tint = PulseCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exercise Duration",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentDurationMinutes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseCyan
                        )
                        Text(
                            text = " / $safeDurationGoal mins",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Duration Progress Bar
                LinearProgressIndicator(
                    progress = { animatedDurProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("duration_goal_progress_bar"),
                    color = if (isDurationGoalAchieved) PulseMint else PulseCyan,
                    trackColor = DarkSurfaceElevated
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDurationGoalAchieved) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Achieved",
                                tint = PulseMint,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Duration goal met! (${(durRatio * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PulseMint
                            )
                        }
                    } else {
                        Text(
                            text = "$durRemaining mins remaining today",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "${(durRatio * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDurationGoalAchieved) PulseMint else TextPrimary
                    )
                }
            }
        }
    }

    // Set Goals Dialog
    if (showGoalSettingsDialog) {
        SetGoalsDialog(
            currentCalorieGoal = targetCalories,
            currentDurationGoal = targetDurationMinutes,
            onDismiss = { showGoalSettingsDialog = false },
            onSave = { calGoal, durGoal ->
                onSaveGoals(calGoal, durGoal)
                showGoalSettingsDialog = false
            }
        )
    }
}

@Composable
fun SetGoalsDialog(
    currentCalorieGoal: Int,
    currentDurationGoal: Int,
    onDismiss: () -> Unit,
    onSave: (calorieGoal: Int, durationGoal: Int) -> Unit
) {
    var tempCalorieGoal by remember { mutableIntStateOf(currentCalorieGoal) }
    var tempDurationGoal by remember { mutableIntStateOf(currentDurationGoal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        modifier = Modifier.testTag("set_goals_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Goals",
                    tint = PulseCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Set Daily Goals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configure your daily targets. Progress bars will track your logged workouts and wearable activity automatically.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. Calorie Goal Selector
                Text(
                    text = "1. DAILY CALORIE BURN GOAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PulseCoral
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$tempCalorieGoal kcal",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PulseCoral
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { tempCalorieGoal = (tempCalorieGoal - 50).coerceAtLeast(100) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "-50 kcal", tint = TextPrimary)
                        }
                        IconButton(
                            onClick = { tempCalorieGoal = (tempCalorieGoal + 50).coerceAtMost(3000) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+50 kcal", tint = TextPrimary)
                        }
                    }
                }

                Slider(
                    value = tempCalorieGoal.toFloat(),
                    onValueChange = { tempCalorieGoal = it.toInt() },
                    valueRange = 100f..2500f,
                    steps = 47,
                    colors = SliderDefaults.colors(
                        thumbColor = PulseCoral,
                        activeTrackColor = PulseCoral,
                        inactiveTrackColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.testTag("dialog_calorie_slider")
                )

                // Quick presets for calories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(400, 500, 650, 800, 1000).forEach { preset ->
                        val isSelected = tempCalorieGoal == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PulseCoral else DarkSurfaceElevated)
                                .clickable { tempCalorieGoal = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${preset}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DarkBackground else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Exercise Duration Goal Selector
                Text(
                    text = "2. DAILY EXERCISE DURATION GOAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PulseCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$tempDurationGoal minutes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PulseCyan
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { tempDurationGoal = (tempDurationGoal - 5).coerceAtLeast(10) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "-5 min", tint = TextPrimary)
                        }
                        IconButton(
                            onClick = { tempDurationGoal = (tempDurationGoal + 5).coerceAtMost(240) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+5 min", tint = TextPrimary)
                        }
                    }
                }

                Slider(
                    value = tempDurationGoal.toFloat(),
                    onValueChange = { tempDurationGoal = it.toInt() },
                    valueRange = 10f..180f,
                    steps = 33,
                    colors = SliderDefaults.colors(
                        thumbColor = PulseCyan,
                        activeTrackColor = PulseCyan,
                        inactiveTrackColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.testTag("dialog_duration_slider")
                )

                // Quick presets for duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(20, 30, 45, 60, 90).forEach { preset ->
                        val isSelected = tempDurationGoal == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PulseCyan else DarkSurfaceElevated)
                                .clickable { tempDurationGoal = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${preset}m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DarkBackground else TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(tempCalorieGoal, tempDurationGoal) },
                colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = DarkBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_daily_goals_button")
            ) {
                Text("Save Goals", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
