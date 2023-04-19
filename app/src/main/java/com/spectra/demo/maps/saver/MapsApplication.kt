package com.spectra.demo.maps.saver

import androidx.multidex.MultiDexApplication
import com.spectra.demo.maps.saver.model.AppModule
import com.spectra.demo.maps.saver.model.db.DBModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication
import org.koin.ksp.generated.module

class MapsApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MapsApplication)
            modules(listOf(AppModule().module, DBModule().module))
        }
    }

}