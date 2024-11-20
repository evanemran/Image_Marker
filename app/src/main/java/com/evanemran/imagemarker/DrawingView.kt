package com.evanemran.imagemarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Stack

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var path = Path()
    private val paint = Paint().apply {
        color = 0xFF000000.toInt() // Black color
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val paths = Stack<Path>() // Store paths for drawing
    private val undonePaths = Stack<Path>() // Store undone paths

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path = Path() // Create a new path
                path.moveTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                paths.push(path) // Save the path
                undonePaths.clear() // Clear undone paths when new path is drawn
                path = Path() // Reset current path
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw all saved paths
        for (p in paths) {
            canvas.drawPath(p, paint)
        }
        // Draw the current path
        canvas.drawPath(path, paint)
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            undonePaths.push(paths.pop()) // Move the last path to the undone stack
            invalidate()
        }
    }

    fun redo() {
        if (undonePaths.isNotEmpty()) {
            paths.push(undonePaths.pop()) // Move the last undone path back to paths
            invalidate()
        }
    }

    fun resetDrawing() {
        paths.clear()
        undonePaths.clear()
        path.reset()
        invalidate()
    }
}
