package com.lighthouse.beep.ui.feature.editor.model

import android.graphics.RectF
import androidx.annotation.StringRes
import com.lighthouse.beep.core.common.exts.toDate
import com.lighthouse.beep.ui.dialog.textinput.TextInputFormat
import com.lighthouse.beep.ui.dialog.textinput.TextInputParam
import com.lighthouse.beep.ui.feature.editor.R

@Suppress("unused")
internal enum class EditType(@StringRes val textResId: Int) {
    MEMO(R.string.editor_gifticon_property_memo) {
        override val maxLength: Int = 20

        override fun createEditDataWithText(value: String): EditData {
            return EditData.Memo(value)
        }

        override fun createTextInputParam(data: GifticonData): TextInputParam {
            return TextInputParam(
                text = data.memo,
                maxLength = maxLength,
                inputFormat = TextInputFormat.TEXT,
            )
        }

        override fun getText(data: GifticonData): String {
            return data.memo
        }
    },
    THUMBNAIL(R.string.editor_gifticon_property_thumbnail) {
        override fun createEditDataWithCrop(rect: RectF): EditData {
            return EditData.Thumbnail(rect)
        }
    },
    NAME(R.string.editor_gifticon_property_name) {
        override fun createEditDataWithText(value: String): EditData {
            return EditData.Name(value)
        }

        override fun createEditDataWithCrop(value: String, rect: RectF): EditData {
            return EditData.CropName(value, rect)
        }

        override fun createTextInputParam(data: GifticonData): TextInputParam {
            return TextInputParam(
                text = data.name,
                inputFormat = TextInputFormat.TEXT,
            )
        }

        override fun isInvalid(data: GifticonData): Boolean {
            return data.name.isEmpty()
        }

        override fun getText(data: GifticonData): String {
            return data.name
        }
    },
    BRAND(R.string.editor_gifticon_property_brand) {
        override fun createEditDataWithText(value: String): EditData {
            return EditData.Brand(value)
        }

        override fun createEditDataWithCrop(value: String, rect: RectF): EditData {
            return EditData.CropBrand(value, rect)
        }

        override fun createTextInputParam(data: GifticonData): TextInputParam {
            return TextInputParam(
                text = data.brand,
                inputFormat = TextInputFormat.TEXT,
            )
        }

        override fun isInvalid(data: GifticonData): Boolean {
            return data.brand.isEmpty()
        }

        override fun getText(data: GifticonData): String {
            return data.brand
        }
    },
    BARCODE(R.string.editor_gifticon_property_barcode) {
        private val validBarcodeCount = setOf(12, 14, 16, 18, 20, 22, 24)

        override fun createEditDataWithText(value: String): EditData {
            return EditData.Barcode(value)
        }

        override fun createEditDataWithCrop(value: String, rect: RectF): EditData {
            return EditData.CropBarcode(value, rect)
        }

        override fun createTextInputParam(data: GifticonData): TextInputParam {
            return TextInputParam(
                text = data.barcode,
                inputFormat = TextInputFormat.BARCODE,
            )
        }

        override fun isInvalid(data: GifticonData): Boolean {
            return data.barcode.length !in validBarcodeCount
        }

        override fun getText(data: GifticonData): String {
            return data.displayBarcode
        }
    },
    EXPIRED(R.string.editor_gifticon_property_expired) {
        override fun createEditDataWithCrop(value: String, rect: RectF): EditData {
            return EditData.CropExpired(value.toDate(), rect)
        }

        override fun getText(data: GifticonData): String {
            return data.displayExpired
        }
    },
    BALANCE(R.string.editor_gifticon_property_balance) {
        override fun createEditDataWithText(value: String): EditData {
            return EditData.Balance(value)
        }

        override fun createEditDataWithCrop(value: String, rect: RectF): EditData {
            return EditData.CropBalance(value, rect)
        }

        override fun createTextInputParam(data: GifticonData): TextInputParam {
            return TextInputParam(
                text = data.balance,
                inputFormat = TextInputFormat.BALANCE,
            )
        }

        override fun isInvalid(data: GifticonData): Boolean {
            return data.isCashCard && data.balance.isEmpty()
        }

        override fun getText(data: GifticonData): String {
            return data.displayBalance
        }
    };

    open val maxLength = Int.MAX_VALUE

    open fun createEditDataWithText(value: String): EditData = EditData.None

    open fun createEditDataWithCrop(value: String, rect: RectF): EditData = EditData.None

    open fun createEditDataWithCrop(rect: RectF): EditData = EditData.None

    open fun createTextInputParam(data: GifticonData) = TextInputParam.None

    open fun isInvalid(data: GifticonData): Boolean = false

    open fun getText(data: GifticonData): String = ""
}