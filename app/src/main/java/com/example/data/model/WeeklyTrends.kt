package com.example.data.model

import com.example.data.local.WorkoutEntity

data class DayTrendItem(
    val dayLabel: String,       // "Mon", "Tue", etc.
    val dateLabel: String,      // "Oct 2"
    val dateString: String,     // "yyyy-MM-dd"
    val totalCalories: Int,
    val totalDurationMinutes: Int,
    val workouts: List<WorkoutEntity>,
    val isToday: Boolean
)

data class ExerciseDistributionItem(
    val exerciseType: ExerciseType,
    val totalCalories: Int,
    val totalDurationMinutes: Int,
    val percentageOfTime: Float
)

data class WeeklyAnalyticsState(
    val dayTrends: List<DayTrendItem>,
    val totalWeeklyCalories: Int,
    val totalWeeklyDurationMinutes: Int,
    val avgDailyCalories: Int,
    val avgDailyDurationMinutes: Int,
    val peakDayLabel: String,
    val peakCalories: Int,
    val distributions: List<ExerciseDistributionItem>
)
