package com.elacqua.opticmap.ocr

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import timber.log.Timber
import java.lang.StringBuilder

object OCRHandler {
    private lateinit var textRecognizer: TextRecognizer

    fun getTextFromBitmap(image: Bitmap, appContext: Context): String {
        if (!this::textRecognizer.isInitialized) {
            textRecognizer = TextRecognizer.Builder(appContext).build()
        }
        var result = ""
        if (!textRecognizer.isOperational) {
            Timber.e("OCRHandler::getTextFromBitmap TextRecognizer is not operational ")
        } else {
            val frame = Frame.Builder().setBitmap(image).build()
            val items = textRecognizer.detect(frame)
            val stringBuilder = StringBuilder()
            for (i in 0 until items.size()) {
                val item = items[i]
                stringBuilder.append(item.value + "\n")
            }
            result = stringBuilder.toString()
        }
        return result
    }
}