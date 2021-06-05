package com.elacqua.opticmap.ocr

import android.content.Context
import android.graphics.*
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
                processTextRecognitionResult(texts, image, callback)
            }
            .addOnFailureListener { e ->
                callback.onFailure(e.stackTraceToString())
            }
    }

    private fun processTextRecognitionResult(
        texts: Text,
        image: InputImage,
        callback: OCRResultListener
    ) {
        val blocks = texts.textBlocks
        if (blocks.size == 0 || image.bitmapInternal == null) {
            return
        }
        val bitmap = image.bitmapInternal!!.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        var textCount = 0
        val paint = Paint()
        paint.color = Color.BLACK
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val element = elements[k]
                    if (element.boundingBox == null) {
                        textCount++
                        continue
                    }
                    translator.translate(
                        element.text,
                        object : TranslateResultListener {
                            override fun onSuccess(text: String) {
                                drawWhiteBox(canvas, element.boundingBox!!)
                                drawTextAccordingToBox(canvas, paint, element.boundingBox!!, text)
                                if (++textCount >= blocks.size * lines.size * elements.size) {
                                    callback.onSuccess(bitmap)
                                    translator.close()
                                }
                            }

                            override fun onFailure(message: String) {
                                Timber.e("translate: $message")
                                translator.close()
                                callback.onFailure(message)
                            }
                        })
                }
            }
        }
        return
    }

    private fun drawWhiteBox(canvas: Canvas, rect: Rect) {
        val backgroundPaint = Paint()
        backgroundPaint.color = Color.WHITE
        canvas.drawRect(rect, backgroundPaint)
    }

    private fun drawTextAccordingToBox(canvas: Canvas, paint: Paint, rect: Rect, text: String) {
//        val newWidth = paint.measureText(text, 0, text.length)
//        paint.textAlign = Paint.Align.CENTER
//        paint.textSize =
//            abs(rect.width()) / newWidth * paint.textSize
//        canvas.drawText(text, rect.left.toFloat(), rect.bottom.toFloat(), paint)

        val newWidth = paint.measureText(text, 0, text.length)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize =
            abs(rect.width()) / newWidth * paint.textSize

        val top = paint.fontMetrics.top
        val bottom = paint.fontMetrics.bottom
        val baseLineY = (rect.centerY() - top/2 - bottom/2)
        canvas.drawText(text, rect.centerX().toFloat(), baseLineY, paint)
    }
}