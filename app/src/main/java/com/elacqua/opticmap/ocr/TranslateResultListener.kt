package com.elacqua.opticmap.ocr

interface TranslateResultListener {
    fun onSuccess(text: String)
    fun onFailure(message: String)
}