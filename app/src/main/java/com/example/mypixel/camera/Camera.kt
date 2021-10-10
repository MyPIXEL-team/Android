package com.example.mypixel.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.lifecycle.LifecycleObserver

class Camera(context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int) : LifecycleObserver {
    private val mContext: Context = context
    private val mCameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val mCameraId: String = getFrontCameraId()

    init {
        surfaceTexture.setDefaultBufferSize(width, height)
    }

    fun start() {
        if (mIsCameraStarted) {
            return
        }

        mIsCameraStarted = true
    }

    fun release() {
        // TODO: Not yet implemented.
    }

    private fun getFrontCameraId(): String {
        val cameraIds = mCameraManager.cameraIdList

        for (cameraId in cameraIds) {
            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId)
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId
            }
        }

        throw Exception("Failed to find front camera id")
    }
}