package com.elacqua.opticmap.ocr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.*
import timber.log.Timber

class MLTranslator {
    private val _translatedText = MutableLiveData<String>()
    val  translatedText : LiveData<String> = _translatedText

    private var options: TranslatorOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.TURKISH)
        .build()
    private val translator: Translator = Translation.getClient(options)

    private fun downloadModel(text: String){
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Timber.e("Model Downloaded")
                translate(text)
            }
            .addOnFailureListener { exception ->
                Timber.e("downloadModel: ${exception.message}")
            }

    }

    fun translate(text : String){
        translator.translate(text)
            .addOnSuccessListener { result ->
                _translatedText.value = result
            }
            .addOnFailureListener { exception ->
                Timber.e("translate: ${exception.message}")
                downloadModel(text)
            }
    }

    fun close(){
        translator.close()
    }
}