package com.example.mypixel.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mypixel.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (areAllPermissionsAllowed()) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // TODO: Show button that can request permissions again.
                finish()
            }
        }
    }

    private fun checkPermissions() {
        if (areAllPermissionsAllowed()) {
            return
        }

        requestPermissions(PERMISSIONS_REQUIRED, PERMISSION_REQUEST_CODE)
    }

    private fun areAllPermissionsAllowed(): Boolean {
        for (permission in PERMISSIONS_REQUIRED) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    private val PERMISSION_REQUEST_CODE = 2000
    private val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.CAMERA
    )
}