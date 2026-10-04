package com.example.data.model

enum class WorkoutIntensity(
    val title: String,
    val description: String,
    val multiplier: Float,
    val targetHrZone: String
) {
    LOW(
        title = "Low / Light",
        description = "Conversational pace, light sweating",
        multiplier = 0.8f,
        targetHrZone = "50-60% Max HR (Warm-up / Recovery)"
    ),
    MODERATE(
        title = "Moderate",
        description = "Comfortable effort, steady breathing",
        multiplier = 1.0f,
        targetHrZone = "60-70% Max HR (Fat Burn)"
    ),
    HIGH(
        title = "High / Vigorous",
        description = "Heavy breathing, difficult to talk",
        multiplier = 1.25f,
        targetHrZone = "70-85% Max HR (Cardio / Aerobic)"
    ),
    MAXIMUM(
        title = "Maximum / Peak",
        description = "All-out exertion, intervals, sprint bursts",
        multiplier = 1.5f,
        targetHrZone = "85-100% Max HR (Anaerobic / Peak)"
    );

    companion object {
        fun fromName(name: String): WorkoutIntensity {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.title.equals(name, ignoreCase = true) }
                ?: MODERATE
        }
    }
}
