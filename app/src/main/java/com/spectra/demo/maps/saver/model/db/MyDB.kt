package com.spectra.demo.maps.saver.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Database(
    entities = [MapData::class],
    version = 1,
    exportSchema = false
)
abstract class MyDB : RoomDatabase() {
    abstract fun savedMapsDao(): SavedMapsDao
}

@Module
class DBModule{

    @Single
    fun getDB(context: Context): MyDB = Room.databaseBuilder(context, MyDB::class.java,"SavedMaps.DB").build()

    @Factory
    fun savedMapsDAO(db: MyDB): SavedMapsDao = db.savedMapsDao()

}