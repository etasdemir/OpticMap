package com.elacqua.opticmap.ocr

import android.graphics.Bitmap

interface OCRResultListener {
    fun onSuccess(bitmap: Bitmap?)
    fun onFailure(message: String)
}