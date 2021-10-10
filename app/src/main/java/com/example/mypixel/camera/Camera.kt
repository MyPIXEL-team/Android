package com.example.mypixel.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.LifecycleObserver

class Camera(context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int) : LifecycleObserver {
    private val mContext: Context = context
    private val mCameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val mCameraId: String = getFrontCameraId()
    private var mIsCameraStarted: Boolean = false
    private var mCameraHandler: Handler? = null
    private var mCameraHandlerThread: HandlerThread? = null

    init {
        surfaceTexture.setDefaultBufferSize(width, height)
    }

    fun start() {
        if (mIsCameraStarted) {
            return
        }

        startCameraHandler()

        mIsCameraStarted = true
    }

    fun release() {
        stopCameraHandler()
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

    private fun startCameraHandler() {
        mCameraHandlerThread = HandlerThread("CameraHandlerThread").apply { start() }
        mCameraHandler = Handler(mCameraHandlerThread!!.looper)
    }

    private fun stopCameraHandler() {
        mCameraHandlerThread?.let {
            it.quitSafely()

            try {
                it.join(1000L)
                mCameraHandlerThread = null
                mCameraHandler = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}