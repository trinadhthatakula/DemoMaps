package com.spectra.demo.maps.saver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectra.demo.maps.saver.model.Supporter
import com.spectra.demo.maps.saver.model.db.MapData
import com.spectra.demo.maps.saver.model.db.SavedMapsRepository
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SavedMapsViewModel(
    private val supporter: Supporter,
    private val savedMapsRepository: SavedMapsRepository
): ViewModel() {

    val savedMaps = savedMapsRepository.getAllSavedMaps()

    fun getSavedMapByID(id:Int) = savedMapsRepository.getSavedMapsByID(id)

    fun insert(mapData: MapData, onInserted: () -> Unit){
        viewModelScope.launch {
            savedMapsRepository.insert(mapData)
            onInserted()
        }
    }

    fun delete(mapData: MapData){
        viewModelScope.launch {
            savedMapsRepository.delete(mapData)
        }
    }

    fun deleteAll(){
        viewModelScope.launch {
            savedMapsRepository.deleteAll()
        }
    }

}