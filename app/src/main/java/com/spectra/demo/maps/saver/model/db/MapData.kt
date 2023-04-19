package com.spectra.demo.maps.saver.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spectra.demo.maps.saver.model.PolyData
import com.spectra.demo.maps.saver.model.getAsString

@Entity(tableName = "SavedMaps")
data class MapData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "icon") var icon: Int = 0,
    @ColumnInfo(name = "polyData") var polyData: String = PolyData().getAsString(),
)
