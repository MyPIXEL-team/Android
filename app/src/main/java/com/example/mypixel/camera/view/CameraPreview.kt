package com.example.mypixel.camera.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.mypixel.camera.Camera

class CameraPreview : LifecycleObserver, GLSurfaceView {
    private var mLifecycle: Lifecycle? = null
    private var mCamera: Camera? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun onResume() {
        super.onResume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun onPause() {
        releaseCamera()
        super.onPause()
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        mLifecycle?.removeObserver(this)
        mLifecycle = lifecycleOwner.lifecycle
        mLifecycle!!.addObserver(this)
    }

    fun setupCamera() {
        releaseCamera()

        try {
            mCamera = Camera()
        } catch (e: Exception) {
            handler.post { Toast.makeText(context, "Failed to create a camera", Toast.LENGTH_SHORT).show() }
        }

        startCamera()
    }

    private fun init() {
    }

    private fun startCamera() {
    }

    private fun releaseCamera() {
        mCamera?.let { camera ->
            mLifecycle?.removeObserver(this)
            camera.release()
        }
        mCamera = null
    }
}