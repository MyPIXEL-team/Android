package com.example.mypixel.camera.gl

import android.opengl.GLES31

class GLFrameBuffer(width: Int, height: Int, target: Int, internalFormat: Int, type: Int) {
    private var mWidth = width
    private var mHeight = height
    private var mFrameBuffer: Int? = null
    private var mTexture: GLTexture? = null

    init {
        val frameBuffers = IntArray(1)
        GLES31.glGenFramebuffers(1, frameBuffers, 0)
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBuffers[0])
        mFrameBuffer = frameBuffers[0]
        mTexture = GLTexture(width, height, target, internalFormat, type)
    }

    fun bind() {
        mFrameBuffer ?: return
        mTexture ?: return

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, mFrameBuffer!!)
        GLES31.glViewport(0, 0, mWidth, mHeight)
        GLES31.glFramebufferTexture2D(
                GLES31.GL_FRAMEBUFFER,
                GLES31.GL_COLOR_ATTACHMENT0,
                mTexture!!.getTarget(),
                mTexture!!.getTexture(),
                0)
    }

    fun release() {
        mFrameBuffer?.let { frameBuffer ->
            GLES31.glDeleteFramebuffers(1, intArrayOf(frameBuffer), 0)
        }
        mFrameBuffer = null

        mTexture?.release()
        mTexture = null
    }

    fun getWidth(): Int = mWidth

    fun getHeight(): Int = mHeight

    fun getTexture(): GLTexture? = mTexture
}