package com.elacqua.opticmap.ocr

import com.elacqua.opticmap.util.Languages
import com.elacqua.opticmap.util.UIState
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import timber.log.Timber

class MLTranslator(private val langFrom: Languages, private val langTo: Languages) {
    private lateinit var translator: Translator

    private fun downloadModel(onFinish: () -> Unit){
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Timber.e("Model Downloaded")
                onFinish()
            }
            .addOnFailureListener { exception ->
                Timber.e("downloadModel: ${exception.message}")
            }
    }

    fun translate(text : String, callback: TranslateResultListener){
        UIState.isLoadingState.postValue(true)
        val options: TranslatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(langFrom.shortName)
            .setTargetLanguage(langTo.shortName)
            .build()
        translator = Translation.getClient(options)
        downloadModel {
            translator.translate(text)
                .addOnCompleteListener {
                    UIState.isLoadingState.postValue(false)
                }
                .addOnSuccessListener { result ->
                    callback.onSuccess(result)
                }
                .addOnFailureListener { exception ->
                    callback.onFailure(exception.stackTraceToString())
                }
        }
    }

    fun close(){
        translator.close()
    }
}