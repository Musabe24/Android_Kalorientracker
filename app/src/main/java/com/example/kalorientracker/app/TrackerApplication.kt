package com.example.kalorientracker.app

import android.app.Application

class TrackerApplication : Application() {
    val appContainer: TrackerAppContainer by lazy {
        DefaultTrackerAppContainer(this)
    }
}
