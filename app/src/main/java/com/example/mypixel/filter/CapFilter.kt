package com.example.mypixel.filter

import android.graphics.*
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour.*
import kotlin.math.cos
import kotlin.math.sin

class CapFilter : Filter() {
    private var leftEyeSizeHalf: Float = 0.0f
    private var rightEyeSizeHalf: Float = 0.0f

    private var leftLip: PointF = PointF()
    private var rightLip: PointF = PointF()

    override fun onDraw(canvas: Canvas, face: Face) {
        face.getContour(FACE)?.let { onDrawFace(canvas, it.points) }
        face.getContour(LEFT_EYE)?.let { onDrawLeftEye(canvas, it.points) }
        face.getContour(RIGHT_EYE)?.let { onDrawRightEye(canvas, it.points) }
        face.getContour(UPPER_LIP_TOP)?.let { onDrawUpperLipTop(canvas, it.points) }
        face.getContour(LOWER_LIP_BOTTOM)?.let { onDrawLowerLipBottom(canvas, it.points) }
        face.getContour(NOSE_BRIDGE)?.let { onDrawNoseBridge(canvas, it.points) }
        face.getContour(RIGHT_CHEEK)?.let { onDrawRightCheek(canvas, it.points) }
    }

    override fun onDrawFace(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        val path = Path()

        path.moveTo(translateX(points[0].x), translateY(points[0].y))
        points.forEach { point ->
            path.lineTo(translateX(point.x), translateY(point.y))
        }
        path.close()

        // Face color.
        paint.color = Color.parseColor("#DBB180")
        paint.style = Paint.Style.FILL
        path.fillType = Path.FillType.EVEN_ODD
        canvas.drawPath(path, paint)

        // Face outline.
        paint.color = Color.parseColor("#000000")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 40.0f
        canvas.drawPath(path, paint)

        // Cap
        path.reset()

        val capPoints = mutableListOf<PointF>()
        for (i in (points.size - 5) until points.size) {
            capPoints.add(points[i])
        }
        for (i in 0..5) {
            capPoints.add(points[i])
        }

        val capCenter = (capPoints[0].x + capPoints[capPoints.size - 1].x) / 2.0f
        val capBottom = (capPoints[0].y + capPoints[capPoints.size - 1].y) / 2.0f

        capPoints.forEach { capPoint ->
            capPoint.x = (7.0f * capPoint.x - capCenter) / 6.0f
            capPoint.y = 2.0f * capPoint.y - capBottom
        }

        path.moveTo(translateX(capPoints[0].x), translateY(capPoints[0].y))
        capPoints.forEach { point ->
            path.lineTo(translateX(point.x), translateY(point.y))
        }

        // Cap inside.
        paint.color = Color.parseColor("#CA4E11")
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        // Cap bottom.
        val bottomPath = Path()
        bottomPath.moveTo(translateX(capPoints[0].x), translateY(capPoints[0].y))
        bottomPath.lineTo(translateX(capPoints[1].x), translateY(capPoints[1].y))
        bottomPath.lineTo(translateX(capPoints[capPoints.size - 2].x), translateY(capPoints[capPoints.size - 2].y))
        bottomPath.lineTo(translateX(capPoints[capPoints.size - 1].x), translateY(capPoints[capPoints.size - 1].y))
        bottomPath.close()

        paint.color = Color.parseColor("#933709")
        paint.style = Paint.Style.FILL
        canvas.drawPath(bottomPath, paint)

        // Cap outline.
        paint.color = Color.parseColor("#000000")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 40.0f
        canvas.drawPath(path, paint)

        // Cap dots.
        val capDots = mutableListOf<PointF>()
        val divisions = 11
        for (i in 0..divisions) {
            val x = (i.toFloat() * capPoints[0].x + (divisions - i).toFloat() * capPoints[capPoints.size - 1].x) / divisions.toFloat()
            val y = (i.toFloat() * capPoints[0].y + (divisions - i).toFloat() * capPoints[capPoints.size - 1].y) / divisions.toFloat()
            capDots.add(PointF(x, y))
        }

        val radian_90 = 1.5708
        val cos = cos(radian_90)
        val sin = sin(radian_90)
        for (i in listOf(2, 4, 6, 8, 10)) {
            val prev = capDots[i - 1]
            val cur = capDots[i]
            val next = capDots[i + 1]

            val bottomLeft = PointF((prev.x + cur.x) / 2.0f, (prev.y + cur.y) / 2.0f)
            val bottomRight = PointF((cur.x + next.x) / 2.0f, (cur.y + next.y) / 2.0f)
            val topLeft = PointF(
                    (cos * (bottomRight.x - bottomLeft.x) - sin * (bottomRight.y - bottomLeft.y) + bottomLeft.x).toFloat(),
                    (sin * (bottomRight.x - bottomLeft.x) + cos * (bottomRight.y - bottomLeft.y) + bottomLeft.y).toFloat()
            )
            val topRight = PointF(
                    (cos * (bottomLeft.x - bottomRight.x) + sin * (bottomLeft.y - bottomRight.y) + bottomRight.x).toFloat(),
                    (-sin * (bottomLeft.x - bottomRight.x) + cos * (bottomLeft.y - bottomRight.y) + bottomRight.y).toFloat()
            )

            paint.color = Color.parseColor("#CA4E11")
            paint.style = Paint.Style.FILL

            path.reset()

            path.moveTo(translateX(bottomLeft.x), translateY(bottomLeft.y))
            path.lineTo(translateX(bottomRight.x), translateY(bottomRight.y))
            path.lineTo(translateX(topRight.x), translateY(topRight.y))
            path.lineTo(translateX(topLeft.x), translateY(topLeft.y))
            path.close()

            canvas.drawPath(path, paint)
        }
    }

