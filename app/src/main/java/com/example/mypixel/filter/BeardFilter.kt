package com.example.mypixel.filter

import android.graphics.*
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour.*
import kotlin.math.cos
import kotlin.math.sin

class BeardFilter : Filter() {
    private var hairOutline = Path()
    private var hairTopOutline = Path()
    private var beardOutline = Path()

    private var hairDotBottomLeft: PointF = PointF()
    private var hairDotBottomRight: PointF = PointF()

    private var leftEyeSizeHalf: Float = 0.0f
    private var rightEyeSizeHalf: Float = 0.0f

    private var leftLip: PointF = PointF()
    private var rightLip: PointF = PointF()

    override fun onDraw(canvas: Canvas, face: Face) {
        face.getContour(RIGHT_CHEEK)?.let { onDrawRightCheek(canvas, it.points) }
        face.getContour(FACE)?.let { onDrawFace(canvas, it.points) }
        face.getContour(LEFT_CHEEK)?.let { onDrawLeftCheek(canvas, it.points) }
        face.getContour(RIGHT_EYEBROW_TOP)?.let { onDrawRightEyebrowTop(canvas, it.points) }
        face.getContour(LEFT_EYEBROW_TOP)?.let { onDrawLeftEyebrowTop(canvas, it.points) }
        face.getContour(LEFT_EYE)?.let { onDrawLeftEye(canvas, it.points) }
        face.getContour(RIGHT_EYE)?.let { onDrawRightEye(canvas, it.points) }
        face.getContour(NOSE_BRIDGE)?.let { onDrawNoseBridge(canvas, it.points) }
        face.getContour(NOSE_BOTTOM)?.let { onDrawNoseBottom(canvas, it.points) }
        face.getContour(UPPER_LIP_TOP)?.let { onDrawUpperLipTop(canvas, it.points) }
        face.getContour(LOWER_LIP_BOTTOM)?.let { onDrawLowerLipBottom(canvas, it.points) }
    }

    override fun onDrawFace(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        val faceOutline = Path()

        val eyebrow = (points[5].y + points[points.size - 5].y) / 2.0f
        for (i in 0..5) {
            points[i].y = 2.0f * points[i].y - eyebrow
        }
        for (i in (points.size - 5) until (points.size)) {
            points[i].y = 2.0f * points[i].y - eyebrow
        }

        faceOutline.moveTo(translateX(points[0].x), translateY(points[0].y))
        points.forEach { point ->
            faceOutline.lineTo(translateX(point.x), translateY(point.y))
        }
        faceOutline.close()

        hairOutline.reset()
        hairTopOutline.reset()

        hairOutline.moveTo(translateX(points[points.size - 6].x), translateY(points[points.size - 6].y))
        hairTopOutline.moveTo(translateX(points[points.size - 6].x), translateY(points[points.size - 6].y))
        for (i in (points.size - 6) until (points.size)) {
            hairOutline.lineTo(translateX(points[i].x), translateY(points[i].y))
            hairTopOutline.lineTo(translateX(points[i].x), translateY(points[i].y))
        }
        for (i in 0..6) {
            hairOutline.lineTo(translateX(points[i].x), translateY(points[i].y))
            hairTopOutline.lineTo(translateX(points[i].x), translateY(points[i].y))
        }

        for (i in 9..27) {
            beardOutline.lineTo(translateX(points[i].x), translateY(points[i].y))
        }

        // Face color.
        paint.color = Color.parseColor("#EAD9D9")
        paint.style = Paint.Style.FILL
        faceOutline.fillType = Path.FillType.EVEN_ODD
        canvas.drawPath(faceOutline, paint)

        // Face outline.
        paint.color = Color.parseColor("#000000")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 40.0f
        canvas.drawPath(faceOutline, paint)
    }

