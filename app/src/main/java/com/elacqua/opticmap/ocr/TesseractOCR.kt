package com.elacqua.opticmap.ocr

import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI

object TesseractOCR {

    private lateinit var tess: TessBaseAPI

    fun getText(imgBitmap: Bitmap, rootPath: String, language: String): String {
        initTesseract(rootPath, language)
        return doOCR(imgBitmap)
    }

    private fun initTesseract(rootPath: String, language: String) {
        tess = TessBaseAPI()
        tess.init(rootPath, language)
    }

    private fun doOCR(imgBitmap: Bitmap): String {
        tess.setImage(imgBitmap)
        val result = tess.utF8Text ?: ""
        tess.end()
        return result
    }
}