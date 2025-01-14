package com.lighthouse.beep.library.recognizer.parser

import com.google.mlkit.vision.barcode.common.Barcode
import com.lighthouse.beep.library.recognizer.model.BarcodeParserResult

internal class BarcodeParser {
    private val barcodeFilterRegex = listOf(
        "\\b(\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4})\\b".toRegex(),
        "\\b(\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{2})\\b".toRegex(),
        "\\b(\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4})\\b".toRegex(),
        "\\b(\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{2})\\b".toRegex(),
        "\\b(\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{4})\\b".toRegex(),
        "\\b(\\d{4}[- ]+\\d{4}[- ]+\\d{4}[- ]+\\d{2})\\b".toRegex(),
        "\\b(\\d{4}[- ]+\\d{5}[- ]+\\d{4})\\b".toRegex(),
        "\\b(\\d{4}[- ]+\\d{4}[- ]+\\d{4})\\b".toRegex(),
        "\\b(\\d{16})\\b".toRegex(),
        "\\b(\\d{14})\\b".toRegex(),
        "\\b(\\d{12})\\b".toRegex(),
    )

    fun parseBarcode(inputs: List<String>): BarcodeParserResult {
        var barcode = ""
        val barcodeFiltered = mutableListOf<String>()
        inputs.forEach { text ->
            if (barcode.isNotEmpty()) {
                barcodeFiltered.add(text)
            } else {
                val find = barcodeFilterRegex.firstNotNullOfOrNull { regex ->
                    regex.find(text)
                }
                if (find == null) {
                    barcodeFiltered.add(text)
                } else {
                    if (barcode == "") {
                        barcode = find.groupValues.getOrNull(1)?.filter { it.isDigit() } ?: ""
                    }
                }
            }
        }
        return BarcodeParserResult(
            Barcode.FORMAT_CODE_128,
            barcode,
            barcodeFiltered
        )
    }
}
