package com.example

import android.app.Application
import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.export.TripExportManager
import com.example.data.feedback.FeedbackManager
import com.example.data.repository.TripRepository
import com.example.data.settings.SettingsRepository
import com.example.data.tracking.TripTrackingEngine
import com.example.domain.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import java.io.File

class TripTimerApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: AppDatabase
        private set
    lateinit var tripRepository: TripRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var exportManager: TripExportManager
        private set
    lateinit var feedbackManager: FeedbackManager
        private set
    lateinit var trackingEngine: TripTrackingEngine
        private set

    var currentSettingsSnapshot: AppSettings = AppSettings()
        private set

    override fun onCreate() {
        super.onCreate()

        // Configure osmdroid for offline tile caching and custom user agent
        runCatching {
            val sharedPrefs = getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
            Configuration.getInstance().load(this, sharedPrefs)
            Configuration.getInstance().userAgentValue = packageName
            val basePath = File(cacheDir, "osmdroid")
            Configuration.getInstance().osmdroidBasePath = basePath
            Configuration.getInstance().osmdroidTileCache = File(basePath, "tiles")
        }

        database = AppDatabase.getInstance(this)
        tripRepository = TripRepository(database.tripDao())
        settingsRepository = SettingsRepository(this)
        exportManager = TripExportManager(this)
        feedbackManager = FeedbackManager(this)

        trackingEngine = TripTrackingEngine(
            context = this,
            tripRepository = tripRepository,
            settingsRepository = settingsRepository,
            feedbackManager = feedbackManager
        )

        applicationScope.launch {
            settingsRepository.settingsFlow.collectLatest { settings ->
                currentSettingsSnapshot = settings
            }
        }
    }
}
