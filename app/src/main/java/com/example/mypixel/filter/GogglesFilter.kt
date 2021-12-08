package com.example.mypixel.filter

import android.graphics.*
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour.*

class GogglesFilter : Filter() {
    private var leftEye: PointF = PointF()
    private var leftEyeTop: Float = 0.0f
    private var leftEyeBottom: Float = 0.0f
    private var rightEye: PointF = PointF()
    private var rightEyeTop: Float = 0.0f
    private var rightEyeBottom: Float = 0.0f

    private var leftEyeSizeHalf: Float = 0.0f
    private var rightEyeSizeHalf: Float = 0.0f

    private var leftLip: PointF = PointF()
    private var rightLip: PointF = PointF()

    override fun onDraw(canvas: Canvas, face: Face) {
        face.getContour(FACE)?.let { onDrawFace(canvas, it.points) }
        face.getContour(LEFT_EYEBROW_TOP)?.let { onDrawLeftEyebrowTop(canvas, it.points) }
        face.getContour(LEFT_EYEBROW_BOTTOM)?.let { onDrawLeftEyebrowBottom(canvas, it.points) }
        face.getContour(RIGHT_EYEBROW_TOP)?.let { onDrawRightEyebrowTop(canvas, it.points) }
        face.getContour(RIGHT_EYEBROW_BOTTOM)?.let { onDrawRightEyebrowBottom(canvas, it.points) }
        face.getContour(LEFT_EYE)?.let { onDrawLeftEye(canvas, it.points) }
        face.getContour(RIGHT_EYE)?.let { onDrawRightEye(canvas, it.points) }
        face.getContour(UPPER_LIP_TOP)?.let { onDrawUpperLipTop(canvas, it.points) }
        face.getContour(LOWER_LIP_BOTTOM)?.let { onDrawLowerLipBottom(canvas, it.points) }
        face.getContour(NOSE_BRIDGE)?.let { onDrawNoseBridge(canvas, it.points) }
    }

    override fun onDrawFace(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        val faceOutline = Path()
        val hairOutline = Path()

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

        hairOutline.moveTo(translateX(points[points.size - 6].x), translateY(points[points.size - 6].y))
        for (i in (points.size - 6) until (points.size)) {
            hairOutline.lineTo(translateX(points[i].x), translateY(points[i].y))
        }
        for (i in 0..6) {
            hairOutline.lineTo(translateX(points[i].x), translateY(points[i].y))
        }
        hairOutline.close()

        // Face color.
        paint.color = Color.parseColor("#DBB180")
        paint.style = Paint.Style.FILL
        faceOutline.fillType = Path.FillType.EVEN_ODD
        canvas.drawPath(faceOutline, paint)

        // Hair color.
        paint.color = Color.parseColor("#4C4C4C")
        faceOutline.fillType = Path.FillType.EVEN_ODD
        canvas.drawPath(hairOutline, paint)

        // Face outline.
        paint.color = Color.parseColor("#000000")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 40.0f
        canvas.drawPath(faceOutline, paint)
    }

