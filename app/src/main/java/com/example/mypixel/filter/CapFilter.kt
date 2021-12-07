package com.example.mypixel.filter

import android.graphics.Canvas
import android.graphics.PointF
import com.google.mlkit.vision.face.Face

class CapFilter : Filter() {
    override fun onDraw(canvas: Canvas, face: Face) {
    }

    override fun onDrawFace(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLeftEyebrowTop(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLeftEyebrowBottom(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawRightEyebrowTop(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawRightEyebrowBottom(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLeftEye(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawRightEye(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawUpperLipTop(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawUpperLipBottom(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLowerLipTop(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLowerLipBottom(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawNoseBridge(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawNoseBottom(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLeftCheek(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawRightCheek(canvas: Canvas, points: List<PointF>) {}
}