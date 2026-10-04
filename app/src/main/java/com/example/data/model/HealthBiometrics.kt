package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ZoneCardio
import com.example.ui.theme.ZoneFatBurn
import com.example.ui.theme.ZonePeak
import com.example.ui.theme.ZoneRest

enum class HeartRateZone(
    val label: String,
    val rangeDesc: String,
    val color: Color
) {
    REST("Resting", "< 100 BPM", ZoneRest),
    FAT_BURN("Fat Burn", "100 - 129 BPM", ZoneFatBurn),
    CARDIO("Cardio", "130 - 159 BPM", ZoneCardio),
    PEAK("Peak Effort", "160+ BPM", ZonePeak);

    companion object {
        fun fromBpm(bpm: Int): HeartRateZone {
            return when {
                bpm < 100 -> REST
                bpm < 130 -> FAT_BURN
                bpm < 160 -> CARDIO
                else -> PEAK
            }
        }
    }
}

data class HealthBiometrics(
    val heartRate: Int = 72,
    val restingHeartRate: Int = 62,
    val minHeartRateToday: Int = 58,
    val maxHeartRateToday: Int = 145,
    val hrZone: HeartRateZone = HeartRateZone.REST,
    val spO2Percentage: Int = 98,
    val stressLevel: Int = 28, // 0 - 100 scale
    val activeStepsToday: Int = 6420,
    val stepGoal: Int = 10000,
    val activeCaloriesBurned: Int = 410,
    val dailyCalorieGoal: Int = 650,
    val activeMinutesToday: Int = 45,
    val cadenceSpm: Int = 0, // Steps per min
    val hrvMs: Int = 65,     // Heart Rate Variability ms
    val skinTempCelsius: Float = 36.5f,
    val isLiveSyncing: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
