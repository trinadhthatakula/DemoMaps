package com.spectra.demo.maps.saver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.ViewGroup
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
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolygon
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.mapClickEvents
import com.spectra.demo.maps.saver.databinding.ActivityMainBinding
import com.spectra.demo.maps.saver.model.CardPainter
import com.spectra.demo.maps.saver.model.CustomPainter
import com.spectra.demo.maps.saver.model.PolyMode.GON
import com.spectra.demo.maps.saver.model.PolyMode.LINE
import com.spectra.demo.maps.saver.model.supporter
import com.spectra.demo.maps.saver.model.utils.resToPx
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
    private var polyMode = GON
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
                        if (shouldLocate) {
                            animateMapTo(currentLocation)
                            shouldLocate = false
                        }
                    }
                }
            }
        }
    }

    private fun animateMapTo(latLng: LatLng = currentLocation) {
        gMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                mapZoom
            )
        )
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
                    startLocationUpdates()
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
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (gMap != null) {
                    startLocationUpdates()
                }
            }
        }

        setUpCurvedState()

        binding.zoomIn.setOnClickListener {
            mapZoom += 1
            animateMapTo(gMap?.cameraPosition?.target ?: currentLocation)
        }
        binding.zoomOut.setOnClickListener {
            mapZoom -= 1
            animateMapTo(gMap?.cameraPosition?.target ?: currentLocation)
        }

        binding.areaFab.setOnClickListener {
            polyMode = GON
            handleMarkers()
        }
        binding.distanceFab.setOnClickListener {
            polyMode = LINE
            handleMarkers()
        }

        binding.areaFab.hide()
        binding.distanceFab.hide()

        binding.saveMap.setOnClickListener {
            gMap?.snapshot {
                it?.let { mapSnap ->
                    supporter.mapSnap = (mapSnap)
                    startActivity(Intent(this, SnapPreviewActivity::class.java))
                }
            }
        }

    }

    override fun onPause() {
        if (gMap != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

    private var polyLine: Polyline? = null
    private var polyGon: Polygon? = null

    private fun handleMarkers() {

        val primaryColor = supporter.getColor(
            com.google.android.material.R.attr.colorPrimary,
            this@MainActivity
        )

        if (clickedPoints.size > 1)
            binding.distanceFab.show()
        if (clickedPoints.size > 2)
            binding.areaFab.show()
        when (polyMode) {
            GON -> {
                polyGon?.remove()
                polyLine?.remove()
                polyGon = null
                polyLine = null
                if (clickedPoints.size > 1) {
                    gMap?.addPolygon {
                        addAll(clickedPoints)
                        strokeColor(primaryColor)
                        fillColor(getColor(R.color.seed_green_faded))
                    }?.let { polyGon = it }
                }
            }

            LINE -> {
                polyGon?.remove()
                polyLine?.remove()
                polyGon = null
                polyLine = null
                if (clickedPoints.size > 1) {
                    gMap?.addPolyline {
                        addAll(clickedPoints)
                        color(primaryColor)
                    }?.let { polyLine = it }
                }
            }
        }

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
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


    private fun setUpCurvedState() {
        val cardHeight = R.dimen.profile_card_height.resToPx(this).toInt()
        val avatarMargin = R.dimen.avatar_margin.resToPx(this)
        binding.canvasFrame.addView(
            CustomPainter(
                context = this,
                width = ViewGroup.LayoutParams.MATCH_PARENT,
                height = cardHeight,
                painter = CardPainter(
                    color = supporter.getColor(
                        com.google.android.material.R.attr.colorPrimary,
                        this
                    ),
                    avatarMargin = avatarMargin
                )
            )
        )
    }


}
