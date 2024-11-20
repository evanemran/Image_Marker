package com.evanemran.imagemarker

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private var scale = 1f
    private var minScale = 1f
    private var maxScale = 5f
    private val scaleGestureDetector = android.view.ScaleGestureDetector(context, ScaleListener())

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        // Handle drag and pan
        val action = event.action
        if (action == MotionEvent.ACTION_MOVE && scale > minScale) {
            val dx = event.x - lastTouchX
            val dy = event.y - lastTouchY
            matrix.postTranslate(dx, dy)
            imageMatrix = matrix
        }

        lastTouchX = event.x
        lastTouchY = event.y
        return true
    }

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private inner class ScaleListener : android.view.ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = scale * scaleFactor
            if (newScale in minScale..maxScale) {
                scale *= scaleFactor
                matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                imageMatrix = matrix
            }
            return true
        }

        override fun onScaleBegin(detector: android.view.ScaleGestureDetector): Boolean = true

        override fun onScaleEnd(detector: android.view.ScaleGestureDetector) {}
    }
}
