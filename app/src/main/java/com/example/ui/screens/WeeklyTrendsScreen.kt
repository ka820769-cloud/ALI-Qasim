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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WorkoutEntity
import com.example.data.model.ExerciseType
import com.example.data.model.WorkoutIntensity
import com.example.ui.FitnessViewModel
import com.example.ui.components.TrendMetricMode
import com.example.ui.components.WeeklyTrendsChart
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCoral
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseMint
import com.example.ui.theme.PulsePurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyTrendsScreen(
    viewModel: FitnessViewModel,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weeklyAnalytics by viewModel.weeklyAnalytics.collectAsStateWithLifecycle()
    val dailyCalorieGoal by viewModel.dailyCalorieGoal.collectAsStateWithLifecycle()
    val dailyDurationGoal by viewModel.dailyDurationGoal.collectAsStateWithLifecycle()
    val dayTrends = weeklyAnalytics.dayTrends

    var selectedDayIndex by remember { mutableIntStateOf(6) } // default to Today (last item)
    var metricMode by remember { mutableStateOf(TrendMetricMode.COMBINED) }

    // Keep selectedDayIndex bounded
    val safeDayIndex = if (dayTrends.isNotEmpty()) selectedDayIndex.coerceIn(0, dayTrends.size - 1) else 0
    val selectedDay = if (dayTrends.isNotEmpty()) dayTrends[safeDayIndex] else null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Screen Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Trends",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Duration & Calorie analytics from logged workouts",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PulseCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Trends",
                        tint = PulseCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Metric Mode Switcher Chips (Recharts view selector)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrendMetricMode.entries.forEach { mode ->
                    val isSelected = metricMode == mode
                    val chipColor = when (mode) {
                        TrendMetricMode.COMBINED -> PulsePurple
                        TrendMetricMode.CALORIES -> PulseCoral
                        TrendMetricMode.DURATION -> PulseCyan
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) chipColor.copy(alpha = 0.2f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) chipColor else DarkSurfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { metricMode = mode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = mode.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) chipColor else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Recharts-inspired Interactive Canvas Chart
        item {
            WeeklyTrendsChart(
                dayTrends = dayTrends,
                selectedDayIndex = safeDayIndex,
                onSelectDay = { selectedDayIndex = it },
                metricMode = metricMode
            )
        }

        // Selected Day Details Banner (Interactive Tooltip / Callout)
        item {
            if (selectedDay != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                        .testTag("selected_day_tooltip_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = PulseCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${selectedDay.dayLabel}, ${selectedDay.dateLabel}" + if (selectedDay.isToday) " (Today)" else "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${selectedDay.workouts.size} manual workouts logged",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Calories",
                                        tint = PulseCoral,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${selectedDay.totalCalories} kcal",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PulseCoral
                                    )
                                }
                                Text(text = "Burned", fontSize = 10.sp, color = TextSecondary)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Duration",
                                        tint = PulseCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${selectedDay.totalDurationMinutes} min",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PulseCyan
                                    )
                                }
                                Text(text = "Duration", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // 4 Summary Metrics (Weekly KPI Cards)
        item {
            Text(
                text = "WEEKLY SUMMARY STATISTICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrendKpiCard(
                    title = "TOTAL BURN",
                    value = "${weeklyAnalytics.totalWeeklyCalories}",
                    unit = "kcal",
                    subtitle = "Avg ${weeklyAnalytics.avgDailyCalories} kcal/day",
                    accentColor = PulseCoral,
                    modifier = Modifier.weight(1f)
                )

                TrendKpiCard(
                    title = "TOTAL DURATION",
                    value = "${weeklyAnalytics.totalWeeklyDurationMinutes}",
                    unit = "mins",
                    subtitle = "Avg ${weeklyAnalytics.avgDailyDurationMinutes} mins/day",
                    accentColor = PulseCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrendKpiCard(
                    title = "PEAK WORKOUT DAY",
                    value = weeklyAnalytics.peakDayLabel,
                    unit = "",
                    subtitle = "${weeklyAnalytics.peakCalories} kcal highest burn",
                    accentColor = PulseAmber,
                    modifier = Modifier.weight(1f)
                )

                TrendKpiCard(
                    title = "DAILY TARGET",
                    value = "$dailyCalorieGoal",
                    unit = "kcal",
                    subtitle = "$dailyDurationGoal mins exercise target",
                    accentColor = PulseMint,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Exercise Distribution Breakdown (Proportional Chart)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "EXERCISE BREAKDOWN (PAST 7 DAYS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (weeklyAnalytics.distributions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No workouts logged in the last 7 days",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        weeklyAnalytics.distributions.forEach { dist ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = dist.exerciseType.iconEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dist.exerciseType.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${dist.totalCalories} kcal",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PulseCoral
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${dist.totalDurationMinutes}m (${(dist.percentageOfTime * 100).toInt()}%)",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { dist.percentageOfTime.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = PulseCyan,
                                    trackColor = DarkSurfaceElevated
                                )
                            }
                        }
                    }
                }
            }
        }

        // Workouts on Selected Day
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKOUTS ON ${selectedDay?.dayLabel?.uppercase() ?: "SELECTED DAY"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )

                if (selectedDay != null && selectedDay.workouts.isEmpty()) {
                    Button(
                        onClick = onNavigateToLog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseCyan,
                            contentColor = DarkBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Log Workout",
                            tint = DarkBackground,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Workout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (selectedDay == null || selectedDay.workouts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No workouts logged for this day",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap '+ Log Workout' to add manual exercises and update the chart",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            items(selectedDay.workouts, key = { it.id }) { workout ->
                DayWorkoutItem(workout = workout)
            }
        }
    }
}

@Composable
private fun TrendKpiCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DayWorkoutItem(workout: WorkoutEntity) {
    val exerciseType = remember(workout.exerciseType) {
        ExerciseType.fromName(workout.exerciseType)
    }
    val intensity = remember(workout.intensity) {
        WorkoutIntensity.fromName(workout.intensity)
    }
    val formattedTime = remember(workout.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(workout.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = exerciseType.iconEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = workout.exerciseName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$formattedTime • ${workout.durationMinutes} min • ${intensity.title}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (workout.notes.isNotBlank()) {
                        Text(
                            text = "\"${workout.notes}\"",
                            fontSize = 11.sp,
                            color = PulseCyan,
                            maxLines = 1
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${workout.caloriesBurned} kcal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PulseCoral
                )
                Text(
                    text = "${(workout.caloriesBurned.toFloat() / workout.durationMinutes.toFloat()).toInt()} kcal/min",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
