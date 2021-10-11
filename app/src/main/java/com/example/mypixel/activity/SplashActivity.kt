package com.example.mypixel.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mypixel.BuildConfig
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
                startMainActivity()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showPermissionPopup()
                } else {
                    showSettingsPopup()
                }
            }
        }
    }

    private fun showPermissionPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.activity_splash, null)
        val alertDialog = AlertDialog.Builder(this)
                .setMessage(getString(R.string.permission_request_with_rationale))
                .setPositiveButton(getString(R.string.ok)) { _, _ -> checkPermissions() }
                .setNegativeButton(getString(R.string.no)) { _, _ -> finish() }
                .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun showSettingsPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.activity_splash, null)
        val alertDialog = AlertDialog.Builder(this)
                .setMessage(getString(R.string.permission_request_without_rationale))
                .setPositiveButton(getString(R.string.ok)) { _, _ -> startSettings() }
                .setNegativeButton(getString(R.string.no)) { _, _ -> finish() }
                .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun checkPermissions() {
        if (areAllPermissionsAllowed()) {
            startMainActivity()
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

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun startSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
        startActivity(intent)
    }

    private val PERMISSION_REQUEST_CODE = 2000
    private val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.CAMERA
    )
}