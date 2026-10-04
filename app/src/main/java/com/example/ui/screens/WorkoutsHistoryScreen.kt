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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WorkoutEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutsHistoryScreen(
    viewModel: FitnessViewModel,
    onNavigateToLog: () -> Unit,
    onNavigateToTrends: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    val filterExercise by viewModel.filterExercise.collectAsStateWithLifecycle()

    var workoutToDelete by remember { mutableStateOf<WorkoutEntity?>(null) }

    val filteredWorkouts = remember(workouts, filterExercise) {
        if (filterExercise == null) workouts
        else workouts.filter { it.exerciseType == filterExercise?.name }
    }

    val totalCalories = remember(workouts) { workouts.sumOf { it.caloriesBurned } }
    val totalMinutes = remember(workouts) { workouts.sumOf { it.durationMinutes } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header Row with Title and + Log Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Workout History",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "${workouts.size} sessions recorded",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onNavigateToLog,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseCyan,
                    contentColor = DarkBackground
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("log_new_workout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Log Workout",
                    tint = DarkBackground,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Log Workout",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Statistics Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TOTAL SESSIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = "${workouts.size}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                }
                Box(modifier = Modifier.size(1.dp, 32.dp).background(DarkSurfaceVariant))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "CALORIES BURNED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = "$totalCalories kcal", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PulseCoral)
                }
                Box(modifier = Modifier.size(1.dp, 32.dp).background(DarkSurfaceVariant))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "ACTIVE TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = "${totalMinutes}m", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PulseCyan)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .clickable { onNavigateToTrends() }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📊 View Weekly Duration & Calorie Trends →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PulseCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips Row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAll = filterExercise == null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAll) PulseCyan else DarkSurface)
                    .border(1.dp, if (isAll) PulseCyan else DarkSurfaceVariant, RoundedCornerShape(12.dp))
                    .clickable { viewModel.setFilterExercise(null) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAll) DarkBackground else TextPrimary
                )
            }

            ExerciseType.entries.take(7).forEach { exercise ->
                val isSelected = filterExercise == exercise
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PulseCyan else DarkSurface)
                        .border(1.dp, if (isSelected) PulseCyan else DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setFilterExercise(if (isSelected) null else exercise) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = exercise.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = exercise.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DarkBackground else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Workouts List
        if (filteredWorkouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "No workouts",
                            tint = TextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Workouts Logged Yet",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tap '+ Log Workout' to calculate and log your exercises",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToLog,
                        colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = DarkBackground),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Log First Workout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredWorkouts, key = { it.id }) { workout ->
                    WorkoutListItem(
                        workout = workout,
                        onDeleteClick = { workoutToDelete = workout }
                    )
                }
            }
        }
    }

    // Confirm Delete Dialog
    if (workoutToDelete != null) {
        val target = workoutToDelete!!
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Delete Workout?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This will remove '${target.exerciseName}' and deduct ${target.caloriesBurned} kcal from today's daily total.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorkout(target)
                        workoutToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PulseCoral,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun WorkoutListItem(
    workout: WorkoutEntity,
    onDeleteClick: () -> Unit
) {
    val exerciseType = remember(workout.exerciseType) {
        ExerciseType.fromName(workout.exerciseType)
    }
    val intensity = remember(workout.intensity) {
        WorkoutIntensity.fromName(workout.intensity)
    }

    val formattedDate = remember(workout.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(workout.timestamp))
    }

    val intensityColor = when (intensity) {
        WorkoutIntensity.LOW -> PulseMint
        WorkoutIntensity.MODERATE -> PulseCyan
        WorkoutIntensity.HIGH -> PulseAmber
        WorkoutIntensity.MAXIMUM -> PulseCoral
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("workout_item_${workout.id}")
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = exerciseType.iconEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = workout.exerciseName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedDate,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${workout.durationMinutes} min",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PulseCyan
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+${workout.caloriesBurned} kcal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseCoral
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(intensityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = intensity.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = intensityColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Workout",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Notes preview if present
            if (workout.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Notes",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = workout.notes,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Wearable heart rate info if logged with wearable
            if (workout.avgHeartRate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Avg HR",
                        tint = PulseCoral,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Avg ${workout.avgHeartRate} BPM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
