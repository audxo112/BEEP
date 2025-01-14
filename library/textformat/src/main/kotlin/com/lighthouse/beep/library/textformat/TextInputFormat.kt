package com.lighthouse.beep.library.textformat

import android.text.InputFilter
import android.text.InputType
import com.lighthouse.beep.core.common.exts.toDigit
import com.lighthouse.beep.library.textformat.filter.DigitFilterWithMaxLength
import java.text.DecimalFormat

enum class TextInputFormat(
    val separator: Char = Char.MIN_VALUE,
    val filters: Array<InputFilter> = arrayOf(),
    val rawInputType: Int = InputType.TYPE_CLASS_TEXT,
    val inputType: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
) {
    TEXT,
    BALANCE(
        separator = ',',
        filters = arrayOf(DigitFilterWithMaxLength(7)),
        rawInputType = InputType.TYPE_CLASS_NUMBER,
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
    ) {
        private val balanceFormat = DecimalFormat("###,###,###")

        override fun valueToTransformed(value: String): String {
            val balance = value.toDigit()
            return if (balance > 0) balanceFormat.format(balance) else ""
        }

        override fun transformedToValue(text: String): String {
            return text.filter { it.isDigit() }.toDigit().toString()
        }
    },
    BARCODE(
        separator = ' ',
        filters = arrayOf(DigitFilterWithMaxLength(48)),
        rawInputType = InputType.TYPE_CLASS_NUMBER,
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
    ) {
        override fun valueToTransformed(value: String): String {
            return value.chunked(4).joinToString(separator.toString())
        }

        override fun transformedToValue(text: String): String {
            return text.filter { it.isDigit() }
        }
    };

    open fun valueToTransformed(value: String): String {
        return value
    }

    open fun transformedToValue(text: String): String {
        return text
    }
}