    override fun onDrawLeftEye(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL

        val leftmostPoint = points[points.size / 2]
        val rightmostPoint = points[0]
        val left = translateX(leftmostPoint.x)
        val right = translateX(rightmostPoint.x)
        val top = translateY((leftmostPoint.y + rightmostPoint.y) / 2.0f) - (right - left) / 2.0f
        val bottom = translateY((leftmostPoint.y + rightmostPoint.y) / 2.0f) + (right - left) / 2.0f
        val centerX = (left + right) / 2.0f
        val centerY = (top + bottom) / 2.0f

        // Upper.
        paint.color = Color.parseColor("#AF2C7B")
        canvas.drawRect(left, top, right, centerY, paint)

        // Bottom left.
        paint.color = Color.parseColor("#000000")
        canvas.drawRect(left, centerY, centerX, bottom, paint)

        // Bottom right.
        paint.color = Color.parseColor("#C13F8F")
        canvas.drawRect(centerX, centerY, right, bottom, paint)

        leftEyeSizeHalf = (rightmostPoint.x - leftmostPoint.x) / 2.0f
    }

    override fun onDrawRightEye(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL

        val leftmostPoint = points[points.size / 2]
        val rightmostPoint = points[0]
        val left = translateX(leftmostPoint.x)
        val right = translateX(rightmostPoint.x)
        val top = translateY((leftmostPoint.y + rightmostPoint.y) / 2.0f) - (right - left) / 2.0f
        val bottom = translateY((leftmostPoint.y + rightmostPoint.y) / 2.0f) + (right - left) / 2.0f
        val centerX = (left + right) / 2.0f
        val centerY = (top + bottom) / 2.0f

        // Upper.
        paint.color = Color.parseColor("#AF2C7B")
        canvas.drawRect(left, top, right, centerY, paint)

        // Bottom left.
        paint.color = Color.parseColor("#000000")
        canvas.drawRect(left, centerY, centerX, bottom, paint)

        // Bottom right.
        paint.color = Color.parseColor("#C13F8F")
        canvas.drawRect(centerX, centerY, right, bottom, paint)

        rightEyeSizeHalf = (rightmostPoint.x - leftmostPoint.x) / 2.0f
    }

    override fun onDrawUpperLipTop(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#711010")

        val leftmostPoint = points[points.size - 1]
        val rightmostPoint = points[0]
        val left = translateX(leftmostPoint.x)
        val right = translateX(rightmostPoint.x)
        val top = translateY((points.minOf { point -> point.y } + points.maxOf { point -> point.y }) / 2.0f)
        val bottom = translateY((leftmostPoint.y + rightmostPoint.y) / 2.0f)

        // Upper.
        canvas.drawRect(left, top, right, bottom, paint)

        leftLip = leftmostPoint
        rightLip = rightmostPoint
    }

    override fun onDrawLowerLipBottom(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#711010")

        val left = translateX(leftLip.x)
        val right = translateX(rightLip.x)
        val top = translateY((leftLip.y + rightLip.y) / 2.0f)
        val bottom = translateY((points.minOf { point -> point.y } + points.maxOf { point -> point.y }) / 2.0f)

        // Lower.
        canvas.drawRect(left, top, right, bottom, paint)
    }

    override fun onDrawNoseBridge(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#000000")

        val point = points[1]
        val noseSize = (leftEyeSizeHalf + rightEyeSizeHalf) / 4.0f
        val left = translateX(point.x - noseSize)
        val right = translateX(point.x + noseSize)
        val top = translateY(point.y - noseSize)
        val bottom = translateY(point.y + noseSize)

        canvas.drawRect(left, top, right, bottom, paint)
    }

    override fun onDrawRightCheek(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#917656")

        val point = points[0]
        val pointSize = (leftEyeSizeHalf + rightEyeSizeHalf) / 4.0f
        val left = translateX(point.x - pointSize)
        val right = translateX(point.x + pointSize)
        val top = translateY(point.y - pointSize)
        val bottom = translateY(point.y + pointSize)

        canvas.drawRect(left, top, right, bottom, paint)
    }
}