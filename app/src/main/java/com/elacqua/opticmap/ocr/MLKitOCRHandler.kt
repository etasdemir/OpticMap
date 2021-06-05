package com.elacqua.opticmap.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import java.lang.StringBuilder


object MLKitOCRHandler {
    fun runTextRecognition(bitmap: Bitmap, callback: OCRResult) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer: TextRecognizer = TextRecognition.getClient()
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                val result = processTextRecognitionResult(texts)
                callback.onSuccess(result)
            }
            .addOnFailureListener { e ->
                callback.onFailure(e.stackTraceToString())
            }
    }

    private fun processTextRecognitionResult(texts: Text): String {
        val stringBuilder = StringBuilder()
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            return stringBuilder.toString()
        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    stringBuilder.append(elements[k])
                }
            }
        }
        return stringBuilder.toString()
    }
}