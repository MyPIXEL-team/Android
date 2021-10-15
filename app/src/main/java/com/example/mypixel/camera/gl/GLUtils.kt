package com.example.mypixel.camera.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLUtils {
    fun createFullQuadVertices(): FloatBuffer {
        val fullQuadCoords = floatArrayOf(-1f, +1f, -1f, -1f, +1f, +1f, +1f, -1f)
        val fullQuadVertices = ByteBuffer.allocateDirect(fullQuadCoords.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        fullQuadVertices.put(fullQuadCoords).clear()

        return fullQuadVertices
    }
}