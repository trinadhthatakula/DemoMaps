package com.spectra.demo.maps.saver.model

import android.graphics.*
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.spectra.demo.maps.saver.R
import com.spectra.demo.maps.saver.model.utils.RectFFactory
import com.spectra.demo.maps.saver.model.utils.bottomLeft
import com.spectra.demo.maps.saver.model.utils.bottomRight
import com.spectra.demo.maps.saver.model.utils.centerLeft
import com.spectra.demo.maps.saver.model.utils.inflate
import com.spectra.demo.maps.saver.model.utils.topLeft
import com.spectra.demo.maps.saver.model.utils.topRight

class CardPainter(
    @ColorInt private val color: Int = Color.GRAY,
    private val avatarMargin: Float
) : Painter {

    override fun paint(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val shapeBounds = RectFFactory.fromLTWH(0f, 0f, width, height)

        val curveCubic = RectFFactory.fromLTRB(
            shapeBounds.left + shapeBounds.width() * 0.25f,
            shapeBounds.top,
            shapeBounds.centerX(),
            shapeBounds.centerY() + (avatarMargin * 1.5f)
        )

        val curveCubic2 = RectFFactory.fromLTRB(
            shapeBounds.centerX(),
            shapeBounds.top,
            shapeBounds.left + shapeBounds.width() * 0.75f,
            shapeBounds.centerY() + (avatarMargin * 1.5f)
        )

        drawBackground(canvas, shapeBounds, curveCubic, curveCubic2)

    }


    private fun drawBackground(
        canvas: Canvas,
        bounds: RectF,
        curveCubic: RectF,
        curveCubic2: RectF,
    ) {
        val paint = Paint()
        paint.color = color
        val start1 = PointF(curveCubic.left, curveCubic.top)
        val handlePoint = PointF(curveCubic.left + (curveCubic.width() * 0.5f), curveCubic.top)
        val handlePoint2 = PointF(curveCubic.left + (curveCubic.width() * 0.5f), curveCubic.bottom)
        val end1 = PointF(curveCubic.right, curveCubic.bottom)

        //val start2 = PointF(curveCubic2.left,curveCubic2.top)
        val handlePoint3 =
            PointF(curveCubic2.left + (curveCubic2.width() * 0.5f), curveCubic2.bottom)
        val handlePoint4 = PointF(curveCubic2.left + (curveCubic2.width() * 0.5f), curveCubic2.top)
        val end2 = PointF(curveCubic2.right, curveCubic2.top)

        val backgroundPath = Path().apply {
            moveTo(bounds.left, bounds.centerY())
            quadTo(
                bounds.topLeft.x,
                bounds.topLeft.y,
                bounds.topLeft.x + bounds.height() / 2,
                bounds.topLeft.y
            )
            lineTo(start1.x, start1.y)
            cubicTo(
                handlePoint.x, handlePoint.y,
                handlePoint2.x, handlePoint2.y,
                end1.x, end1.y
            )
            cubicTo(
                handlePoint3.x, handlePoint3.y,
                handlePoint4.x, handlePoint4.y,
                end2.x, end2.y
            )
            lineTo(bounds.topRight.x - bounds.height() / 2, bounds.topRight.y)
            quadTo(bounds.topRight.x, bounds.topRight.y, bounds.right, bounds.centerY())
            quadTo(
                bounds.bottomRight.x,
                bounds.bottomRight.y,
                bounds.right - bounds.height() / 2,
                bounds.bottomRight.y
            )
            lineTo(bounds.bottomLeft.x + bounds.height() / 2, bounds.bottomLeft.y)
            quadTo(
                bounds.bottomLeft.x,
                bounds.bottomLeft.y,
                bounds.left,
                bounds.centerY()
            )
            close()
        }

        canvas.drawPath(backgroundPath, paint)
    }

}