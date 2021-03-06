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


class MLKitOCRHandler(private val translator: MLTranslator) {
    fun runTextRecognition(image: InputImage, ocrResultListener: OCRResultListener<Text>) {
        val recognizer: TextRecognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                ocrResultListener.onSuccess(texts)
            }
            .addOnFailureListener { e ->
                ocrResultListener.onFailure(e.stackTraceToString())
            }
    }

    fun translateText(
        image: Bitmap,
        texts: Text,
        recognitionOptions: RecognitionOptions,
        ocrResultListener: OCRResultListener<Bitmap>
    ) {
        if (texts.textBlocks.size > 0) {
            val bitmap = image.copy(Bitmap.Config.ARGB_8888, true)
            when (recognitionOptions) {
                RecognitionOptions.TRANSLATE_BLOCKS ->
                    processImageBlocks(texts, bitmap, ocrResultListener)
                RecognitionOptions.TRANSLATE_LINES ->
                    processImageLines(texts, bitmap, ocrResultListener)
                RecognitionOptions.TRANSLATE_WHOLE ->
                    processImageWhole(texts, bitmap, ocrResultListener)
            }
        }
    }

    fun ocrToSpeech(texts: Text, callback: TranslateResultListener) {
        translator.translate(texts.text, callback)
    }

    fun getImageFromUri(uri: Uri, context: Context): InputImage = InputImage.fromFilePath(context, uri)

    private fun processImageBlocks(
        texts: Text,
        bitmap: Bitmap,
        callback: OCRResultListener<Bitmap>
    ) {
        val blocks = texts.textBlocks
        val canvas = Canvas(bitmap)
        var textCount = 0
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
                                if (element.boundingBox != null) {
                                    drawBoxes(canvas, element.boundingBox!!, text)
                                }
                                if (++textCount >= blocks.size * lines.size * elements.size) {
                                    callback.onSuccess(bitmap)
                                }
                            }

                            override fun onFailure(message: String) {
                                Timber.e("processImageBlocks: $message")
                                callback.onFailure(message)
                            }
                        })

                }
            }
        }
        return
    }

    private fun processImageLines(
        texts: Text,
        bitmap: Bitmap,
        callback: OCRResultListener<Bitmap>
    ) {
        val blocks = texts.textBlocks
        val canvas = Canvas(bitmap)
        var lineCount = 0
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                translator.translate(
                    lines[j].text,
                    object : TranslateResultListener {
                        override fun onSuccess(text: String) {
                            if (lines[j].boundingBox != null) {
                                drawBoxes(canvas, lines[j].boundingBox!!, text)
                            }
                            if (++lineCount >= blocks.size * lines.size) {
                                callback.onSuccess(bitmap)
                            }
                        }

                        override fun onFailure(message: String) {
                            Timber.e("processImageLines: $message")
                            callback.onFailure(message)
                        }
                    })
            }
        }
        return
    }

    private fun processImageWhole(
        texts: Text,
        bitmap: Bitmap,
        callback: OCRResultListener<Bitmap>
    ) {
        val blocks = texts.textBlocks
        val canvas = Canvas(bitmap)
        var lineCount = 0
        for (i in blocks.indices) {
            translator.translate(
                blocks[i].text,
                object : TranslateResultListener {
                    override fun onSuccess(text: String) {
                        if (blocks[i].boundingBox != null) {
                            drawBoxes(canvas, blocks[i].boundingBox!!, text)
                        }
                        if (++lineCount >= blocks.size) {
                            callback.onSuccess(bitmap)
                        }
                    }

                    override fun onFailure(message: String) {
                        Timber.e("processImageLines: $message")
                        callback.onFailure(message)
                    }
                })
        }
        return
    }

    private fun drawBoxes(canvas: Canvas, rect: Rect, text: String) {
        drawWhiteBox(canvas, rect)
        drawTextAccordingToBox(canvas, rect, text)
    }

    private fun drawWhiteBox(canvas: Canvas, rect: Rect) {
        val backgroundPaint = Paint()
        backgroundPaint.color = Color.WHITE
        canvas.drawRect(rect, backgroundPaint)
    }

    private fun drawTextAccordingToBox(canvas: Canvas, rect: Rect, text: String) {
        val paint = Paint()
        val tempRect = Rect()
        paint.color = Color.BLACK
        paint.getTextBounds(text, 0, text.length, tempRect)
        if (tempRect.width() < rect.width() && tempRect.height() < rect.height()) {
            calculateMaxTextSize(text, paint, rect.width().toFloat(), rect.height().toFloat())
        } else {
            val newWidth = paint.measureText(text, 0, text.length)
            paint.textSize =
                abs(rect.width()) / newWidth * paint.textSize
        }
        canvas.drawText(text, rect.left.toFloat(), rect.bottom.toFloat(), paint)
    }

    private fun calculateMaxTextSize(
        text: String,
        paint: Paint,
        maxWidth: Float,
        maxHeight: Float
    ): Float {
        val bound = Rect()
        var size = 1.0f
        val step = 1.0f
        while (true) {
            paint.getTextBounds(text, 0, text.length, bound)
            if (bound.width() < maxWidth && bound.height() < maxHeight) {
                size += step
                paint.textSize = size
            } else {
                return size - step
            }
        }
    }

    fun closeTranslator() {
        translator.close()
    }
}