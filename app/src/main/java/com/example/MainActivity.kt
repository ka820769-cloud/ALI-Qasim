package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.FitnessViewModel
import com.example.ui.PulseSyncApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val fitnessViewModel: FitnessViewModel by viewModels {
        val app = application as PulseSyncApplication
        FitnessViewModel.Factory(
            repository = app.repository,
            wearableSyncManager = app.wearableSyncManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PulseSyncApp(viewModel = fitnessViewModel)
            }
        }
    }
}
