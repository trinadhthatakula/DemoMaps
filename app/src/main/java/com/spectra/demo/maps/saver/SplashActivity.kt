package com.spectra.demo.maps.saver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spectra.demo.maps.saver.databinding.ActivitySplashBinding
import java.util.Timer
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Timer().schedule(5000){
            startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            finish()
        }

    }
}