package com.elacqua.opticmap.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptions
import java.lang.StringBuilder


object MLKitOCRHandler {
    fun runTextRecognition(imageUri: Uri, context: Context, callback: OCRResult) {
        val image = InputImage.fromFilePath(context, imageUri)
        val recognizer: TextRecognizer = TextRecognition.getClient( TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
//                val result = processTextRecognitionResult(texts)
                val result = texts.text
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