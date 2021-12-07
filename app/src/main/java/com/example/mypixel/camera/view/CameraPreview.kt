package com.example.mypixel.camera.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
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
import com.example.mypixel.filter.BeardFilter
import com.example.mypixel.filter.CapFilter
import com.example.mypixel.filter.Filter
import com.example.mypixel.filter.GogglesFilter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.nio.ByteBuffer
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
    private val mTransform = FloatArray(16)
    private lateinit var mFullQuadVertices: FloatBuffer
    private lateinit var mFaceDetector: FaceDetector
    private val mLockFaceDetecting = Any()
    private val mFaceDetectingRunnable: FaceDetectingRunnable = FaceDetectingRunnable()
    private var mFaceDetectingThread: Thread? = null
    private var mIsFaceDetectingThreadStarted: Boolean = false
    private var mIsFaceReady: Boolean = false
    private var mFace: Face? = null
    private val mInputImageWidth = 240
    private val mInputImageHeight = 320
    private var mInputImageScaleFactor: Float = 0.0f
    private val mFilters: Array<Filter> = arrayOf(CapFilter(), GogglesFilter(), BeardFilter())
    private var mCurrentFilter: Int = 0

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

        mInputImageScaleFactor = height / mInputImageHeight.toFloat()

        mFilters.forEach { filter ->
            filter.setWidth(mWidth)
            filter.setScaleFactor(mInputImageScaleFactor)
        }

        mSurfaceFrameBuffer = GLFrameBuffer(width, height, GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE)

        val oldSurfaceTexture: SurfaceTexture? = mSurfaceTexture

        mSurfaceTexture = SurfaceTexture(mSurfaceFrameBuffer!!.getTexture()!!.getTexture())
        mSurfaceTexture!!.setOnFrameAvailableListener(this)

        handler.post(this::setupCamera)

        oldSurfaceTexture?.release()

        requestRender()
    }

    override fun onDrawFrame(gl: GL10) {
        GLES31.glClearColor(0f, 0f, 0f, 1f)
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)

        mSurfaceTexture?.let { surfaceTexture ->
            if (mIsSurfaceTextureReady) {
                surfaceTexture.updateTexImage()
                surfaceTexture.getTransformMatrix(mTransform)
                mIsSurfaceTextureReady = false
            }

            mCopyShader?.let { copyShader ->
                copyShader.setTransform(mTransform)
                mSurfaceFrameBuffer?.let { surfaceFrameBuffer ->
                    runShaderProgram(copyShader, surfaceFrameBuffer, null)
                }
            } ?: return

            mFaceDetectingRunnable.setNextInputImage(getInputImage())

            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mIsFaceReady) {
            mFace?.let { face ->
                // TODO: Draw a facial filter.
            }

            mFace = null
            mIsFaceReady = false
        }
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
        createCamera()
        startCamera()
    }

    fun setFilter(position: Int) {
        if (position > mFilters.size) {
            return
        }

        mCurrentFilter = position
    }

    private fun init() {
        mFullQuadVertices = GLUtils().createFullQuadVertices()

        setEGLContextClientVersion(3)
        setRenderer(this)
        setWillNotDraw(false)
        renderMode = RENDERMODE_WHEN_DIRTY

        val faceDetectorOptions = FaceDetectorOptions.Builder()
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .build()
        mFaceDetector = FaceDetection.getClient(faceDetectorOptions)
    }

    private fun createCamera() {
        try {
            mSurfaceTexture?.let { surfaceTexture ->
                mCamera = Camera(context, surfaceTexture, mWidth, mHeight)
            }
        } catch (e: Exception) {
            handler.post { Toast.makeText(context, "Failed to create a camera", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun startCamera() {
        mCamera?.start()

        if (!mIsFaceDetectingThreadStarted) {
            mFaceDetectingRunnable.setActive(true)
            mFaceDetectingThread = Thread(mFaceDetectingRunnable).apply { start() }
            mIsFaceDetectingThreadStarted = true
        }
    }

    private fun getCurrentFrameAsBitmap(): Bitmap {
        val buffer = ByteBuffer.allocateDirect(mWidth * mHeight * 4)
        GLES31.glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, buffer)

        buffer.rewind()

        val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        return bitmap
    }

    private fun getInputBitmap(bitmap: Bitmap): Bitmap {
        val width = mInputImageWidth
        val height = mInputImageHeight
        val widthRatio = width.toFloat() / bitmap.width
        val heightRatio = height.toFloat() / bitmap.height

        val inputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val matrix = Matrix()
        matrix.setScale(widthRatio, heightRatio)
        matrix.postRotate(180.0f, width / 2.0f, height / 2.0f)

        val canvas = Canvas(inputBitmap)
        canvas.drawBitmap(bitmap, matrix, null)

        return inputBitmap
    }

    private fun getInputImage(): InputImage {
        val frameBitmap = getCurrentFrameAsBitmap()
        val inputBitmap = getInputBitmap(frameBitmap)

        return InputImage.fromBitmap(inputBitmap, 0)
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

            synchronized(mLockFaceDetecting) {
                mFaceDetectingRunnable.setActive(false)
                mFaceDetectingThread?.join()
                mFaceDetectingThread = null
                mIsFaceDetectingThreadStarted = false
            }
        }
    }

    private inner class FaceDetectingRunnable : Runnable {
        private var mIsActive: Boolean = false
        private val mLockNextInputImage = Any()
        private var mNextInputImage: InputImage? = null

        fun setActive(active: Boolean) {
            synchronized(mLockNextInputImage) {
                mIsActive = active
                mLockNextInputImage.notifyAll()
            }
        }

        fun setNextInputImage(inputImage: InputImage) {
            synchronized(mLockNextInputImage) {
                mNextInputImage = inputImage
                mLockNextInputImage.notifyAll()
            }
        }

        override fun run() {
            var inputImage: InputImage

            while (true) {
                synchronized(mLockNextInputImage) {
                    while (mIsActive && mNextInputImage == null) {
                        try {
                            mLockNextInputImage.wait()
                        } catch (e: Exception) {
                            return
                        }
                    }

                    if (!mIsActive) {
                        return
                    }

                    inputImage = mNextInputImage!!
                    mNextInputImage = null
                }

                synchronized(mLockFaceDetecting) {
                    mFaceDetector.process(inputImage).addOnSuccessListener { faces ->
                        if (faces.size > 0) {
                            mFace = faces[0]
                            mIsFaceReady = true
                        }
                    }
                }
            }
        }
    }
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
private fun Any.wait() = (this as Object).wait()

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
private fun Any.notifyAll() = (this as Object).notifyAll()