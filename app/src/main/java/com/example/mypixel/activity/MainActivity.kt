package com.example.mypixel.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mypixel.R
import com.example.mypixel.camera.view.CameraPreview

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        setupCameraPreview()
    }

    private fun setupCameraPreview() {
        val cameraPreview = findViewById<CameraPreview>(R.id.camera_preview)
    }
}