package com.lighthouse.presentation.ui.common.view

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import com.lighthouse.presentation.extension.toDigit
import java.text.DecimalFormat

class BalanceTextInputEditText(context: Context, attrs: AttributeSet) : FormattedTextInputEditText(context, attrs) {

    private val balanceFormat = DecimalFormat("###,###,###")

    init {
        filters = arrayOf(
            InputFilter.LengthFilter(10),
            InputFilter { source, _, _, _, dstStart, _ ->
                return@InputFilter if (dstStart == 0) {
                    var zeroIndex = 0
                    for (char in source) {
                        if (char != '0') {
                            break
                        }
                        zeroIndex += 1
                    }
                    source.subSequence(zeroIndex, source.length)
                } else {
                    source
                }
            }
        )
    }

    override fun onTransformedNewValue(
        newString: String,
        oldDisplayValue: String,
        start: Int,
        before: Int,
        count: Int
    ): String {
        return if (before == 1 && count == 0 && start < oldDisplayValue.length && oldDisplayValue[start] == ',') {
            transformedToValue(
                newString.substring(0, Integer.max(start - 1, 0)) + newString.substring(
                    Integer.max(start, 0),
                    newString.length
                )
            )
        } else {
            transformedToValue(newString)
        }
    }

    override fun calculateSelection(
        newDisplayValue: String,
        oldDisplayValue: String,
        start: Int,
        before: Int,
        count: Int
    ): Int {
        return if (oldDisplayValue.length == start + before || oldDisplayValue == "0") {
            newDisplayValue.length
        } else {
            val endStringCount = Integer.max(oldDisplayValue.length - start - before, 0)
            val oldDividerCount =
                oldDisplayValue.substring(start + before, oldDisplayValue.length).filter { it == ',' }.length
            val endNumCount = Integer.max(endStringCount - oldDividerCount, 0)
            var index = 0
            var numCount = 0
            while (newDisplayValue.lastIndex - index >= 0 && (numCount < endNumCount || newDisplayValue[newDisplayValue.lastIndex - index] == ',')) {
                if (newDisplayValue.lastIndex - index < 0) {
                    break
                }
                if (newDisplayValue[newDisplayValue.lastIndex - index] != ',') {
                    numCount += 1
                }
                index += 1
            }
            newDisplayValue.lastIndex - index + 1
        }
    }

    override fun valueToTransformed(text: String): String {
        return balanceFormat.format(text.toDigit())
    }

    override fun transformedToValue(text: String): String {
        return text.filter { it.isDigit() }.toDigit().toString()
    }
}
