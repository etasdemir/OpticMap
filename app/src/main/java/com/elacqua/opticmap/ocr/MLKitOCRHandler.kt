package com.elacqua.opticmap.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptions
import timber.log.Timber
import kotlin.math.abs


class MLKitOCRHandler(
    private val context: Context,
    private val translator: MLTranslator
) {
    fun runTextRecognition(imageUri: Uri, callback: OCRResultListener) {
        val image = InputImage.fromFilePath(context, imageUri)
        val recognizer: TextRecognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                val bitmap = processTextRecognitionResult(texts, image)
                val result = texts.text
                callback.onSuccess(result, bitmap)
            }
            .addOnFailureListener { e ->
                callback.onFailure(e.stackTraceToString())
            }
    }

    private fun processTextRecognitionResult(texts: Text, image: InputImage): Bitmap? {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            return null
        }
        var bitmap: Bitmap? = null
        var canvas: Canvas? = null
        val paint = Paint()
        paint.color = Color.BLACK
        image.bitmapInternal?.let {
            bitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            bitmap?.let {
                canvas = Canvas(bitmap!!)
            }
        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val element = elements[k]
                    translator.translate(
                        element.text,
                        object : TranslateResultListener {
                            override fun onSuccess(text: String) {
                                val backgroundPaint = Paint()
                                backgroundPaint.color = Color.WHITE
                                canvas!!.drawRect(element.boundingBox!!, backgroundPaint)
                                val newWidth = paint.measureText(text)
                                paint.textSize =
                                    abs(element.boundingBox!!.width()) / newWidth * paint.textSize
                                canvas!!.drawText(
                                    text,
                                    element.boundingBox?.left?.toFloat() ?: 0f,
                                    element.boundingBox?.bottom?.toFloat() ?: 0f,
                                    paint
                                )
                            }

                            override fun onFailure(message: String) {
                                Timber.e("translate: $message")
                            }
                        })
                }
            }
        }
        return bitmap
    }
}