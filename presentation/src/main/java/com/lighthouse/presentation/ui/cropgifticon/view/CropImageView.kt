package com.lighthouse.presentation.ui.cropgifticon.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.minus
import com.lighthouse.presentation.R
import com.lighthouse.presentation.extension.dp
import com.lighthouse.presentation.extension.getBitmap

class CropImageView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var originBitmap: Bitmap? = null
    private val originRectF = RectF()
    private val imageMatrix = Matrix()

    fun setOriginUri(uri: Uri) {
        originBitmap = context.contentResolver.getBitmap(uri)
        applyImageMatrix(originBitmap)
    }

    private fun applyImageMatrix(bitmap: Bitmap?) {
        if (width == 0 || height == 0 || bitmap == null) {
            return
        }

        val viewRatio = width.toFloat() / height
        val imageRatio = bitmap.width.toFloat() / bitmap.height
        val imageWidth: Int
        val imageHeight: Int
        if (viewRatio > imageRatio) {
            imageWidth = bitmap.width * height / bitmap.height
            imageHeight = height
        } else {
            imageWidth = width
            imageHeight = bitmap.height * width / bitmap.width
        }
        val horizontalMargin = (width - imageWidth) / 2f
        val verticalMargin = (height - imageHeight) / 2f
        originRectF.set(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        imageRect.set(horizontalMargin, verticalMargin, width - horizontalMargin, height - verticalMargin)
        cropRect.set(imageRect)

        applyMatrix(bitmap)
    }

    private fun applyMatrix(bitmap: Bitmap?) {
        if (width == 0 || height == 0 || bitmap == null) {
            return
        }
        val scale = (width / bitmap.width.toFloat()).coerceAtMost(height / bitmap.height.toFloat())
        imageMatrix.reset()
        imageMatrix.postScale(scale, scale)
        imageMatrix.mapRect(originRectF)

        imageMatrix.reset()
        imageMatrix.postTranslate((width - originRectF.width()) / 2, (height - originRectF.height()) / 2)
        imageMatrix.mapRect(originRectF)

        imageMatrix.reset()
        imageMatrix.postScale(
            zoom,
            zoom,
            (originRectF.right + originRectF.left) / 2,
            (originRectF.bottom + originRectF.top) / 2
        )
        imageMatrix.mapRect(originRectF)
    }

    private val backgroundPaint by lazy {
        Paint().apply {
            color = context.getColor(R.color.black_a60)
        }
    }
    private val guidelinePaint by lazy {
        Paint().apply {
            color = context.getColor(R.color.gray_400)
            strokeWidth = 1.dp
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }
    private val edgePaint by lazy {
        Paint().apply {
            color = context.getColor(R.color.white)
            strokeWidth = 1.dp
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    private val cornerPaint by lazy {
        Paint().apply {
            color = context.getColor(R.color.primary)
            strokeWidth = CORNER_THICKNESS
            isAntiAlias = true
        }
    }

    private val cropRect = RectF()
    private val imageRect = RectF()

    private var eventType = EventType.NONE

    private var touchRange: TouchRange? = null
    private val cropBaseRect = RectF()

    private val touchStartPos = PointF()
    private val touchEndPos = PointF()

    private var zoom = 2f
    private var zoomOffsetX = 0f
    private var zoomOffsetY = 0f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        applyImageMatrix(originBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        val bitmap = originBitmap
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, imageRect, null)
            drawShadow(canvas)
            if (eventType != EventType.NONE) {
                drawGuidelines(canvas)
            }
            drawEdge(canvas)
            drawCorner(canvas)
        }
    }

    private fun drawShadow(canvas: Canvas) {
        canvas.drawRect(imageRect.left, imageRect.top, imageRect.right, cropRect.top, backgroundPaint)
        canvas.drawRect(imageRect.left, cropRect.top, cropRect.left, cropRect.bottom, backgroundPaint)
        canvas.drawRect(cropRect.right, cropRect.top, imageRect.right, cropRect.bottom, backgroundPaint)
        canvas.drawRect(imageRect.left, cropRect.bottom, imageRect.right, imageRect.bottom, backgroundPaint)
    }

    private fun drawGuidelines(canvas: Canvas) {
        val gapWidth = cropRect.width() / 3
        val gapHeight = cropRect.height() / 3

        val x1 = cropRect.left + gapWidth
        val x2 = cropRect.right - gapWidth
        canvas.drawLine(x1, cropRect.top, x1, cropRect.bottom, guidelinePaint)
        canvas.drawLine(x2, cropRect.top, x2, cropRect.bottom, guidelinePaint)

        val y1 = cropRect.top + gapHeight
        val y2 = cropRect.bottom - gapHeight
        canvas.drawLine(cropRect.left, y1, cropRect.right, y1, guidelinePaint)
        canvas.drawLine(cropRect.left, y2, cropRect.right, y2, guidelinePaint)
    }

    private fun drawEdge(canvas: Canvas) {
        val line = edgePaint.strokeWidth / 2
        canvas.drawRect(
            cropRect.left + line,
            cropRect.top + line,
            cropRect.right - line,
            cropRect.bottom - line,
            edgePaint
        )
    }

    private fun drawCorner(canvas: Canvas) {
        val line = CORNER_THICKNESS / 2
        canvas.drawLine(
            cropRect.left + CORNER_THICKNESS,
            cropRect.top + line,
            cropRect.left + CORNER_LENGTH + CORNER_THICKNESS,
            cropRect.top + line,
            cornerPaint
        )
        canvas.drawLine(
            cropRect.left + line,
            cropRect.top,
            cropRect.left + line,
            cropRect.top + CORNER_LENGTH + CORNER_THICKNESS,
            cornerPaint
        )

        canvas.drawLine(
            cropRect.right - CORNER_THICKNESS,
            cropRect.top + line,
            cropRect.right - CORNER_LENGTH - CORNER_THICKNESS,
            cropRect.top + line,
            cornerPaint
        )
        canvas.drawLine(
            cropRect.right - line,
            cropRect.top,
            cropRect.right - line,
            cropRect.top + CORNER_LENGTH + CORNER_THICKNESS,
            cornerPaint
        )

        canvas.drawLine(
            cropRect.left + CORNER_THICKNESS,
            cropRect.bottom - line,
            cropRect.left + CORNER_LENGTH + CORNER_THICKNESS,
            cropRect.bottom - line,
            cornerPaint
        )
        canvas.drawLine(
            cropRect.left + line,
            cropRect.bottom,
            cropRect.left + line,
            cropRect.bottom - CORNER_LENGTH - CORNER_THICKNESS,
            cornerPaint
        )

        canvas.drawLine(
            cropRect.right - CORNER_THICKNESS,
            cropRect.bottom - line,
            cropRect.right - CORNER_LENGTH - CORNER_THICKNESS,
            cropRect.bottom - line,
            cornerPaint
        )
        canvas.drawLine(
            cropRect.right - line,
            cropRect.bottom,
            cropRect.right - line,
            cropRect.bottom - CORNER_LENGTH - CORNER_THICKNESS,
            cornerPaint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        setTouchPos(event)
        if (getEventType(event) == EventType.NONE) {
            return false
        }

        actionEvent()
        cleanEvent(event)
        return true
    }

    private fun setTouchPos(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            touchStartPos.set(event.x, event.y)
            cropBaseRect.set(cropRect)
        }
        touchEndPos.set(event.x, event.y)
    }

    private fun getEventType(event: MotionEvent): EventType {
        if (event.action == MotionEvent.ACTION_DOWN) {
            touchRange = getTouchRange(event.x, event.y)
            eventType = when (touchRange) {
                TouchRange.CENTER -> EventType.MOVE
                null -> EventType.NONE
                else -> EventType.RESIZE
            }
        }
        return eventType
    }

    private fun actionEvent() {
        when (eventType) {
            EventType.RESIZE -> resizeCrop()
            EventType.MOVE -> moveCrop()
            else -> {}
        }
    }

    private fun cleanEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                eventType = EventType.NONE
            }
            else -> {}
        }
    }

    private fun getTouchRange(x: Float, y: Float): TouchRange? {
        return when {
            containLeft(x, y) -> when {
                containTop(x, y) -> TouchRange.LEFT_TOP
                containBottom(x, y) -> TouchRange.LEFT_BOTTOM
                else -> TouchRange.LEFT
            }
            containRight(x, y) -> when {
                containTop(x, y) -> TouchRange.RIGHT_TOP
                containBottom(x, y) -> TouchRange.RIGHT_BOTTOM
                else -> TouchRange.RIGHT
            }
            containTop(x, y) -> TouchRange.TOP
            containBottom(x, y) -> TouchRange.BOTTOM
            containCenter(x, y) -> TouchRange.CENTER
            else -> null
        }
    }

    private fun containLeft(x: Float, y: Float) =
        x in cropRect.left - EDGE_TOUCH_RANGE..cropRect.left + EDGE_TOUCH_RANGE &&
            y in cropRect.top - EDGE_TOUCH_RANGE..cropRect.bottom + EDGE_TOUCH_RANGE

    private fun containRight(x: Float, y: Float) =
        x in cropRect.right - EDGE_TOUCH_RANGE..cropRect.right + EDGE_TOUCH_RANGE &&
            y in cropRect.top - EDGE_TOUCH_RANGE..cropRect.bottom + EDGE_TOUCH_RANGE

    private fun containTop(x: Float, y: Float) =
        x in cropRect.left - EDGE_TOUCH_RANGE..cropRect.right + EDGE_TOUCH_RANGE &&
            y in cropRect.top - EDGE_TOUCH_RANGE..cropRect.top + EDGE_TOUCH_RANGE

    private fun containBottom(x: Float, y: Float) =
        x in cropRect.left - EDGE_TOUCH_RANGE..cropRect.right + EDGE_TOUCH_RANGE &&
            y in cropRect.bottom - EDGE_TOUCH_RANGE..cropRect.bottom + EDGE_TOUCH_RANGE

    private fun containCenter(x: Float, y: Float) = cropRect.contains(x, y)

    private fun moveCrop() {
        val diff = touchEndPos.minus(touchStartPos)

        val offsetX = when {
            cropBaseRect.left + diff.x < imageRect.left -> imageRect.left - cropBaseRect.left
            cropBaseRect.right + diff.x > imageRect.right -> imageRect.right - cropBaseRect.right
            else -> diff.x
        }
        val offsetY = when {
            cropBaseRect.top + diff.y < imageRect.top -> imageRect.top - cropBaseRect.top
            cropBaseRect.bottom + diff.y > imageRect.bottom -> imageRect.bottom - cropBaseRect.bottom
            else -> diff.y
        }

        cropRect.apply {
            set(cropBaseRect)
            offset(offsetX, offsetY)
        }
        invalidate()
    }

    private fun resizeCrop() {
        val range = touchRange ?: return
        val diff = touchEndPos.minus(touchStartPos)

        when (range) {
            TouchRange.LEFT, TouchRange.LEFT_TOP, TouchRange.LEFT_BOTTOM -> resizeLeft(diff.x)
            TouchRange.RIGHT, TouchRange.RIGHT_TOP, TouchRange.RIGHT_BOTTOM -> resizeRight(diff.x)
            else -> {}
        }

        when (range) {
            TouchRange.TOP, TouchRange.LEFT_TOP, TouchRange.RIGHT_TOP -> resizeTop(diff.y)
            TouchRange.BOTTOM, TouchRange.LEFT_BOTTOM, TouchRange.RIGHT_BOTTOM -> resizeBottom(diff.y)
            else -> {}
        }

        invalidate()
    }

    private fun resizeLeft(diffX: Float) {
        val movedLeft = cropBaseRect.left + diffX
        cropRect.left = when {
            movedLeft < imageRect.left -> imageRect.left
            movedLeft > cropRect.right - MIN_SIZE -> cropBaseRect.right - MIN_SIZE
            else -> movedLeft
        }
    }

    private fun resizeRight(diffX: Float) {
        val movedRight = cropBaseRect.right + diffX
        cropRect.right = when {
            movedRight > imageRect.right -> imageRect.right
            movedRight < cropRect.left + MIN_SIZE -> cropBaseRect.left + MIN_SIZE
            else -> movedRight
        }
    }

    private fun resizeTop(diffY: Float) {
        val movedTop = cropBaseRect.top + diffY
        cropRect.top = when {
            movedTop < imageRect.top -> imageRect.top
            movedTop > cropRect.bottom - MIN_SIZE -> cropBaseRect.bottom - MIN_SIZE
            else -> movedTop
        }
    }

    private fun resizeBottom(diffY: Float) {
        val movedBottom = cropBaseRect.bottom + diffY
        cropRect.bottom = when {
            movedBottom > imageRect.bottom -> imageRect.bottom
            movedBottom < cropRect.top + MIN_SIZE -> cropBaseRect.top + MIN_SIZE
            else -> movedBottom
        }
    }

    enum class TouchRange {
        LEFT, LEFT_TOP, TOP, RIGHT_TOP, RIGHT, RIGHT_BOTTOM, BOTTOM, LEFT_BOTTOM, CENTER
    }

    enum class EventType {
        NONE, MOVE, RESIZE
    }

    companion object {
        private val CORNER_THICKNESS = 3.dp
        private val CORNER_LENGTH = 24.dp.toInt()
        private val MIN_SIZE = (CORNER_LENGTH + CORNER_THICKNESS) * 2
        private val EDGE_TOUCH_RANGE = 24.dp.toInt()
    }
}
