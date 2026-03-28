package com.example.kalorientracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.kalorientracker.ui.tracker.TrackerScreen
import com.example.kalorientracker.ui.tracker.TrackerViewModel
import com.example.kalorientracker.ui.theme.KalorientrackerTheme

class MainActivity : ComponentActivity() {
    private val trackerViewModel: TrackerViewModel by viewModels {
        TrackerViewModel.factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KalorientrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TrackerScreen(
                        viewModel = trackerViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
