package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entity.TripEntity
import com.example.data.database.entity.TripPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun observeTripById(tripId: Long): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveOrInterruptedTrip(): TripEntity?

    @Query("SELECT MAX(tripNumber) FROM trips")
    suspend fun getMaxTripNumber(): Int?

    @Query("SELECT COUNT(*) FROM trips WHERE isCompleted = 1")
    fun getCompletedTripCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: Long)

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()

    // Points queries
    @Query("SELECT * FROM trip_points WHERE tripId = :tripId ORDER BY sequenceNumber ASC")
    fun getPointsForTrip(tripId: Long): Flow<List<TripPointEntity>>

    @Query("SELECT * FROM trip_points WHERE tripId = :tripId ORDER BY sequenceNumber ASC")
    suspend fun getPointsForTripList(tripId: Long): List<TripPointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<TripPointEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: TripPointEntity): Long

    @Query("DELETE FROM trip_points WHERE tripId = :tripId")
    suspend fun deletePointsForTrip(tripId: Long)

    @Transaction
    suspend fun saveCompletedTrip(trip: TripEntity, points: List<TripPointEntity>) {
        val tripId = insertTrip(trip)
        val mappedPoints = points.map { it.copy(tripId = tripId) }
        if (mappedPoints.isNotEmpty()) {
            insertPoints(mappedPoints)
        }
    }
}
