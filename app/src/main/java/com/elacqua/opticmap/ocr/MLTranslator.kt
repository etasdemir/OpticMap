package com.elacqua.opticmap.ocr

import com.elacqua.opticmap.util.Languages
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import timber.log.Timber

class MLTranslator(langFrom: Languages, langTo: Languages) {
    private var translator: Translator

    init {
        val options: TranslatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(langFrom.shortName)
            .setTargetLanguage(langTo.shortName)
            .build()
        translator = Translation.getClient(options)
    }

    fun downloadModel(downloadOnFinish: (Boolean) -> Unit) {
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                downloadOnFinish(true)
            }
            .addOnFailureListener { exception ->
                Timber.e("downloadModel: ${exception.message}")
                downloadOnFinish(false)
            }
    }

    fun translate(text: String, callback: TranslateResultListener) {
        translator.translate(text)
            .addOnSuccessListener { result ->
                callback.onSuccess(result)
            }
            .addOnFailureListener { exception ->
                callback.onFailure(exception.stackTraceToString())
            }
    }

    fun close() {
        translator.close()
    }
}