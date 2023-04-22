package com.spectra.demo.maps.saver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolygon
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.spectra.demo.maps.saver.databinding.ActivityOpenerBinding
import com.spectra.demo.maps.saver.model.CardPainter
import com.spectra.demo.maps.saver.model.CustomPainter
import com.spectra.demo.maps.saver.model.PolyMode
import com.spectra.demo.maps.saver.model.Supporter
import com.spectra.demo.maps.saver.model.copyToFile
import com.spectra.demo.maps.saver.model.readFileText
import com.spectra.demo.maps.saver.model.toPolyData
import com.spectra.demo.maps.saver.model.utils.resToPx
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.newScope
import java.io.File

class OpenerActivity : AppCompatActivity() {

    private val supporter: Supporter by inject()
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityOpenerBinding.inflate(layoutInflater)
    }
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private var shouldLocate = false
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

    private val mapMarkers = ArrayList<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        permLauncher
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment


        if (intent != null && Intent.ACTION_VIEW == intent.action && intent.data != null) {
            val fileUri = intent.data

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    mapFragment.awaitMap().let { map ->
                        gMap = map
                        startLocationUpdates()
                        fileUri?.let { handleFileOpening(it) }
                    }
                }
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    if (gMap != null) {
                        startLocationUpdates()
                    }
                }
            }

        }

        binding.zoomIn.setOnClickListener {
            mapZoom += 1
            animateMapTo(gMap?.cameraPosition?.target ?: currentLocation)
        }
        binding.zoomOut.setOnClickListener {
            mapZoom -= 1
            animateMapTo(gMap?.cameraPosition?.target ?: currentLocation)
        }


        binding.saveMap.setOnClickListener {
            gMap?.snapshot {
                it?.let { mapSnap ->
                    supporter.mapSnap = (mapSnap)
                    startActivity(Intent(this, SnapPreviewActivity::class.java))
                }
            }
        }

        setUpCurvedState()

    }

    private fun handleFileOpening(fileUri: Uri) {
        val folder = File(filesDir, "shared_geo_json")
        if (folder.exists() || folder.mkdirs()) {
            val file = File(folder, "${System.currentTimeMillis()}.gloc")
            if (!file.exists() && file.createNewFile())
                if (fileUri.copyToFile(file, this)) {
                    val txt = file.readFileText()
                    Log.d("Opener", "handleFileOpening: file text = $txt")
                    txt.let { gloc ->
                        val polyData = gloc.toPolyData()
                        supporter.polyMode = PolyMode.valueOf(polyData.type)
                        supporter.markedPoints.clear()
                        mapMarkers.clear()
                        val latLngBoundsBuilder = LatLngBounds.builder()
                        polyData.points.forEach {
                            val pos = LatLng(it.lat, it.lng)
                            supporter.markedPoints.add(pos)
                            gMap?.addMarker {
                                latLngBoundsBuilder.include(pos)
                                position(pos)
                                supporter.markedPoints.add(pos)
                            }?.let { marker ->
                                mapMarkers.add(marker)
                            }
                        }
                        gMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(),20))
                        handleMarkers()
                    }
                }
        }
    }

    private var polyLine: Polyline? = null
    private var polyGon: Polygon? = null


    private fun handleMarkers() {

        val primaryColor = supporter.getColor(
            com.google.android.material.R.attr.colorPrimary,
            this@OpenerActivity
        )

        /*if (supporter.markedPoints.size > 1)
            binding.distanceFab.show()
        if (supporter.markedPoints.size > 2)
            binding.areaFab.show()*/
        when (supporter.polyMode) {
            PolyMode.GON -> {
                polyGon?.remove()
                polyLine?.remove()
                polyGon = null
                polyLine = null
                if (supporter.markedPoints.size > 1) {
                    gMap?.addPolygon {
                        addAll(supporter.markedPoints)
                        strokeColor(primaryColor)
                        fillColor(getColor(R.color.seed_green_faded))
                    }?.let { polyGon = it }
                }
            }

            PolyMode.LINE -> {
                polyGon?.remove()
                polyLine?.remove()
                polyGon = null
                polyLine = null
                if (supporter.markedPoints.size > 1) {
                    gMap?.addPolyline {
                        addAll(supporter.markedPoints)
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


    override fun onPause() {
        if (gMap != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onPause()
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