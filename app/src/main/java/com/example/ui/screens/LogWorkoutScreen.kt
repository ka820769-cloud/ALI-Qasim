package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExerciseType
import com.example.data.model.WorkoutIntensity
import com.example.ui.FitnessViewModel
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogWorkoutScreen(
    viewModel: FitnessViewModel,
    onWorkoutSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedExercise by viewModel.selectedExercise.collectAsStateWithLifecycle()
    val customExerciseName by viewModel.customExerciseName.collectAsStateWithLifecycle()
    val durationMinutes by viewModel.durationMinutes.collectAsStateWithLifecycle()
    val selectedIntensity by viewModel.selectedIntensity.collectAsStateWithLifecycle()
    val workoutNotes by viewModel.workoutNotes.collectAsStateWithLifecycle()
    val userWeightKg by viewModel.userWeightKg.collectAsStateWithLifecycle()
    val estimatedCalories by viewModel.estimatedCalories.collectAsStateWithLifecycle()
    val burnRate by viewModel.calorieBurnRate.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var showWeightDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Screen Header
        Text(
            text = "Log Workout",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Select exercise, duration & intensity for scientific ACSM calorie burn calculation",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // 1. Live Real-Time Calorie Calculation Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            DarkSurfaceVariant,
                            DarkSurface
                        )
                    )
                )
                .border(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(PulseCoral, PulseAmber)),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
                .testTag("calorie_estimation_banner")
        ) {
            Column {
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
                                .background(PulseCoral.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Estimated Calories",
                                tint = PulseCoral,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CALCULATED CALORIE BURN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = PulseAmber
                            )
                            Text(
                                text = "Adds directly to your daily total",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // User weight pill (tap to edit)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { showWeightDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = "User Weight",
                                tint = PulseCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userWeightKg.toInt()} kg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Weight",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$estimatedCalories",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "kcal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PulseCoral
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Burn Rate",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "~${String.format("%.1f", burnRate)} kcal/min",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PulseMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ACSM Formula: ${(selectedExercise.baseMet * selectedIntensity.multiplier)} MET • $durationMinutes mins • ${selectedIntensity.title}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Exercise Selection
        Text(
            text = "1. SELECT EXERCISE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExerciseType.entries.forEach { exercise ->
                val isSelected = selectedExercise == exercise
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PulseCyan.copy(alpha = 0.15f) else DarkSurface)
                        .border(
                            1.5.dp,
                            if (isSelected) PulseCyan else DarkSurfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.selectExercise(exercise) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("exercise_chip_${exercise.name.lowercase()}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = exercise.iconEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exercise.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PulseCyan else TextPrimary
                        )
                    }
                }
            }
        }

        // Custom name field if OTHER is selected
        AnimatedVisibility(visible = selectedExercise == ExerciseType.OTHER) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedTextField(
                    value = customExerciseName,
                    onValueChange = { viewModel.setCustomExerciseName(it) },
                    label = { Text("Custom Exercise Name") },
                    placeholder = { Text("e.g. Crossfit, Kickboxing, Kettlebell") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Duration Input
        Text(
            text = "2. DURATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
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
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Duration",
                                tint = PulseCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$durationMinutes minutes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    // Stepper controls (-5m, +5m)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.setDurationMinutes(durationMinutes - 5) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease 5 mins",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setDurationMinutes(durationMinutes + 5) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase 5 mins",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Slider (5 to 180 mins)
                Slider(
                    value = durationMinutes.toFloat(),
                    onValueChange = { viewModel.setDurationMinutes(it.toInt()) },
                    valueRange = 5f..180f,
                    steps = 34, // 5 min increments
                    colors = SliderDefaults.colors(
                        thumbColor = PulseCyan,
                        activeTrackColor = PulseCyan,
                        inactiveTrackColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.testTag("duration_slider")
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(15, 30, 45, 60, 90).forEach { preset ->
                        val isPresetActive = durationMinutes == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPresetActive) PulseCyan else DarkSurfaceElevated)
                                .clickable { viewModel.setDurationMinutes(preset) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${preset}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPresetActive) DarkBackground else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Intensity Selector
        Text(
            text = "3. INTENSITY LEVEL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkoutIntensity.entries.forEach { intensity ->
                val isSelected = selectedIntensity == intensity
                val intensityAccent = when (intensity) {
                    WorkoutIntensity.LOW -> PulseMint
                    WorkoutIntensity.MODERATE -> PulseCyan
                    WorkoutIntensity.HIGH -> PulseAmber
                    WorkoutIntensity.MAXIMUM -> PulseCoral
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) intensityAccent.copy(alpha = 0.12f) else DarkSurface)
                        .border(
                            1.5.dp,
                            if (isSelected) intensityAccent else DarkSurfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.selectIntensity(intensity) }
                        .padding(14.dp)
                        .testTag("intensity_${intensity.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = intensity.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) intensityAccent else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(intensityAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${intensity.multiplier}x burn",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = intensityAccent
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = intensity.description,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = intensity.targetHrZone,
                                fontSize = 11.sp,
                                color = intensityAccent.copy(alpha = 0.9f)
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(intensityAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Notes (Optional)
        Text(
            text = "4. NOTES (OPTIONAL)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = workoutNotes,
            onValueChange = { viewModel.setWorkoutNotes(it) },
            placeholder = { Text("e.g. 5km outdoor pace, felt strong, bench press 80kg PR") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("workout_notes_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PulseCyan,
                unfocusedBorderColor = DarkSurfaceVariant,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            shape = RoundedCornerShape(16.dp),
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Save Workout Button
        Button(
            onClick = {
                viewModel.saveWorkout()
                onWorkoutSaved()
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_workout_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PulseCyan,
                contentColor = DarkBackground
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = DarkBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Save and add calories",
                        tint = DarkBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE WORKOUT (+$estimatedCalories KCAL)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Weight edit dialog
    if (showWeightDialog) {
        var tempWeight by remember { mutableStateOf(userWeightKg.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Your Body Weight", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Calorie burn calculations use the scientific ACSM formula based on body mass.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempWeight,
                        onValueChange = { tempWeight = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PulseCyan,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = tempWeight.toFloatOrNull()
                        if (num != null && num in 30f..250f) {
                            viewModel.updateUserWeight(num)
                        }
                        showWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = DarkBackground)
                ) {
                    Text("Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
