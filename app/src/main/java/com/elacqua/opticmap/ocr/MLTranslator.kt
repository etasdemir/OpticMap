package com.elacqua.opticmap.ocr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.elacqua.opticmap.util.Languages
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import timber.log.Timber

class MLTranslator {
    private lateinit var translator: Translator
    private val _translatedText = MutableLiveData<String>()
    val  translatedText : LiveData<String> = _translatedText

    private fun downloadModel(text: String, from: Languages, to: Languages){
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Timber.e("Model Downloaded")
                translate(text, from, to)
            }
            .addOnFailureListener { exception ->
                Timber.e("downloadModel: ${exception.message}")
            }
    }

    fun translate(text : String, from: Languages, to: Languages){
        val options: TranslatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(from.shortName)
            .setTargetLanguage(to.shortName)
            .build()
        translator = Translation.getClient(options)
        translator.translate(text)
            .addOnSuccessListener { result ->
                _translatedText.value = result
            }
            .addOnFailureListener { exception ->
                Timber.e("translate: ${exception.message}") // Downloading model
                downloadModel(text, from, to)
            }
    }

    fun close(){
        translator.close()
    }
}