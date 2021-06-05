package com.elacqua.opticmap.ocr

import android.graphics.Bitmap

interface OCRResultListener {
    fun onSuccess(text: String, bitmap: Bitmap?)
    fun onFailure(message: String)
}