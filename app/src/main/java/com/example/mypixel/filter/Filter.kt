package com.example.mypixel.filter

import android.graphics.Canvas
import android.graphics.PointF
import com.google.mlkit.vision.face.Face

abstract class Filter {
    private var mWidth: Int = 0
    private var mScaleFactor: Float = 0.0f

    open fun onDraw(canvas: Canvas, face: Face) {}

    fun setWidth(width: Int) {
        mWidth = width
    }

    fun setScaleFactor(scaleFactor: Float) {
        mScaleFactor = scaleFactor
    }

    protected open fun onDrawFace(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawLeftEyebrowTop(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawLeftEyebrowBottom(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawRightEyebrowTop(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawRightEyebrowBottom(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawLeftEye(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawRightEye(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawUpperLipTop(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawUpperLipBottom(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawLowerLipTop(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawLowerLipBottom(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawNoseBridge(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawNoseBottom(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawLeftCheek(canvas: Canvas, points: List<PointF>) {}

    protected open fun onDrawRightCheek(canvas: Canvas, points: List<PointF>) {}

    private fun translateX(x: Float): Float = mWidth - scale(x)

    private fun translateY(y: Float): Float = scale(y)

    private fun scale(pixel: Float): Float = pixel * mScaleFactor
}