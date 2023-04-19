package com.spectra.demo.maps.saver

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.spectra.demo.maps.saver.databinding.ActivitySnapPreviewBinding
import com.spectra.demo.maps.saver.model.Point
import com.spectra.demo.maps.saver.model.PolyData
import com.spectra.demo.maps.saver.model.Supporter
import com.spectra.demo.maps.saver.model.db.MapData
import com.spectra.demo.maps.saver.model.getAsString
import com.spectra.demo.maps.saver.viewModel.SavedMapsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SnapPreviewActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivitySnapPreviewBinding.inflate(layoutInflater)
    }
    private val supporter: Supporter by inject()
    private val viewModel: SavedMapsViewModel by viewModel()
    var iconSelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supporter.mapSnap?.let {
            binding.snapIv.setImageBitmap(it)
        }

        binding.saveBtn.setOnClickListener {
            val title: String = binding.titleTv.text.toString()
            val description: String = binding.DescriptionTv.text.toString()
            if (title.isBlank() || title.isEmpty()) {
                binding.titleTv.error = "Title Cannot be empty"
            } else {
                supporter.polyData = PolyData(
                    points = supporter.markedPoints.map { latLng ->
                        Point(
                            latLng.latitude,
                            latLng.longitude
                        )
                    },
                    type = supporter.polyMode.name,
                    title = title,
                    desc = description
                )
                val mapData = MapData(
                    icon = iconSelected,
                    polyData = supporter.polyData.getAsString()
                )
                viewModel.insert(mapData) {
                    Toast.makeText(this, "Added to Database", Toast.LENGTH_SHORT).show()
                    binding.editSlab.isVisible = false
                    startActivity(Intent(this,ResultActivity::class.java))
                }
            }
        }


    }

}