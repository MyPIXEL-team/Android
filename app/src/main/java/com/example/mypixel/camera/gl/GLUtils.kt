package com.example.mypixel.camera.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLUtils {
    private val sizeOfFloat = 4
    fun createFullQuadVertices(): FloatBuffer {
        val fullQuadCoords = floatArrayOf(-1f, +1f, -1f, -1f, +1f, +1f, +1f, -1f)
        val fullQuadVertices = ByteBuffer.allocateDirect(fullQuadCoords.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        fullQuadVertices.put(fullQuadCoords).clear()

        return fullQuadVertices
    }

    fun createFloatBuffer(coords: FloatArray) : FloatBuffer{
        val bb = ByteBuffer.allocateDirect(coords.size * sizeOfFloat)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }
}