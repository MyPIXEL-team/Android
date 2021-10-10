package com.example.mypixel.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.LifecycleObserver

class Camera(context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int) : LifecycleObserver {
    private val mContext: Context = context
    private val mCameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val mCameraId: String = getFrontCameraId()
    private var mIsCameraStarted: Boolean = false
    private var mCameraHandler: Handler? = null
    private var mCameraHandlerThread: HandlerThread? = null
    private var mCameraDevice: CameraDevice? = null
    private val mCameraDeviceStateCallback: CameraDevice.StateCallback = getCameraDeviceStateCallback()
    private var mCameraCaptureSession: CameraCaptureSession? = null
    private var mPreviewRequest: CaptureRequest.Builder? = null

    init {
        surfaceTexture.setDefaultBufferSize(width, height)
    }

    fun start() {
        if (!checkPermission()) {
            return
        }

        if (mIsCameraStarted) {
            return
        }

        startCameraHandler()

        mIsCameraStarted = true
    }

    fun release() {
        stopCameraHandler()
        stopCameraDevice()
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

    private fun getCameraDeviceStateCallback() = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            if (mCameraDevice == null) {
                mCameraDevice = cameraDevice
                // TODO: Need to create capture session here.
            }
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            mIsCameraStarted = false
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mIsCameraStarted = false
            cameraDevice.close()
            mCameraDevice = null
        }
    }

    private fun checkPermission(): Boolean {
        return checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun createPreviewRequest() {
        mPreviewRequest = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewRequest?.let {
            it.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_PREVIEW)
            it.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            it.addTarget(mSurface)
        }
    }

    private fun repeatPreviewRequest() {
        mPreviewRequest ?: return

        try {
            mCameraCaptureSession?.setRepeatingRequest(mPreviewRequest!!.build(), null, mCameraHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun stopCameraDevice() {
        mCameraDevice?.close()
        mCameraDevice = null
    }
}