    override fun onDrawLeftEyebrowTop(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLeftEyebrowBottom(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawRightEyebrowTop(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawRightEyebrowBottom(canvas: Canvas, points: List<PointF>) {}

    override fun onDrawLeftEye(canvas: Canvas, points: List<PointF>) {
        val topY = points.minOf { point -> point.y }
        val bottomY = points.maxOf { point -> point.y }
        val centerY = (points[0].y + points[points.size / 2].y) / 2.0f

        rightEye = points[0]
        rightEyeTop = 2.0f * topY - centerY
        rightEyeBottom = 2.0f * bottomY - centerY

        val leftmostPoint = points[points.size / 2]
        val rightmostPoint = points[0]
        leftEyeSizeHalf = (rightmostPoint.x - leftmostPoint.x) / 2.0f
    }

    override fun onDrawRightEye(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        val path = Path()

        val topY = points.minOf { point -> point.y }
        val bottomY = points.maxOf { point -> point.y }
        val centerY = (points[0].y + points[points.size / 2].y) / 2.0f

        leftEye = points[points.size / 2]
        leftEyeTop = 2.0f * topY - centerY
        leftEyeBottom = 2.0f * bottomY - centerY

        paint.style = Paint.Style.FILL;
        paint.color = Color.parseColor("#000000")

        val leftShades = leftEye.x
        val rightShades = rightEye.x
        val topShades = (leftEyeTop + rightEyeTop) / 2.0f
        val bottomShades = (leftEyeBottom + rightEyeBottom) / 2.0f

        val outlineSize = (bottomShades - topShades) / 2.0f
        val outlineSizeHalf = outlineSize / 2.0f

        val outlineShadesTopLeft = PointF(leftShades + outlineSizeHalf, topShades - outlineSizeHalf)
        val outlineShadesTopRight = PointF(rightShades - outlineSizeHalf, topShades - outlineSizeHalf)
        val outlineShadesBottomLeft = PointF(leftShades + outlineSizeHalf, bottomShades + outlineSizeHalf)
        val outlineShadesBottomRight = PointF(rightShades - outlineSizeHalf, bottomShades + outlineSizeHalf)

        // Googles outline.
        val outlineGooglesTopLeft = PointF(outlineShadesTopLeft.x + outlineSize, outlineShadesTopLeft.y - outlineSize)
        val outlineGooglesTopRight = PointF(outlineShadesTopRight.x - outlineSize, outlineShadesTopRight.y - outlineSize)
        val outlineGooglesBottomLeft = PointF(outlineShadesBottomLeft.x + outlineSize, outlineShadesBottomLeft.y + outlineSize)
        val outlineGooglesBottomRight = PointF(outlineShadesBottomRight.x - outlineSize, outlineShadesBottomRight.y + outlineSize)

        paint.color = Color.parseColor("#000000")
        paint.style = Paint.Style.FILL

        path.moveTo(translateX(outlineGooglesTopLeft.x), translateY(outlineGooglesTopLeft.y))
        path.lineTo(translateX(outlineGooglesTopRight.x), translateY(outlineGooglesTopRight.y))
        path.lineTo(translateX(outlineGooglesBottomRight.x), translateY(outlineGooglesBottomRight.y))
        path.lineTo(translateX(outlineGooglesBottomLeft.x), translateY(outlineGooglesBottomLeft.y))
        path.close()

        canvas.drawPath(path, paint)

        // Shades outline.
        path.reset()

        paint.color = Color.parseColor("#B4B4B4")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = outlineSize * 3.0f

        path.moveTo(translateX(outlineShadesTopLeft.x), translateY(outlineShadesTopLeft.y))
        path.lineTo(translateX(outlineShadesTopRight.x), translateY(outlineShadesTopRight.y))
        path.lineTo(translateX(outlineShadesBottomRight.x), translateY(outlineShadesBottomRight.y))
        path.lineTo(translateX(outlineShadesBottomLeft.x), translateY(outlineShadesBottomLeft.y))
        path.close()

        canvas.drawPath(path, paint)

        paint.color = Color.parseColor("#8D8D8D")
        paint.style = Paint.Style.FILL

        val dotSize = outlineSize / 3.0f
        val shadesVertices = listOf(
                outlineShadesTopLeft,
                outlineShadesTopRight,
                outlineShadesBottomLeft,
                outlineShadesBottomRight
        )

        shadesVertices.forEach { vertex ->
            canvas.drawRect(
                    translateX(vertex.x + dotSize),
                    translateY(vertex.y - dotSize),
                    translateX(vertex.x - dotSize),
                    translateY(vertex.y + dotSize),
                    paint
            )
        }

        // Hair dots.
        paint.color = Color.parseColor("#636363")

        canvas.drawRect(
                translateX(outlineShadesTopLeft.x + 1.5f * dotSize),
                translateY(outlineShadesTopLeft.y - 6.0f * dotSize),
                translateX(outlineShadesTopLeft.x - 1.5f * dotSize),
                translateY(outlineShadesTopLeft.y - 3.0f * dotSize),
                paint
        )
        canvas.drawRect(
                translateX(outlineShadesTopLeft.x - 1.5f * dotSize),
                translateY(outlineShadesTopLeft.y - 9.0f * dotSize),
                translateX(outlineShadesTopLeft.x - 4.5f * dotSize),
                translateY(outlineShadesTopLeft.y - 6.0f * dotSize),
                paint
        )

        val leftmostPoint = points[points.size / 2]
        val rightmostPoint = points[0]
        rightEyeSizeHalf = (rightmostPoint.x - leftmostPoint.x) / 2.0f
    }

    override fun onDrawUpperLipTop(canvas: Canvas, points: List<PointF>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#000000")

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
        paint.color = Color.parseColor("#000000")

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
}