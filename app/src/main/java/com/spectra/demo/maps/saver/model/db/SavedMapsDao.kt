package com.spectra.demo.maps.saver.model.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMapsDao {

    @Query("SELECT * FROM SavedMaps")
    fun getAllSavedMaps(): Flow<List<MapData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(mapData: MapData)

    @Query("DELETE FROM SavedMaps")
    suspend fun deleteAll()
    
    @Delete
    suspend fun deleteMapData(mapData: MapData)

    @Query("SELECT * FROM SavedMaps WHERE id == :id")
    fun getSaveMapsByID(id:Int) : Flow<List<MapData>>

}