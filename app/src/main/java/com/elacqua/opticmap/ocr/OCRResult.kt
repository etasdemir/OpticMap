package com.elacqua.opticmap.ocr

interface OCRResult {
    fun onSuccess(text: String)
    fun onFailure(message: String)
}