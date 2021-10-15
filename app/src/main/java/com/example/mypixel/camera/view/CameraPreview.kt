package com.example.mypixel.camera.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.mypixel.camera.Camera
import com.example.mypixel.camera.gl.GLFrameBuffer
import com.example.mypixel.camera.gl.GLUtils
import com.example.mypixel.camera.shader.CopyShader
import com.example.mypixel.camera.shader.Shader
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraPreview : LifecycleObserver, GLSurfaceView, GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private var mLifecycle: Lifecycle? = null
    private var mCamera: Camera? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurfaceFrameBuffer: GLFrameBuffer? = null
    private var mIsSurfaceTextureReady: Boolean = false
    private var mCopyShader: CopyShader? = null
    private lateinit var mFullQuadVertices: FloatBuffer

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
        releaseResources()
        super.onPause()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        try {
            if (mCopyShader == null) {
                mCopyShader = CopyShader(context)
            }
        } catch (e: Exception) {
            handler.post { Toast.makeText(context, "Failed to create a shader", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        if (mWidth == width && mHeight == height) {
            return
        }

        mWidth = width
        mHeight = height

        mSurfaceFrameBuffer = GLFrameBuffer(width, height, GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE)

        val oldSurfaceTexture: SurfaceTexture? = mSurfaceTexture

        mSurfaceTexture = SurfaceTexture(mSurfaceFrameBuffer!!.getTexture()!!.getTexture())
        mSurfaceTexture!!.setOnFrameAvailableListener(this)

        handler.post(this::setupCamera)

        oldSurfaceTexture?.release()

        requestRender()
    }

    override fun onDrawFrame(gl: GL10) {
        // TODO("Not yet implemented")
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        queueEvent {
            mIsSurfaceTextureReady = true
            requestRender()
        }
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
        mFullQuadVertices = GLUtils().createFullQuadVertices()

        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private fun startCamera() {
    }

    private fun runShaderProgram(shader: Shader, inputFrameBuffer: GLFrameBuffer, outputFrameBuffer: GLFrameBuffer?) {
        if (outputFrameBuffer == null) {
            GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0)
            GLES31.glViewport(0, 0, mWidth, mHeight)
        } else {
            outputFrameBuffer.bind()
        }

        shader.onDraw(inputFrameBuffer.getTexture(), mFullQuadVertices)
    }

    private fun releaseCamera() {
        mCamera?.let { camera ->
            mLifecycle?.removeObserver(this)
            camera.release()
        }
        mCamera = null
    }

    private fun releaseResources() {
        queueEvent {
            mWidth = 0
            mHeight = 0

            mSurfaceFrameBuffer?.release()
            mSurfaceFrameBuffer = null

            mSurfaceTexture?.release()
            mSurfaceTexture = null

            mCopyShader?.release()
            mCopyShader = null
        }
    }
}