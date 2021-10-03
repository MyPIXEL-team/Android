package com.example.mypixel.camera.gl

import android.opengl.GLES31

class GLTexture(width: Int, height: Int, target: Int, internalFormat: Int, type: Int) {
    private var mWidth = width
    private var mHeight = height
    private var mTarget = target
    private var mInternalFormat = internalFormat
    private var mType = type
    private var mTexture: Int? = null

    init {
        val textures = IntArray(1)

        GLES31.glGenTextures(1, textures, 0)
        GLES31.glBindTexture(target, textures[0])

        GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE)
        GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE)
        GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR)
        GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR)

        mTexture = textures[0]

        if (target == GLES31.GL_TEXTURE_2D) {
            GLES31.glTexImage2D(
                    GLES31.GL_TEXTURE_2D,
                    0,
                    internalFormat,
                    width,
                    height,
                    0,
                    getFormatFromInternalFormat(internalFormat),
                    type,
                    null)
        }
    }

    fun release() {
        mTexture?.let { texture ->
            GLES31.glDeleteTextures(1, intArrayOf(texture), 0)
        }
        mTexture = null
    }

    fun getTexture(): Int = mTexture ?: -1

    fun getWidth(): Int = mWidth

    fun getHeight(): Int = mHeight

    fun getTarget(): Int = mTarget

    fun getInternalFormat(): Int = mInternalFormat

    fun getType(): Int = mType

    private fun getFormatFromInternalFormat(internalFormat: Int): Int {
        return when (internalFormat) {
            GLES31.GL_RGBA16F, GLES31.GL_SRGB8_ALPHA8 -> GLES31.GL_RGBA
            else -> internalFormat
        }
    }
}