    override fun onDrawLeftEyebrowTop(canvas: Canvas, points: List<PointF>) {
        for (i in points.indices) {
            val point = points[points.size - 1 - i]
            point.y += (leftEyeSizeHalf + rightEyeSizeHalf) / 8.0f
            hairOutline.lineTo(translateX(point.x), translateY(point.y))
        }
        hairOutline.close()

        // Hair.
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#655E5E")

        canvas.drawPath(hairOutline, paint)

        // Hair outline.

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#000000")
        paint.strokeWidth = 40.0f

        canvas.drawPath(hairTopOutline, paint)

        // Hair dot.
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#6E6E6E")

        val radian_90 = 1.5708
        val cos = cos(radian_90)
        val sin = sin(radian_90)

        val hairDotTopLeft = PointF(
                (cos * (hairDotBottomRight.x - hairDotBottomLeft.x) - sin * (hairDotBottomRight.y - hairDotBottomLeft.y) + hairDotBottomLeft.x).toFloat(),
                (sin * (hairDotBottomRight.x - hairDotBottomLeft.x) + cos * (hairDotBottomRight.y - hairDotBottomLeft.y) + hairDotBottomLeft.y).toFloat()
        )
        val hairDotTopRight = PointF(
                (cos * (hairDotBottomLeft.x - hairDotBottomRight.x) + sin * (hairDotBottomLeft.y - hairDotBottomRight.y) + hairDotBottomRight.x).toFloat(),
                (-sin * (hairDotBottomLeft.x - hairDotBottomRight.x) + cos * (hairDotBottomLeft.y - hairDotBottomRight.y) + hairDotBottomRight.y).toFloat()
        )

        val path = Path()

        path.moveTo(translateX(hairDotBottomLeft.x), translateY(hairDotBottomLeft.y))
        path.lineTo(translateX(hairDotBottomRight.x), translateY(hairDotBottomRight.y))
        path.lineTo(translateX(hairDotTopRight.x), translateY(hairDotTopRight.y))
        path.lineTo(translateX(hairDotTopLeft.x), translateY(hairDotTopLeft.y))
        path.close()

        canvas.drawPath(path, paint)

        val bottomLeft = hairDotTopRight
        val bottomRight = PointF(
                2.0f * hairDotTopRight.x - hairDotTopLeft.x,
                2.0f * hairDotTopRight.y - hairDotTopLeft.y
        )
        val topLeft = PointF(
                (cos * (bottomRight.x - bottomLeft.x) - sin * (bottomRight.y - bottomLeft.y) + bottomLeft.x).toFloat(),
                (sin * (bottomRight.x - bottomLeft.x) + cos * (bottomRight.y - bottomLeft.y) + bottomLeft.y).toFloat()
        )
        val topRight = PointF(
                (cos * (bottomLeft.x - bottomRight.x) + sin * (bottomLeft.y - bottomRight.y) + bottomRight.x).toFloat(),
                (-sin * (bottomLeft.x - bottomRight.x) + cos * (bottomLeft.y - bottomRight.y) + bottomRight.y).toFloat()
        )

        path.reset()

        path.moveTo(translateX(bottomLeft.x), translateY(bottomLeft.y))
        path.lineTo(translateX(bottomRight.x), translateY(bottomRight.y))
        path.lineTo(translateX(topRight.x), translateY(topRight.y))
        path.lineTo(translateX(topLeft.x), translateY(topLeft.y))
        path.close()

        canvas.drawPath(path, paint)
    }

    override fun onDrawRightEyebrowTop(canvas: Canvas, points: List<PointF>) {
        points.forEach { point ->
            point.y += (leftEyeSizeHalf + rightEyeSizeHalf) / 8.0f
            hairOutline.lineTo(translateX(point.x), translateY(point.y))
        }

        hairDotBottomLeft = points[2]
        hairDotBottomRight = points[3]
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
        paint.color = Color.parseColor("#A58D8D")
        canvas.drawRect(left, top, right, centerY, paint)

        // Bottom left.
        paint.color = Color.parseColor("#000000")
        canvas.drawRect(left, centerY, centerX, bottom, paint)

        // Bottom right.
        paint.color = Color.parseColor("#C9B2B2")
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
        paint.color = Color.parseColor("#A58D8D")
        canvas.drawRect(left, top, right, centerY, paint)

        // Bottom left.
        paint.color = Color.parseColor("#000000")
        canvas.drawRect(left, centerY, centerX, bottom, paint)

        // Bottom right.
        paint.color = Color.parseColor("#C9B2B2")
        canvas.drawRect(centerX, centerY, right, bottom, paint)

        rightEyeSizeHalf = (rightmostPoint.x - leftmostPoint.x) / 2.0f
    }

    override fun onDrawUpperLipTop(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#692F08")

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
        paint.color = Color.parseColor("#692F08")

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
        val left = translateX(point.x - noseSize * 1.8f)
        val right = translateX(point.x + noseSize * 1.8f)
        val top = translateY(point.y - noseSize)
        val bottom = translateY(point.y + noseSize)

        canvas.drawRect(left, top, right, bottom, paint)
    }

    override fun onDrawNoseBottom(canvas: Canvas, points: List<PointF>) {
        val noseSize = (leftEyeSizeHalf + rightEyeSizeHalf) / 4.0f

        points.forEach { point ->
            point.y -= noseSize
            beardOutline.lineTo(translateX(point.x), translateY(point.y))
        }
        beardOutline.close()

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#000000")

        canvas.drawPath(beardOutline, paint)
    }

    override fun onDrawLeftCheek(canvas: Canvas, points: List<PointF>) {
        beardOutline.lineTo(translateX(points[0].x), translateY(points[0].y))
    }

    override fun onDrawRightCheek(canvas: Canvas, points: List<PointF>) {
        beardOutline.reset()
        beardOutline.moveTo(translateX(points[0].x), translateY(points[0].y))
    }
}