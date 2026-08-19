package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.TripTimerApplication
import com.example.data.export.TripExportManager
import com.example.data.repository.TripRepository
import com.example.data.settings.SettingsRepository
import com.example.domain.model.AppSettings
import com.example.domain.model.DistanceUnit
import com.example.domain.model.ThemeMode
import com.example.domain.model.TimeFormat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val tripRepository: TripRepository,
    private val exportManager: TripExportManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun updateMovementThreshold(thresholdKmh: Float) {
        viewModelScope.launch {
            settingsRepository.updateMovementThreshold(thresholdKmh)
        }
    }

    fun updateIdleDetectionDelay(delaySec: Int) {
        viewModelScope.launch {
            settingsRepository.updateIdleDetectionDelay(delaySec)
        }
    }

    fun updateGpsInterval(intervalSec: Int) {
        viewModelScope.launch {
            settingsRepository.updateGpsInterval(intervalSec)
        }
    }

    fun updateDistanceUnit(unit: DistanceUnit) {
        viewModelScope.launch {
            settingsRepository.updateDistanceUnit(unit)
        }
    }

    fun updateTimeFormat(format: TimeFormat) {
        viewModelScope.launch {
            settingsRepository.updateTimeFormat(format)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun updateKeepScreenAwake(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateKeepScreenAwake(enabled)
        }
    }

    fun updatePowerSavingDimScreen(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updatePowerSavingDimScreen(enabled)
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVibrationEnabled(enabled)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSoundEnabled(enabled)
        }
    }

    fun exportAllCsv() {
        viewModelScope.launch {
            val all = tripRepository.allTrips.stateIn(viewModelScope).value
            if (all.isNotEmpty()) {
                val csvFile = exportManager.exportTripsToCsv(all)
                val shareIntent = exportManager.createShareIntent(csvFile, "text/csv").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export All Trips (CSV)").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    fun exportAllJson() {
        viewModelScope.launch {
            val all = tripRepository.allTrips.stateIn(viewModelScope).value
            if (all.isNotEmpty()) {
                val tripsWithPoints = all.map { trip ->
                    trip to tripRepository.getPointsForTripList(trip.id)
                }
                val jsonFile = exportManager.exportTripsToJson(tripsWithPoints)
                val shareIntent = exportManager.createShareIntent(jsonFile, "application/json").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export All Trips & GPS Points (JSON)").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext as TripTimerApplication
                return SettingsViewModel(
                    context = app,
                    settingsRepository = app.settingsRepository,
                    tripRepository = app.tripRepository,
                    exportManager = app.exportManager
                ) as T
            }
        }
    }
}
