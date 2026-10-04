package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.FitnessRepository
import com.example.wearable.WearableSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PulseSyncApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        FitnessRepository(
            workoutDao = database.workoutDao(),
            dailySnapshotDao = database.dailySnapshotDao(),
            context = this
        )
    }

    val wearableSyncManager by lazy {
        WearableSyncManager(this, applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.ensureInitialData()
        }
    }
}
