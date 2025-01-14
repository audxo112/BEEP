package com.lighthouse.beep.library.recognizer.processor

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.max

internal abstract class BaseProcessor : ScaleProcessor() {

    private fun calculateRect(
        bitmap: Bitmap,
        leftPercent: Float,
        topPercent: Float,
        rightPercent: Float,
        bottomPercent: Float,
    ): Rect {
        return Rect(
            (bitmap.width * leftPercent).toInt(),
            (bitmap.height * topPercent).toInt(),
            (bitmap.width * rightPercent).toInt(),
            (bitmap.height * bottomPercent).toInt(),
        )
    }

    private fun cropBitmap(bitmap: Bitmap, rect: Rect): Bitmap {
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
    }

    protected fun cropGifticonImage(
        bitmap: Bitmap,
        leftPercent: Float,
        topPercent: Float,
        rightPercent: Float,
        bottomPercent: Float,
    ): GifticonProcessImage {
        val cropRect = calculateRect(bitmap, leftPercent, topPercent, rightPercent, bottomPercent)
        if (cropRect.width() != cropRect.height()) {
            cropRect.inset(
                max((cropRect.width() - cropRect.height()) / 2, 0),
                max((cropRect.height() - cropRect.width()) / 2, 0),
            )
        }
        return GifticonProcessImage(cropBitmap(bitmap, cropRect), cropRect)
    }

    protected fun cropAndScaleTextImage(
        tag: GifticonProcessTextTag,
        bitmap: Bitmap,
        leftPercent: Float,
        topPercent: Float,
        rightPercent: Float,
        bottomPercent: Float,
    ): GifticonProcessText {
        val cropRect = calculateRect(bitmap, leftPercent, topPercent, rightPercent, bottomPercent)
        return GifticonProcessText(tag, cropBitmap(bitmap, cropRect))
    }

    protected abstract fun processTextImage(bitmap: Bitmap): List<GifticonProcessText>

    protected abstract fun processGifticonImage(origin: Bitmap, bitmap: Bitmap): GifticonProcessImage



    open fun preProcess(bitmap: Bitmap): Bitmap {
        return bitmap
    }

    fun process(bitmap: Bitmap): GifticonProcessResult {
        val newBitmap = preProcess(bitmap)
        val image = processGifticonImage(bitmap, newBitmap)
        val textList = processTextImage(newBitmap)
        return GifticonProcessResult(image, textList)
    }
}
