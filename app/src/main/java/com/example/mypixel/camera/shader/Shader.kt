package com.example.mypixel.camera.shader

import android.content.Context
import android.opengl.GLES31
import com.example.mypixel.camera.gl.GLTexture
import java.nio.FloatBuffer

abstract class Shader {
    private val POSITION_LOCATION = 0

    private var mProgram: Int = 0

    fun onDraw(texture: GLTexture?, cubeBuffer: FloatBuffer) {
        if (mProgram == 0) {
            return
        }

        cubeBuffer.clear()

        GLES31.glUseProgram(mProgram)
        GLES31.glVertexAttribPointer(POSITION_LOCATION, 2, GLES31.GL_FLOAT, false, 0, cubeBuffer)
        GLES31.glEnableVertexAttribArray(POSITION_LOCATION)

        if (texture != null) {
            GLES31.glActiveTexture(GLES31.GL_TEXTURE0)
            GLES31.glBindTexture(texture.getTarget(), texture.getTexture())
        }

        onDrawArraysBefore()
        GLES31.glDrawArrays(GLES31.GL_TRIANGLE_STRIP, 0, 4)
        onDrawArraysAfter()

        if (texture != null) {
            GLES31.glActiveTexture(GLES31.GL_TEXTURE0)
            GLES31.glBindTexture(texture.getTarget(), 0)
        }

        GLES31.glDisableVertexAttribArray(POSITION_LOCATION)
    }

    fun release() {
        if (mProgram != 0) {
            GLES31.glDeleteProgram(mProgram)
            mProgram = 0
        }
    }

    protected fun init(vertexSource: String, fragmentSource: String) {
        try {
            mProgram = loadProgram(vertexSource, fragmentSource)
        } catch (e: Exception) {
            if (e.message != null && e.message!!.contains("GL_OES_EGL_image_external_essl3")) {
                val newFragmentSource = fragmentSource.replace("GL_OES_EGL_image_external_essl3", "GL_OES_EGL_image_external")
                mProgram = loadProgram(vertexSource, newFragmentSource)
            } else {
                throw e
            }
        }
    }

    protected open fun onDrawArraysBefore() {
        // Do nothing.
    }

    protected open fun onDrawArraysAfter() {
        // Do nothing.
    }

    protected fun getString(context: Context, path: String): String {
        try {
            context.assets.open("$path.glsl").use { assetInputStream ->
                return assetInputStream.bufferedReader().readText()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun loadProgram(vertexSource: String, fragmentSource: String): Int {
        val program = GLES31.glCreateProgram()
        if (program == 0) {
            throw Exception("Failed to create a program")
        }

        compileAndAttachShader(program, GLES31.GL_VERTEX_SHADER, vertexSource)
        compileAndAttachShader(program, GLES31.GL_FRAGMENT_SHADER, fragmentSource)

        GLES31.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES31.glGetProgramiv(program, GLES31.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES31.GL_TRUE) {
            val log = GLES31.glGetProgramInfoLog(program)
            GLES31.glDeleteProgram(program)
            throw java.lang.Exception(log)
        }

        return program
    }

    private fun compileAndAttachShader(program: Int, shaderType: Int, source: String) {
        val shader = GLES31.glCreateShader(shaderType)
        if (shader == 0) {
            throw Exception("Failed to create a shader")
        }

        GLES31.glShaderSource(shader, source)
        GLES31.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] != GLES31.GL_TRUE) {
            val log = GLES31.glGetShaderInfoLog(shader)
            GLES31.glDeleteShader(shader)
            throw Exception(log)
        }

        GLES31.glAttachShader(program, shader)
        GLES31.glDeleteShader(shader)
    }
}