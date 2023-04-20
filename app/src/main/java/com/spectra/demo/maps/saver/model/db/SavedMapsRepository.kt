package com.spectra.demo.maps.saver.model.db

import androidx.annotation.WorkerThread
import org.koin.core.annotation.Single

@Single
class SavedMapsRepository(private val savedMapsDao: SavedMapsDao) {

    fun getAllSavedMaps() = savedMapsDao.getAllSavedMaps()
    fun getSavedMapsByID(id:Int) = savedMapsDao.getSaveMapsByID(id)

    suspend fun insert(mapsData: MapData){
        savedMapsDao.insert(mapsData)
    }

    @WorkerThread
    suspend fun delete(mapsData: MapData){
        savedMapsDao.deleteMapData(mapsData)
    }

    @WorkerThread
    suspend fun deleteAll(){
        savedMapsDao.deleteAll()
    }

}