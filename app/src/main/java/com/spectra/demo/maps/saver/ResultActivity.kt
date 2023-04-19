package com.spectra.demo.maps.saver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spectra.demo.maps.saver.databinding.ActivityResultBinding
import com.spectra.demo.maps.saver.model.Supporter
import com.spectra.demo.maps.saver.model.getAsString
import org.koin.android.ext.android.inject

class ResultActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityResultBinding.inflate(layoutInflater)
    }
    private val supporter: Supporter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.resultTv.text = supporter.polyData.getAsString()

    }

}