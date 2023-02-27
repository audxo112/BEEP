package com.lighthouse.beep.model.gifticon

import com.lighthouse.beep.model.etc.Rectangle
import java.util.Date

data class GifticonForAddition(
    val hasImage: Boolean,
    val name: String,
    val brandName: String,
    val barcode: String,
    val expiredAt: Date,
    val isCashCard: Boolean,
    val totalCash: Int,
    val remainCash: Int,
    val originUri: String,
    val tempCroppedUri: String,
    val croppedRect: Rectangle,
    val memo: String
)
