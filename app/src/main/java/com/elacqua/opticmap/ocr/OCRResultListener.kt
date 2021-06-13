package com.elacqua.opticmap.ocr

interface OCRResultListener<T> {
    fun onSuccess(result: T?)
    fun onFailure(message: String)
}