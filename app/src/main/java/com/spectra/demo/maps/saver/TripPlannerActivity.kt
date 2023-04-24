package com.spectra.demo.maps.saver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
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
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.mapClickEvents
import com.spectra.demo.maps.saver.databinding.ActivityTripPlannerBinding
import com.spectra.demo.maps.saver.databinding.TripPlannerDialogBinding
import com.spectra.demo.maps.saver.model.CardPainter
import com.spectra.demo.maps.saver.model.CustomPainter
import com.spectra.demo.maps.saver.model.Supporter
import com.spectra.demo.maps.saver.model.bitmapFromVector
import com.spectra.demo.maps.saver.model.utils.resToPx
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TripPlannerActivity : AppCompatActivity() {

    private val redoList: ArrayList<LatLng> = ArrayList()
    private val supporter: Supporter by inject()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityTripPlannerBinding.inflate(layoutInflater)
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
    private val markers = listOf(
        R.drawable.marker_1,
        R.drawable.marker_2,
        R.drawable.marker_3,
        R.drawable.marker_4,
        R.drawable.marker_5,
        R.drawable.marker_6,
        R.drawable.marker_7,
        R.drawable.marker_8,
        R.drawable.marker_9,
        R.drawable.marker_10,
    )
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

    private val mapMarkers = ArrayList<Marker>()

    @SuppressLint("PotentialBehaviorOverride")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        permLauncher
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        binding.undoFab.hide()
        binding.clearFab.hide()
        binding.redoFab.hide()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mapFragment.awaitMap().let { map ->
                    gMap = map
                    //map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MainActivity,R.raw.night_maps))
                    startLocationUpdates()
                    map.setOnMarkerDragListener(
                        object : OnMarkerDragListener {
                            override fun onMarkerDrag(p0: Marker) {
                            }

                            override fun onMarkerDragEnd(draggedMarker: Marker) {
                                val tag = draggedMarker.tag
                                if (tag is Int) {
                                    mapMarkers[tag] = draggedMarker
                                    supporter.tripPoints[tag] = draggedMarker.position
                                }
                            }

                            override fun onMarkerDragStart(p0: Marker) {
                            }
                        })
                    map.mapClickEvents().collect { clickedLatLng ->
                        if (supporter.tripPoints.size < 10) {
                            map.addMarker {
                                position(clickedLatLng)
                                icon(
                                    bitmapFromVector(
                                        this@TripPlannerActivity,
                                        markers[supporter.tripPoints.size]
                                    )
                                )
                                supporter.tripPoints.add(clickedLatLng)
                            }?.let {
                                it.tag = mapMarkers.size
                                it.isDraggable = true
                                mapMarkers.add(it)
                                showMarkerDialog()
                            }
                            binding.undoFab.show()
                            binding.redoFab.show()
                            binding.clearFab.show()
                        } else {
                            Toast.makeText(
                                this@TripPlannerActivity,
                                "a maximum of 10 marker are allowed",
                                Toast.LENGTH_SHORT
                            ).show()
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

        binding.saveMap.setOnClickListener {
            gMap?.snapshot {
                it?.let { mapSnap ->
                    supporter.mapSnap = (mapSnap)
                    startActivity(Intent(this, SnapPreviewActivity::class.java))
                }
            }
        }

        binding.clearFab.setOnClickListener {
            redoList.clear()
            mapMarkers.clear()
            supporter.tripPoints.clear()
            polyLine?.remove()
            polyGon?.remove()
            gMap?.clear()
            animateMapTo(currentLocation)
            binding.undoFab.hide()
            binding.clearFab.hide()
            binding.redoFab.hide()
        }

        binding.undoFab.setOnClickListener {
            mapMarkers.removeLast().remove()
            val lastLatLng = supporter.tripPoints.removeLast()
            redoList.add(lastLatLng)

            if (mapMarkers.isEmpty()) {
                binding.undoFab.hide()
                binding.redoFab.hide()
                binding.clearFab.hide()
            }
            if (mapMarkers.size == 1) {
                binding.undoFab.hide()
                binding.redoFab.hide()
            }
            if (redoList.isNotEmpty()) {
                binding.redoFab.show()
            }

        }

        binding.redoFab.setOnClickListener {
            if (redoList.isNotEmpty()) {
                redoList.removeLast().let { lastLatLng ->
                    gMap?.addMarker {
                        position(lastLatLng)
                        supporter.tripPoints.add(lastLatLng)
                    }?.let {
                        it.tag = mapMarkers.size
                        it.isDraggable = true
                        mapMarkers.add(it)
                    }
                }
            } else binding.redoFab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
        }

    }

    private fun showMarkerDialog() {
        val d = Dialog(this)
        val tripPlannerDialogBinding = TripPlannerDialogBinding.inflate(layoutInflater)
        d.setContentView(tripPlannerDialogBinding.root)
        d.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        tripPlannerDialogBinding.apply {
            ok.setOnClickListener {

            }
            cancel.setOnClickListener {
                d.dismiss()
                supporter.markedPoints.removeLast()
                mapMarkers.removeLast().remove()
            }
        }
        d.show()

    }


    override fun onPause() {
        if (gMap != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

    private var polyLine: Polyline? = null
    private var polyGon: Polygon? = null

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
