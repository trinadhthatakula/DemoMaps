package com.spectra.demo.maps.saver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.mapClickEvents
import com.spectra.demo.maps.saver.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val permLauncher by lazy {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.keys.forEach { key ->
                if (result[key] != true) {
                    return@registerForActivityResult
                }
            }
            startLocationUpdates()
        }
    }
    private var shouldLocate = true
    private var mapZoom = 15f
    private var gMap: GoogleMap? = null
    private var currentLocation: LatLng = LatLng(0.0, 0.0)
    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val locations = locationResult.locations
                if (locations.isNotEmpty()) {
                    locations[0]?.let { location ->
                        currentLocation = LatLng(location.latitude, location.longitude)
                        if (shouldLocate)
                            gMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLocation,
                                    mapZoom
                                )
                            )
                    }
                }
            }
        }
    }
    private val clickedPoints = ArrayList<LatLng>()
    private val mapMarkers = ArrayList<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        permLauncher
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mapFragment.awaitMap().let { map ->
                    gMap = map
                    map.mapClickEvents().collect { clickedLatLng ->
                        map.addMarker {
                            position(clickedLatLng)
                            clickedPoints.add(clickedLatLng)
                        }?.let {
                            mapMarkers.add(it)
                            if (clickedPoints.size > 1) {
                                handleMarkers()
                            }
                        }

                    }
                }
            }
            repeatOnLifecycle(Lifecycle.State.RESUMED){
                if(gMap!=null){
                    startLocationUpdates()
                }
            }
        }

    }

    override fun onPause() {
        if(gMap!=null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

    private fun handleMarkers() {

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            gMap?.isMyLocationEnabled = true
            fusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.Builder(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    10 * 1000
                ).build(),
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

}
