package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.domain.model.GpsStatus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface LocationClient {
    fun getLocationUpdates(intervalSeconds: Int): Flow<LocationResultWrapper>
}

data class LocationResultWrapper(
    val location: Location? = null,
    val gpsStatus: GpsStatus = GpsStatus.AVAILABLE
)

class FusedLocationClientImpl(private val context: Context) : LocationClient {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(intervalSeconds: Int): Flow<LocationResultWrapper> = callbackFlow {
        val intervalMillis = (intervalSeconds.coerceAtLeast(1) * 1000).toLong()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis((intervalMillis / 2).coerceAtLeast(1000L))
            .setMaxUpdateDelayMillis(intervalMillis)
            .setMinUpdateDistanceMeters(0f)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val lastLocation = result.lastLocation
                if (lastLocation != null) {
                    val status = if (lastLocation.hasAccuracy() && lastLocation.accuracy > 25f) {
                        GpsStatus.WEAK
                    } else {
                        GpsStatus.AVAILABLE
                    }
                    trySend(LocationResultWrapper(location = lastLocation, gpsStatus = status))
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                super.onLocationAvailability(availability)
                if (!availability.isLocationAvailable) {
                    trySend(LocationResultWrapper(location = null, gpsStatus = GpsStatus.UNAVAILABLE))
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            ).addOnFailureListener {
                trySend(LocationResultWrapper(location = null, gpsStatus = GpsStatus.UNAVAILABLE))
            }
        } catch (e: SecurityException) {
            trySend(LocationResultWrapper(location = null, gpsStatus = GpsStatus.PERMISSION_REQUIRED))
        } catch (e: Exception) {
            trySend(LocationResultWrapper(location = null, gpsStatus = GpsStatus.UNAVAILABLE))
        }

        awaitClose {
            try {
                fusedClient.removeLocationUpdates(locationCallback)
            } catch (_: Exception) {
            }
        }
    }
}
