package com.lighthouse.beep.model.gifticon

import com.lighthouse.beep.model.etc.Rectangle
import java.util.Date

data class GifticonForUpdate(
    val id: String,
    val userId: String,
    val hasImage: Boolean,
    val name: String,
    val brandName: String,
    val barcode: String,
    val expiredAt: Date,
    val isCashCard: Boolean,
    val totalCash: Int,
    val remainCash: Int,
    val oldCroppedUri: String,
    val croppedUri: String,
    val croppedRect: Rectangle,
    val memo: String,
    val isUsed: Boolean,
    val createdAt: Date
) {
    val isUpdatedImage
        get() = oldCroppedUri != croppedUri
}
