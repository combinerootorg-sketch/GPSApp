package com.example.domain.model

data class TripPoint(
    val id: Long = 0,
    val tripId: Long = 0,
    val sequenceNumber: Int = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speedMps: Float = 0f,
    val accuracyMeters: Float = 0f,
    val altitudeMeters: Double? = null,
    val bearingDegrees: Float? = null,
    val status: TripStatus = TripStatus.MOVING
)

data class Trip(
    val id: Long = 0,
    val tripNumber: Int = 1,
    val title: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val totalDurationMillis: Long = 0L,
    val movingDurationMillis: Long = 0L,
    val waitingDurationMillis: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val averageSpeedMps: Float = 0f,
    val maxSpeedMps: Float = 0f,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val isCompleted: Boolean = true,
    val notes: String = ""
)

data class AppSettings(
    val movementThresholdKmh: Float = 2.0f,
    val idleDetectionDelaySeconds: Int = 10,
    val gpsUpdateIntervalSeconds: Int = 2,
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    val timeFormat: TimeFormat = TimeFormat.H24,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val keepScreenAwake: Boolean = false,
    val powerSavingDimScreen: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val fuelPricePerLiter: Double = 0.0,
    val fuelEconomyKmPerLiter: Double = 0.0,
    val fuelCurrencySymbol: String = "Rs."
) {
    val isCostCalculatorConfigured: Boolean
        get() = fuelPricePerLiter > 0.0 && fuelEconomyKmPerLiter > 0.0
}

data class TripStatistics(
    val totalTrips: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalMovingDurationMillis: Long = 0L,
    val totalWaitingDurationMillis: Long = 0L,
    val totalDurationMillis: Long = 0L,
    val averageDurationMillis: Long = 0L,
    val averageDistanceMeters: Double = 0.0,
    val longestTripDistanceMeters: Double = 0.0,
    val shortestTripDistanceMeters: Double = 0.0,
    val highestRecordedSpeedMps: Float = 0f,
    val totalDrivingHours: Double = 0.0
)
