package com.spectra.demo.maps.saver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spectra.demo.maps.saver.databinding.ActivityResultBinding
import com.spectra.demo.maps.saver.model.Supporter
import com.spectra.demo.maps.saver.model.getAsString
import org.koin.android.ext.android.inject
import java.io.File

class ResultActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityResultBinding.inflate(layoutInflater)
    }
    private val supporter: Supporter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val title = supporter.polyData.title

        binding.mapIv.setImageBitmap(supporter.mapSnap)
        binding.title.text = title
        binding.subTitle.text = supporter.polyData.desc

        binding.shareImg.setOnClickListener {
            supporter.mapSnap?.let { it1 -> supporter.shareBitmap(this, it1, title) }
        }

        binding.shareFile.setOnClickListener {
            val text = supporter.polyData.getAsString()
            val folder = File(filesDir, "geo_json")
            if (folder.exists() || folder.mkdirs()) {
                val file = File(folder, "${title}_${System.currentTimeMillis()}.gloc")
                if ((!file.exists() || file.delete()) && file.createNewFile())
                    if(supporter.saveToFile(text, file)){
                        supporter.shareFile(file,this)
                    }
            }
        }
    }

}