package com.example.mypixel.camera.shader

import android.content.Context
import android.opengl.GLES31

class CopyShader(context: Context) : Shader() {
    private val TRANSFORM_LOCATION = 2

    private var mTransform: FloatArray? = null

    init {
        try {
            val vertexSource = getString(context, "glsl/copy_vs")
            val fragmentSource = getString(context, "glsl/copy_fs")
            init(vertexSource, fragmentSource)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun onDrawArraysBefore() {
        GLES31.glUniformMatrix4fv(TRANSFORM_LOCATION, 1, false, mTransform, 0)
    }

    fun setTransform(transform: FloatArray) {
        mTransform = transform
    }
}