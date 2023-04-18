package com.spectra.demo.maps.saver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spectra.demo.maps.saver.databinding.ActivitySnapPreviewBinding
import com.spectra.demo.maps.saver.model.supporter

class SnapPreviewActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE){
        ActivitySnapPreviewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supporter.mapSnap?.let {
            binding.snapIv.setImageBitmap(it)
        }

    }

}