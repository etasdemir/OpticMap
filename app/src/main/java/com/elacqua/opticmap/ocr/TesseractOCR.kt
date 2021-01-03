package com.elacqua.opticmap.ocr

import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI

object TesseractOCR {

    private lateinit var tess: TessBaseAPI

    suspend fun getText(imgBitmap: Bitmap, rootPath: String, language: String): String {
        if (!::tess.isInitialized) {
            initTesseract(rootPath, language)
        }
        return doOCR(imgBitmap)
    }

    private suspend fun initTesseract(rootPath: String, language: String) {
        tess = TessBaseAPI()
        tess.init(rootPath, language)
    }

    private suspend fun doOCR(imgBitmap: Bitmap): String {
        tess.setImage(imgBitmap)
        val result = tess.utF8Text ?: ""
        tess.end()
        return result
    }
}