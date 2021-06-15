package com.elacqua.opticmap.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlacesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlace(place: Place): Long

    @Delete
    suspend fun deletePlace(place: Place)

    @Query("select * from place")
    suspend fun getAllPlaces(): List<Place>

}