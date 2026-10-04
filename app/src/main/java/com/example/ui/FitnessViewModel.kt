package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailySnapshotEntity
import com.example.data.local.WorkoutEntity
import com.example.data.model.CalorieCalculator
import com.example.data.model.ExerciseType
import com.example.data.model.DayTrendItem
import com.example.data.model.ExerciseDistributionItem
import com.example.data.model.HealthBiometrics
import com.example.data.model.WearableDevice
import com.example.data.model.WeeklyAnalyticsState
import com.example.data.model.WorkoutIntensity
import com.example.data.repository.FitnessRepository
import com.example.wearable.SimulationEffortMode
import com.example.wearable.WearableSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FitnessViewModel(
    private val repository: FitnessRepository,
    private val wearableSyncManager: WearableSyncManager
) : ViewModel() {

    // Repository flows
    val allWorkouts: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySnapshot: StateFlow<DailySnapshotEntity?> = repository.getTodaySnapshot()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Weekly Trends Analytics State Flow
    val weeklyAnalytics: StateFlow<WeeklyAnalyticsState> = allWorkouts.map { workouts ->
        val dayTrends = ArrayList<DayTrendItem>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = keyFormat.format(Date())

        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance()
            targetCal.add(Calendar.DAY_OF_YEAR, -i)
            targetCal.set(Calendar.HOUR_OF_DAY, 0)
            targetCal.set(Calendar.MINUTE, 0)
            targetCal.set(Calendar.SECOND, 0)
            targetCal.set(Calendar.MILLISECOND, 0)
            val dayStart = targetCal.timeInMillis
            val dayEnd = dayStart + (24L * 60L * 60L * 1000L) - 1L

            val dayWorkouts = workouts.filter { it.timestamp in dayStart..dayEnd }
            val dayKey = keyFormat.format(Date(dayStart))
            val totalCal = dayWorkouts.sumOf { it.caloriesBurned }
            val totalDur = dayWorkouts.sumOf { it.durationMinutes }

            dayTrends.add(
                DayTrendItem(
                    dayLabel = dayFormat.format(Date(dayStart)),
                    dateLabel = dateFormat.format(Date(dayStart)),
                    dateString = dayKey,
                    totalCalories = totalCal,
                    totalDurationMinutes = totalDur,
                    workouts = dayWorkouts,
                    isToday = dayKey == todayKey
                )
            )
        }

        val totalWeeklyCal = dayTrends.sumOf { it.totalCalories }
        val totalWeeklyDur = dayTrends.sumOf { it.totalDurationMinutes }
        val avgDailyCal = if (dayTrends.isNotEmpty()) totalWeeklyCal / 7 else 0
        val avgDailyDur = if (dayTrends.isNotEmpty()) totalWeeklyDur / 7 else 0

        val peakDay = dayTrends.maxByOrNull { it.totalCalories }
        val peakLabel = peakDay?.dayLabel ?: "None"
        val peakCal = peakDay?.totalCalories ?: 0

        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24L * 60L * 60L * 1000L)
        val recentWorkouts = workouts.filter { it.timestamp >= sevenDaysAgo }
        val exerciseGroups = recentWorkouts.groupBy { ExerciseType.fromName(it.exerciseType) }

        val distributions = exerciseGroups.map { (type, list) ->
            val cal = list.sumOf { it.caloriesBurned }
            val dur = list.sumOf { it.durationMinutes }
            val pct = if (totalWeeklyDur > 0) (dur.toFloat() / totalWeeklyDur.toFloat()) else 0f
            ExerciseDistributionItem(
                exerciseType = type,
                totalCalories = cal,
                totalDurationMinutes = dur,
                percentageOfTime = pct
            )
        }.sortedByDescending { it.totalDurationMinutes }

        WeeklyAnalyticsState(
            dayTrends = dayTrends,
            totalWeeklyCalories = totalWeeklyCal,
            totalWeeklyDurationMinutes = totalWeeklyDur,
            avgDailyCalories = avgDailyCal,
            avgDailyDurationMinutes = avgDailyDur,
            peakDayLabel = peakLabel,
            peakCalories = peakCal,
            distributions = distributions
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeeklyAnalyticsState(
            dayTrends = emptyList(),
            totalWeeklyCalories = 0,
            totalWeeklyDurationMinutes = 0,
            avgDailyCalories = 0,
            avgDailyDurationMinutes = 0,
            peakDayLabel = "-",
            peakCalories = 0,
            distributions = emptyList()
        )
    )

    // Wearable flows
    val connectedDevice: StateFlow<WearableDevice?> = wearableSyncManager.connectedDevice
    val availableDevices: StateFlow<List<WearableDevice>> = wearableSyncManager.availableDevices
    val biometrics: StateFlow<HealthBiometrics> = wearableSyncManager.biometrics
    val ecgWave: StateFlow<List<Float>> = wearableSyncManager.ecgWave
    val simulationMode: StateFlow<SimulationEffortMode> = wearableSyncManager.simulationMode
    val isScanningBle: StateFlow<Boolean> = wearableSyncManager.isScanning

    // User preferences
    private val _userWeightKg = MutableStateFlow(repository.getUserWeightKg())
    val userWeightKg: StateFlow<Float> = _userWeightKg.asStateFlow()

    private val _dailyCalorieGoal = MutableStateFlow(repository.getDailyCalorieGoal())
    val dailyCalorieGoal: StateFlow<Int> = _dailyCalorieGoal.asStateFlow()

    private val _dailyDurationGoal = MutableStateFlow(repository.getDailyDurationGoal())
    val dailyDurationGoal: StateFlow<Int> = _dailyDurationGoal.asStateFlow()

    private val _dailyStepGoal = MutableStateFlow(repository.getDailyStepGoal())
    val dailyStepGoal: StateFlow<Int> = _dailyStepGoal.asStateFlow()

    // Today's total logged workout duration in minutes
    val todayWorkoutMinutes: StateFlow<Int> = allWorkouts.map { workouts ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        workouts.filter { it.timestamp >= startOfDay }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Workout Logging Form State
    private val _selectedExercise = MutableStateFlow(ExerciseType.RUNNING)
    val selectedExercise: StateFlow<ExerciseType> = _selectedExercise.asStateFlow()

    private val _customExerciseName = MutableStateFlow("")
    val customExerciseName: StateFlow<String> = _customExerciseName.asStateFlow()

    private val _durationMinutes = MutableStateFlow(30)
    val durationMinutes: StateFlow<Int> = _durationMinutes.asStateFlow()

    private val _selectedIntensity = MutableStateFlow(WorkoutIntensity.MODERATE)
    val selectedIntensity: StateFlow<WorkoutIntensity> = _selectedIntensity.asStateFlow()

    private val _workoutNotes = MutableStateFlow("")
    val workoutNotes: StateFlow<String> = _workoutNotes.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Real-time calculated calories burned for current logging form values
    val estimatedCalories: StateFlow<Int> = combine(
        _selectedExercise,
        _durationMinutes,
        _selectedIntensity,
        _userWeightKg
    ) { exercise, duration, intensity, weight ->
        CalorieCalculator.calculateCalories(
            exerciseType = exercise,
            durationMinutes = duration,
            intensity = intensity,
            userWeightKg = weight
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 206)

    val calorieBurnRate: StateFlow<Float> = combine(
        _selectedExercise,
        _selectedIntensity,
        _userWeightKg
    ) { exercise, intensity, weight ->
        CalorieCalculator.calculateBurnRate(exercise, intensity, weight)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6.86f)

    // History filter
    private val _filterExercise = MutableStateFlow<ExerciseType?>(null)
    val filterExercise: StateFlow<ExerciseType?> = _filterExercise.asStateFlow()

    // Live Workout Companion Mode (Live tracking with real-time wearable heart rate)
    private val _isLiveWorkoutActive = MutableStateFlow(false)
    val isLiveWorkoutActive: StateFlow<Boolean> = _isLiveWorkoutActive.asStateFlow()

    private val _isLiveWorkoutPaused = MutableStateFlow(false)
    val isLiveWorkoutPaused: StateFlow<Boolean> = _isLiveWorkoutPaused.asStateFlow()

    private val _liveWorkoutExercise = MutableStateFlow(ExerciseType.RUNNING)
    val liveWorkoutExercise: StateFlow<ExerciseType> = _liveWorkoutExercise.asStateFlow()

    private val _liveWorkoutSeconds = MutableStateFlow(0)
    val liveWorkoutSeconds: StateFlow<Int> = _liveWorkoutSeconds.asStateFlow()

    private val _liveWorkoutCalories = MutableStateFlow(0)
    val liveWorkoutCalories: StateFlow<Int> = _liveWorkoutCalories.asStateFlow()

    private var liveWorkoutJob: Job? = null

    // Form handlers
    fun selectExercise(exercise: ExerciseType) {
        _selectedExercise.value = exercise
        if (exercise != ExerciseType.OTHER) {
            _customExerciseName.value = ""
        }
    }

    fun setCustomExerciseName(name: String) {
        _customExerciseName.value = name
    }

    fun setDurationMinutes(minutes: Int) {
        _durationMinutes.value = minutes.coerceIn(1, 480)
    }

    fun selectIntensity(intensity: WorkoutIntensity) {
        _selectedIntensity.value = intensity
    }

    fun setWorkoutNotes(notes: String) {
        _workoutNotes.value = notes
    }

    fun saveWorkout() {
        val exercise = _selectedExercise.value
        val duration = _durationMinutes.value
        val intensity = _selectedIntensity.value
        val weight = _userWeightKg.value
        val notes = _workoutNotes.value.trim()
        val customName = _customExerciseName.value.trim()

        val finalName = if (exercise == ExerciseType.OTHER && customName.isNotEmpty()) {
            customName
        } else {
            exercise.displayName
        }

        val calculatedCalories = CalorieCalculator.calculateCalories(
            exerciseType = exercise,
            durationMinutes = duration,
            intensity = intensity,
            userWeightKg = weight
        )

        val currentHr = biometrics.value.heartRate

        viewModelScope.launch {
            _isSaving.value = true
            val workout = WorkoutEntity(
                exerciseType = exercise.name,
                exerciseName = finalName,
                durationMinutes = duration,
                intensity = intensity.name,
                caloriesBurned = calculatedCalories,
                notes = notes,
                timestamp = System.currentTimeMillis(),
                avgHeartRate = if (connectedDevice.value != null) currentHr else null,
                maxHeartRate = if (connectedDevice.value != null) (currentHr + 15).coerceAtMost(200) else null,
                userWeightKgAtLog = weight
            )
            repository.logWorkout(workout)

            // Reset notes and keep exercise/duration ready for next time
            _workoutNotes.value = ""
            _isSaving.value = false
            _toastEvent.emit("Logged $finalName: +$calculatedCalories kcal added to daily total!")
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
            _toastEvent.emit("Deleted ${workout.exerciseName} workout (-${workout.caloriesBurned} kcal)")
        }
    }

    fun setFilterExercise(type: ExerciseType?) {
        _filterExercise.value = type
    }

    fun updateUserWeight(weightKg: Float) {
        _userWeightKg.value = weightKg
        repository.setUserWeightKg(weightKg)
    }

    fun updateCalorieGoal(goal: Int) {
        val clamped = goal.coerceIn(100, 5000)
        _dailyCalorieGoal.value = clamped
        repository.setDailyCalorieGoal(clamped)
        viewModelScope.launch {
            _toastEvent.emit("Daily calorie burn goal saved: $clamped kcal")
        }
    }

    fun updateDurationGoal(minutes: Int) {
        val clamped = minutes.coerceIn(10, 360)
        _dailyDurationGoal.value = clamped
        repository.setDailyDurationGoal(clamped)
        viewModelScope.launch {
            _toastEvent.emit("Daily exercise duration goal saved: $clamped mins")
        }
    }

    fun updateDailyGoals(calorieGoal: Int, durationGoal: Int) {
        val clampedCal = calorieGoal.coerceIn(100, 5000)
        val clampedDur = durationGoal.coerceIn(10, 360)
        _dailyCalorieGoal.value = clampedCal
        _dailyDurationGoal.value = clampedDur
        repository.setDailyCalorieGoal(clampedCal)
        repository.setDailyDurationGoal(clampedDur)
        viewModelScope.launch {
            _toastEvent.emit("Saved daily goals: $clampedCal kcal & $clampedDur mins!")
        }
    }

    fun updateStepGoal(goal: Int) {
        _dailyStepGoal.value = goal
        repository.setDailyStepGoal(goal)
    }

    // Wearable device actions
    fun connectWearable(device: WearableDevice) {
        wearableSyncManager.connectDevice(device)
        viewModelScope.launch {
            _toastEvent.emit("Connected to ${device.name}")
        }
    }

    fun disconnectWearable() {
        val name = connectedDevice.value?.name ?: "Device"
        wearableSyncManager.disconnectDevice()
        viewModelScope.launch {
            _toastEvent.emit("Disconnected from $name")
        }
    }

    fun triggerWearableSync() {
        wearableSyncManager.triggerManualSync()
        viewModelScope.launch {
            _toastEvent.emit("Syncing health biometrics...")
        }
    }

    fun setSimulationEffort(mode: SimulationEffortMode) {
        wearableSyncManager.setSimulationEffort(mode)
    }

    fun startBleScan() {
        wearableSyncManager.startBleScan()
        viewModelScope.launch {
            _toastEvent.emit("Scanning for Bluetooth LE health monitors...")
        }
    }

    // Live Workout Tracking
    fun startLiveWorkout(exercise: ExerciseType) {
        _liveWorkoutExercise.value = exercise
        _liveWorkoutSeconds.value = 0
        _liveWorkoutCalories.value = 0
        _isLiveWorkoutActive.value = true
        _isLiveWorkoutPaused.value = false

        // Automatically adapt simulated wearable to moderate/aerobic for realistic tracking
        wearableSyncManager.setSimulationEffort(SimulationEffortMode.MODERATE)

        liveWorkoutJob?.cancel()
        liveWorkoutJob = viewModelScope.launch {
            while (isActive && _isLiveWorkoutActive.value) {
                delay(1000)
                if (!_isLiveWorkoutPaused.value) {
                    _liveWorkoutSeconds.value += 1
                    val mins = _liveWorkoutSeconds.value / 60f
                    val burnRate = CalorieCalculator.calculateBurnRate(
                        _liveWorkoutExercise.value,
                        WorkoutIntensity.MODERATE,
                        _userWeightKg.value
                    )
                    _liveWorkoutCalories.value = (mins * burnRate).toInt().coerceAtLeast(0)
                }
            }
        }
    }

    fun togglePauseLiveWorkout() {
        _isLiveWorkoutPaused.value = !_isLiveWorkoutPaused.value
    }

    fun stopAndSaveLiveWorkout() {
        val exercise = _liveWorkoutExercise.value
        val elapsedSec = _liveWorkoutSeconds.value
        val durationMins = (elapsedSec / 60).coerceAtLeast(1)
        val calories = _liveWorkoutCalories.value.coerceAtLeast(1)
        val avgHr = biometrics.value.heartRate

        liveWorkoutJob?.cancel()
        _isLiveWorkoutActive.value = false
        _isLiveWorkoutPaused.value = false
        wearableSyncManager.setSimulationEffort(SimulationEffortMode.REST)

        viewModelScope.launch {
            val workout = WorkoutEntity(
                exerciseType = exercise.name,
                exerciseName = "Live ${exercise.displayName}",
                durationMinutes = durationMins,
                intensity = WorkoutIntensity.MODERATE.name,
                caloriesBurned = calories,
                notes = "Recorded live via wearable ($elapsedSec sec, avg $avgHr BPM)",
                timestamp = System.currentTimeMillis(),
                avgHeartRate = avgHr,
                maxHeartRate = biometrics.value.maxHeartRateToday,
                userWeightKgAtLog = _userWeightKg.value
            )
            repository.logWorkout(workout)
            _toastEvent.emit("Saved Live ${exercise.displayName}: +$calories kcal added!")
        }
    }

    fun cancelLiveWorkout() {
        liveWorkoutJob?.cancel()
        _isLiveWorkoutActive.value = false
        _isLiveWorkoutPaused.value = false
        wearableSyncManager.setSimulationEffort(SimulationEffortMode.REST)
    }

    class Factory(
        private val repository: FitnessRepository,
        private val wearableSyncManager: WearableSyncManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FitnessViewModel(repository, wearableSyncManager) as T
        }
    }
}
