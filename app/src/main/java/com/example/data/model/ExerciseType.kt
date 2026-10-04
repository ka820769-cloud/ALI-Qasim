package com.example.data.model

enum class ExerciseType(
    val displayName: String,
    val iconEmoji: String,
    val baseMet: Float,
    val description: String
) {
    RUNNING(
        displayName = "Running",
        iconEmoji = "🏃",
        baseMet = 9.8f,
        description = "Outdoor run or treadmill pace"
    ),
    CYCLING(
        displayName = "Cycling",
        iconEmoji = "🚴",
        baseMet = 6.8f,
        description = "Road bike, spin class, or commute"
    ),
    WEIGHTLIFTING(
        displayName = "Weightlifting",
        iconEmoji = "🏋️",
        baseMet = 5.0f,
        description = "Resistance, dumbbell, barbell training"
    ),
    YOGA(
        displayName = "Yoga",
        iconEmoji = "🧘",
        baseMet = 3.0f,
        description = "Vinyasa, Hatha, or stretching flow"
    ),
    SWIMMING(
        displayName = "Swimming",
        iconEmoji = "🏊",
        baseMet = 7.0f,
        description = "Freestyle, breaststroke, laps"
    ),
    HIIT(
        displayName = "HIIT",
        iconEmoji = "⚡",
        baseMet = 8.5f,
        description = "High-intensity interval circuits"
    ),
    WALKING(
        displayName = "Walking",
        iconEmoji = "🚶",
        baseMet = 3.8f,
        description = "Brisk walk, incline, or outdoor hike"
    ),
    ROWING(
        displayName = "Rowing",
        iconEmoji = "🚣",
        baseMet = 7.0f,
        description = "Indoor rower or water kayak"
    ),
    PILATES(
        displayName = "Pilates",
        iconEmoji = "🤸",
        baseMet = 3.5f,
        description = "Core stability & mat reformer"
    ),
    BOXING(
        displayName = "Boxing",
        iconEmoji = "🥊",
        baseMet = 9.0f,
        description = "Sparring, bag work, and footwork"
    ),
    BASKETBALL(
        displayName = "Basketball",
        iconEmoji = "🏀",
        baseMet = 6.5f,
        description = "Full court or half court scrimmage"
    ),
    OTHER(
        displayName = "Other Workout",
        iconEmoji = "⏱️",
        baseMet = 4.5f,
        description = "Custom athletic session"
    );

    companion object {
        fun fromName(name: String): ExerciseType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: OTHER
        }
    }
}
